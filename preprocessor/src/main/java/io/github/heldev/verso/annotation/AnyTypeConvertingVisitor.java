package io.github.heldev.verso.annotation;

import io.github.heldev.verso.stronglytyped.type.AnyType;
import io.github.heldev.verso.stronglytyped.type.TemplateType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;

import static java.util.stream.Collectors.toList;

public class AnyTypeConvertingVisitor extends SimpleTypeVisitor8<AnyType, Void> {

	@Override
	public AnyType visitTypeVariable(TypeVariable t, Void unused) {
		return TemplateType.parameter(t.toString());
	}

	@Override
	public AnyType visitPrimitive(PrimitiveType t, Void unused) {
		return switch (t.getKind()) {
			case INT -> AnyType.integer();
			case BOOLEAN -> AnyType.string();
			default -> throw new IllegalStateException("Unexpected value: " + t.getKind());
		};
	}

	@Override
	public TemplateType visitDeclared(DeclaredType type, Void unused) {
		var name = ((TypeElement) type.asElement()).getQualifiedName().toString();
		var parameters = type.getTypeArguments().stream().map(this::visit).collect(toList());
		return AnyType.template(name, parameters);
	}
}
