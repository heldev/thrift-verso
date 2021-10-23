package io.github.heldev.verso;

import io.github.heldev.verso.stronglytyped.type.AnyType;
import io.github.heldev.verso.stronglytyped.type.TemplateType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AnyTypeConvertingVisitor extends SimpleTypeVisitor8<AnyType, Void> {

	@Override
	public AnyType visitTypeVariable(TypeVariable t, Void unused) {
		return TemplateType.parameter(t.toString());
	}

	@Override
	public AnyType visitPrimitive(PrimitiveType t, Void unused) {
		switch (t.getKind()) {
			case INT:
				return AnyType.integer();
			case BOOLEAN:
				return AnyType.string();
			default:
				throw new IllegalStateException("Unexpected value: " + t.getKind());
		}
	}

	@Override
	public TemplateType visitDeclared(DeclaredType type, Void unused) {
		String name = ((TypeElement) type.asElement()).getQualifiedName().toString();
		List<AnyType> parameters = type.getTypeArguments().stream().map(this::visit).collect(toList());
		return AnyType.template(name, parameters);
	}
}
