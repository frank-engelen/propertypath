package de.codecentric.propertypath.processing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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

	for (Element e : elements) {
	    processingEnv.getMessager().printMessage(Kind.NOTE, "Processing :" + e.toString(), e);
	    try {
		handle(e);
	    } catch (IOException e1) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e1.getLocalizedMessage(), e);
	    }
	}

	return true;
    }

    private void handle(Element e) throws IOException {
	Filer filer = processingEnv.getFiler();

	List<? extends Element> enclosedElements = e.getEnclosedElements();
	if (!enclosedElements.isEmpty()) {
	    PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(e);
	    String propsClassNameSimple = getPropertyClassName(e);
	    JavaFileObject sourceFile = filer.createSourceFile(packageOf.getQualifiedName() + "." + propsClassNameSimple, e);
	    Writer writer = sourceFile.openWriter();
	    pw = new PrintWriter(writer);
	    println("package " + packageOf.getQualifiedName() + ";");
	    println();

	    final StringBuilder builderAttributes = new StringBuilder();
	    final StringBuilder builderConstructor = new StringBuilder();
	    final Set<String> importPath = new HashSet<String>();
	    importPath.add(PropertyPath.class.getName());

	    for (Element inner : enclosedElements) {

		final Property annotation = inner.getAnnotation(Property.class);
		if (annotation == null) {
		    continue;
		}

		final String simpleNameAttribute = inner.getSimpleName().toString();
		final TypeMirror innerType = inner.asType();
		final Element innerElement = processingEnv.getTypeUtils().asElement(innerType);

		String fqnPMType = PropertyPath.class.getName();
		String simpleNamePMType = PropertyPath.class.getSimpleName();
		String toAdd = ", " + innerType + ".class";
		if (innerElement != null && innerElement.getAnnotation(WithProperties.class) != null) {
		    simpleNamePMType = getPropertyClassName(innerElement);
		    PackageElement packageOfInner = processingEnv.getElementUtils().getPackageOf(innerElement);
		    fqnPMType = packageOfInner.getQualifiedName() + "." + simpleNamePMType;
		    importPath.add(fqnPMType);
		    toAdd = "";
		}

		builderAttributes.append("    public final " + simpleNamePMType + "<ORIGIN, " + innerType + "> " + simpleNameAttribute + ";\n");
		builderConstructor.append("        " + simpleNameAttribute + " = new " + simpleNamePMType + "<ORIGIN, " + innerType + ">(rootType, this, \""
			+ simpleNameAttribute + "\"" + toAdd + ");\n");
		importPath.add(fqnPMType);
	    }

	    for (String importLine : importPath) {
		println("import " + importLine + ";");
	    }

	    println();
	    println("public class " + propsClassNameSimple + "<ORIGIN,TARGET> extends PropertyPath<ORIGIN,TARGET> {");
	    println(builderAttributes.toString());

	    println("    public static " + propsClassNameSimple + "<" + e.getSimpleName() + ", " + e.getSimpleName() + ">  create() {");
	    println("        return new " + propsClassNameSimple + "<" + e.getSimpleName() + ", " + e.getSimpleName() + ">(" + e.getSimpleName()
		    + ".class, null, null);");
	    println("    }");

	    println("    public " + propsClassNameSimple + "(Class<ORIGIN> rootType, PropertyPath<ORIGIN,?> parent, String nameInParent) {");
	    println("        super(rootType, parent, nameInParent, " + e.getSimpleName() + ".class);");
	    println(builderConstructor.toString());
	    println("    }");

	    println("}");
	    pw.flush();

	    writer.flush();
	    writer.close();
	}
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
