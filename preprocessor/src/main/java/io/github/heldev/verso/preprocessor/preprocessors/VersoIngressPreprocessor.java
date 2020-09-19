package io.github.heldev.verso.preprocessor.preprocessors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.github.heldev.verso.preprocessor.DatumTreeReader;
import io.github.heldev.verso.preprocessor.IngressField;
import io.github.heldev.verso.preprocessor.VersoIngress;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.PUBLIC;

public class VersoIngressPreprocessor {
    private final ProcessingEnvironment processingEnvironment;
    private final Filer filer;

    public VersoIngressPreprocessor(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.filer = processingEnvironment.getFiler();
    }

    public void generateReaders(RoundEnvironment roundEnv) {
        try {
            for (Element rootElement : roundEnv.getElementsAnnotatedWith(VersoIngress.class)) {
                if (rootElement.getKind().isClass()) {
                    var clazz = (TypeElement) rootElement;
                    MethodSpec method = buildReadMethod(clazz);

                    String readerName = clazz.getSimpleName().toString() + "$$IngressReader";
                    var type = TypeSpec.classBuilder(readerName)
                            .addMethod(method)
                            .build();

                    try {
                        String packageName = processingEnvironment.getElementUtils().getPackageOf(clazz).toString();

                        JavaFile.builder(packageName, type).build().writeTo(filer);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        } catch (Exception e) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Exception" + Arrays.toString(e.getStackTrace()));
        }
    }

    private MethodSpec buildReadMethod(TypeElement clazz) {

        return MethodSpec.methodBuilder("read")
                .addModifiers(PUBLIC)
                .returns(TypeName.get(clazz.asType()))
                .addParameter(TProtocol.class, "protocol")
                .addException(TException.class)
                .addStatement("var datumTree = new $T().readStruct(protocol)", DatumTreeReader.class)
                .addCode("return new $T", clazz)
                .addCode(buildConstructorCall(clazz))
                .addCode(";")
                .build();
    }

    private CodeBlock buildConstructorCall(TypeElement clazz) {
        ExecutableElement constructor = (ExecutableElement) clazz.getEnclosedElements().stream().filter(e -> e.getKind() == CONSTRUCTOR).findFirst().get();

        return constructor
                .getParameters()
                .stream()
                .map(this::toArgument)
                .collect(CodeBlock.joining(", ", "(", ")"));
    }

    private CodeBlock toArgument(VariableElement parameter) {
        var id = parameter.getAnnotation(IngressField.class).id();

        return CodeBlock.of("($T) datumTree.get((short) $L)", parameter.asType(), id);
    }

}
