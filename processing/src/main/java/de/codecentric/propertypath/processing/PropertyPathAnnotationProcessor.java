package de.codecentric.propertypath.processing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.PropertyPath;
import de.codecentric.propertypath.api.WithProperties;

@SupportedAnnotationTypes("de.codecentric.propertypath.api.WithProperties")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PropertyPathAnnotationProcessor extends AbstractProcessor {

	private PrintWriter pw;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(WithProperties.class);

		if (elements.size() > 0) {
			processingEnv.getMessager().printMessage(Kind.NOTE, "Start processing batch of " + elements.size() + " elements");
			long start = System.currentTimeMillis();
			for (Element currentElement : elements) {
				processingEnv.getMessager().printMessage(Kind.NOTE, "  Processing :" + currentElement.toString(), currentElement);
				try {
					handle(currentElement);
				} catch (IOException e1) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e1.getLocalizedMessage(), currentElement);
				}
			}
			long end = System.currentTimeMillis();
			processingEnv.getMessager().printMessage(Kind.NOTE,
					"Done processing batch of " + elements.size() + " elements in " + (end - start) + "ms");
		}
		return true;
	}

	private void handle(Element currentElement) throws IOException {
		final String propsClassNameSimple = getPropertyClassName(currentElement);

		final StringBuilder builderAttributes = new StringBuilder();
		final StringBuilder builderConstructorImpl = new StringBuilder();

		final List<? extends Element> enclosedElementsOfCurrent = currentElement.getEnclosedElements();
		for (Element propertyElement : enclosedElementsOfCurrent) {

			final Property annotation = propertyElement.getAnnotation(Property.class);
			if (annotation == null) {
				continue;
			}

			final String nameOfProperty = getNameOfProperty(propertyElement);
			final TypeMirror typeOfProperty = getTypeOfProperty(propertyElement);
			final String fqnPMType = getFqnPropertyPathClassForTypeOfProperty(typeOfProperty);

			// Generate Declaration of a Property-Path-Attribute
			builderAttributes.append("    public final " + fqnPMType + "<ORIGIN, " + typeOfProperty + "> " + nameOfProperty + ";\n");

			// Generate Part of Constructor-Implementation
			final Name simpleNameCurrentElement = currentElement.getSimpleName();
			builderConstructorImpl.append("        " + nameOfProperty + " = new " + fqnPMType + "<ORIGIN, " + typeOfProperty + ">(rootType, this, \""
					+ nameOfProperty + "\", " + getWithoutTypeParameter(typeOfProperty) + ".class, " + simpleNameCurrentElement + ".class);\n");
		}

		printPropertiesSourceFile(currentElement, propsClassNameSimple, builderAttributes, builderConstructorImpl);
	}

	private void printPropertiesSourceFile(Element currentElement, String propsClassNameSimple, final StringBuilder builderAttributes,
			final StringBuilder builderConstructorImpl) throws IOException {
		final Filer filer = processingEnv.getFiler();
		final Name simpleNameCurrentElement = currentElement.getSimpleName();

		final PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(currentElement);
		final JavaFileObject sourceFile;
		try {
			sourceFile = filer.createSourceFile(packageOf.getQualifiedName() + "." + propsClassNameSimple, currentElement);
		} catch (Exception e) {
			processingEnv.getMessager().printMessage(Kind.NOTE, "Exception :" + e, currentElement);
			return;
		}
		final Writer writer = sourceFile.openWriter();
		pw = new PrintWriter(writer);

		// Start of File
		println("package " + packageOf.getQualifiedName() + ";");
		println();

		// Start of class declaration
		println("public class " + propsClassNameSimple + "<ORIGIN,TARGET> extends " + getSuperClass(currentElement) + " {");
		println();
		println("    private static final long serialVersionUID = 1L;");
		println();
		println(builderAttributes.toString());

		// Factory-Method "newXYProperties"
		final String starttype = propsClassNameSimple + "<" + simpleNameCurrentElement + ", " + simpleNameCurrentElement + ">";
		println("    public static " + starttype + " new" + propsClassNameSimple + "() {");
		println("        return new " + starttype + "(" + simpleNameCurrentElement + ".class, null, null, " + simpleNameCurrentElement
				+ ".class, null);");
		println("    }");
		println();

		// Constructor
		println("    public " + propsClassNameSimple + "(Class<ORIGIN> rootType, " + PropertyPath.class.getName()
				+ "<ORIGIN,?> parent, String nameInParent, Class<?> typeInParent, Class<?> declaredIn) {");
		println("        super(rootType, parent, nameInParent, typeInParent == null ? " + simpleNameCurrentElement
				+ ".class : typeInParent, declaredIn);");
		println();
		println(builderConstructorImpl.toString());
		println("    }");

		// End-of-class
		println("}");
		pw.flush();
		writer.flush();
		writer.close();
	}

	private String getFqnPropertyPathClassForTypeOfProperty(final TypeMirror typeOfProperty) {
		final Element elementOfTypeOfProperty = processingEnv.getTypeUtils().asElement(typeOfProperty);
		String fqnPMType = PropertyPath.class.getName();
		if (elementOfTypeOfProperty != null && elementOfTypeOfProperty.getAnnotation(WithProperties.class) != null) {
			final String simpleNamePMType = getPropertyClassName(elementOfTypeOfProperty);
			final PackageElement packageOfInner = processingEnv.getElementUtils().getPackageOf(elementOfTypeOfProperty);
			fqnPMType = packageOfInner.getQualifiedName() + "." + simpleNamePMType;
		}
		return fqnPMType;
	}

	private String getWithoutTypeParameter(TypeMirror innerType) {
		String innerTypeName = "" + innerType;
		final int ch = innerTypeName.indexOf("<");
		if (ch == -1) {
			return innerTypeName;
		}
		return innerTypeName.substring(0, ch);
	}

	protected TypeMirror getTypeOfProperty(Element inner) {
		TypeMirror result;
		if (inner instanceof ExecutableElement) {
			result = ((ExecutableElement) inner).getReturnType();
		} else {
			result = inner.asType();
		}

		if (result.getKind().isPrimitive()) {
			String primName = "" + result;
			final String typename;
			if (primName.equals("int")) {
				typename = "java.lang.Integer";
			} else {
				typename = "java.lang." + primName.substring(0, 1).toUpperCase() + primName.substring(1);
			}
			TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(typename);
			result = typeElement.asType();
		}
		return result;
	}

	protected String getNameOfProperty(Element inner) {
		final String name = inner.getSimpleName().toString();
		if (name.startsWith("get")) {
			return transformGetterName2PropertyName(name);
		}
		return name;
	}

	protected String transformGetterName2PropertyName(String name) {
		return name.substring(3, 4).toLowerCase() + name.substring(4);
	}

	private String getSuperClass(Element e) {

		final Types typeUtils = processingEnv.getTypeUtils();

		final List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(getTypeOfProperty(e));
		for (TypeMirror superType : supertypes) {
			if (superType.toString().equals("java.lang.Object")) {
				continue;
			}

			final Element superElement = typeUtils.asElement(superType);
			if (superElement.getAnnotation(WithProperties.class) != null) {
				PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(superElement);
				final String packageName = packageOf == null ? "" : packageOf.getQualifiedName() + ".";
				return packageName + getPropertyClassName(superElement) + "<ORIGIN, TARGET>";
			}
		}

		for (TypeMirror superType : supertypes) {
			if (superType.toString().equals("java.lang.Object")) {
				continue;
			}

			final Element superElement = typeUtils.asElement(superType);
			if (superElement.getKind() == ElementKind.CLASS) {
				return getSuperClass(superElement);
			}
		}

		return PropertyPath.class.getName() + "<ORIGIN,TARGET>";
	}

	private String getPropertyClassName(Element e) {
		return e.getSimpleName() + "Properties";
	}

	public void println() {
		pw.print('\n');
	}

	public void println(String template, String... args) {
		final String output = String.format(template, (Object[]) args);
		pw.print(output);
		pw.print('\n');
	}

}
