package io.github.heldev.verso.preprocessors;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import io.github.heldev.verso.AnyTypeConvertingVisitor;
import io.github.heldev.verso.interfaces.VersoConverter;
import io.github.heldev.verso.stronglytyped.Converter;
import io.github.heldev.verso.stronglytyped.Converters;
import io.github.heldev.verso.stronglytyped.type.TemplateType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class VersoConverterPreprocessor {
	private final Types typeUtils;
	private final Elements elementUtils;

	public VersoConverterPreprocessor(ProcessingEnvironment processingEnvironment) {
		typeUtils = processingEnvironment.getTypeUtils();
		elementUtils = processingEnvironment.getElementUtils();
	}

	public Converters findConverters(RoundEnvironment roundEnv) {

		return methodsIn(roundEnv.getElementsAnnotatedWith(VersoConverter.class)).stream()
				.peek(this::throwIfInvalid)
				.map(this::buildConverter)
				.collect(collectingAndThen(toSet(), Converters::new));
	}

	private Converter buildConverter(ExecutableElement converterMethod) {
		//todo move the check here
		var from = converterMethod.getParameters().get(0).asType();
		var to = converterMethod.getReturnType(); //todo check for null

		return new Converter(convertToModel(from), convertToModel(to));
	}

	private TemplateType convertToModel(TypeMirror typeMirror) {
		var type =  typeMirror.accept(new AnyTypeConvertingVisitor(), null);

		if (type instanceof TemplateType) {
			return (TemplateType) type;
		} else {
			throw new RuntimeException("Be careful with " + type);
		}
	}

	private void throwIfInvalid(ExecutableElement method) {
		if (! isValidConverterDeclaration(method)) {
			throw new RuntimeException("Converter-methods should be publicly accessible, static, exception-," +
					" wildcard- and bounds- free methods with one parameter " + method);
		}
	}

	private boolean isValidConverterDeclaration(ExecutableElement converter) {

		return isPublicStatic(converter)
				&& isExceptionFree(converter)
				&& hasSingleWildcardFreeParameter(converter)
				&& hasAllTypeParametersUnbounded(converter);
	}

	private boolean isPublicStatic(ExecutableElement converter) {

		return converter.getModifiers().contains(Modifier.STATIC)
				&& converter.getModifiers().contains(Modifier.PUBLIC);
	}

	private boolean isExceptionFree(ExecutableElement converter) {
		return converter.getThrownTypes().isEmpty();
	}

	private boolean hasSingleWildcardFreeParameter(ExecutableElement converter) {
		var parameters = converter.getParameters();

		return parameters.size() == 1 && parameters.stream()
				.allMatch(parameter -> isWildcardFree(TypeName.get(parameter.asType())));
	}

	private boolean isWildcardFree(TypeName type) {

		return type instanceof ParameterizedTypeName
				? ((ParameterizedTypeName) type).typeArguments.stream().allMatch(this::isWildcardFree)
				: ! (type instanceof WildcardTypeName);
	}

	private boolean hasAllTypeParametersUnbounded(ExecutableElement converter) {

		return converter
				.getTypeParameters().stream()
				.allMatch(parameter -> parameter.getBounds().stream().allMatch(this::isObject));
	}

	private boolean isObject(TypeMirror typeMirror) {
		var objectElement = elementUtils.getTypeElement(Object.class.getCanonicalName());

		return typeUtils.isSameType(typeMirror, typeUtils.getDeclaredType(objectElement));

	}
}
