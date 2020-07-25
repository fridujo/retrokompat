package com.github.fridujo.retrokompat.executableDoc;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Text;

class ExecutableDocumentationVisitor extends AbstractVisitor {

    private final Deque<String> headings;
    private final List<MethodSpec> testMethods = new ArrayList<>();
    private final List<String> codeSamples = new ArrayList<>();
    private final Path destination;
    private final String destinationPackage;
    private int previousHeadingLevel = 0;
    private int methodBuildingLevel = 0;
    private Boolean compatible;
    private String errorMessage;

    public ExecutableDocumentationVisitor(Path destination, String destinationPackage) {
        this.destination = destination;
        this.destinationPackage = destinationPackage;
        headings = new ArrayDeque<>();
    }

    @Override
    public void visit(Heading heading) {
        int level = heading.getLevel();
        String headingText = ((Text) heading.getFirstChild()).getLiteral();
        if (level > previousHeadingLevel) {
            headings.push(headingText);
        } else {
            finalizeRecorded(level);
            headings.pop();
            if (level == previousHeadingLevel) {
                headings.push(headingText);
            }
        }
        previousHeadingLevel = level;
    }

    void finalizeRecorded(int level) {
        boolean shouldBuildMethod = compatible != null && codeSamples.size() == 2;
        if (shouldBuildMethod) {
            ClassName jarMakerClass = ClassName.get("com.github.fridujo.retrokompat.tools", "JarMaker");

            ClassName compatibilityErrorClass = ClassName.get("com.github.fridujo.retrokompat", "CompatibilityError");
            ClassName compatibilityCheckerClass = ClassName.get("com.github.fridujo.retrokompat", "CompatibilityChecker");

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(toMethodName(headings.getFirst()))
                .addAnnotation(ClassName.get("org.junit.jupiter.api", "Test"))
                .returns(void.class)
                .addCode("$T v1 = $T.compileAndPackage(\n$S\n, $S);\n", Path.class, jarMakerClass, codeSamples.get(1), "v1")
                .addCode("$T v2 = $T.compileAndPackage(\n$S\n, $S);\n", Path.class, jarMakerClass, codeSamples.get(0), "v2")
                .addCode("\n")
                .addCode("$T<$T> errors = new $T().check(v1, v2);\n", Set.class, compatibilityErrorClass, compatibilityCheckerClass)
                .addCode("\n");
            if (compatible == null) {
                throw new IllegalStateException("No compatibility indication is given for " + toBreadcrumbs(headings));
            }
            if (compatible) {
                methodBuilder.addCode("assertThat(errors).isEmpty();");
            } else if (errorMessage != null) {
                methodBuilder.addCode("assertThat(errors.stream().map(Object::toString))\n    .containsExactly($S);", errorMessage);
            } else {
                throw new IllegalStateException("No error message is supplied for not-compatible " + toBreadcrumbs(headings));
            }
            testMethods.add(methodBuilder.build());

            codeSamples.clear();
            compatible = null;
        }

        if (methodBuildingLevel != 0 && level < methodBuildingLevel - 1 && !testMethods.isEmpty()) {
            String typeName = toTypeName(headings.getFirst()) + "Test";
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName);
            testMethods.forEach(classBuilder::addMethod);
            JavaFile javaFile = JavaFile.builder(destinationPackage, classBuilder.build())
                .addStaticImport(ClassName.get("org.assertj.core.api", "Assertions"), "assertThat")
                .build();
            System.out.println("Generating " + destination + File.separatorChar + typeName + ".java");
            try {
                javaFile.writeTo(destination);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            testMethods.clear();
        }

        if (shouldBuildMethod) {
            this.methodBuildingLevel = level;
        }
    }

    private String toBreadcrumbs(Deque<String> headings) {
        List<String> sortableCopy = new ArrayList<>(headings);
        Collections.reverse(sortableCopy);
        return sortableCopy.stream().collect(Collectors.joining(" > "));
    }

    @Override
    public void visit(Text text) {
        String literal = text.getLiteral().toLowerCase();
        if (literal.contains("not compatible")) {
            compatible = false;
        } else if (literal.contains("compatible")) {
            compatible = true;
        }

        if (literal.contains("error") && text.getNext() instanceof Code) {
            errorMessage = ((Code) text.getNext()).getLiteral();
        }
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        codeSamples.add(fencedCodeBlock.getLiteral().trim());
    }

    private String toMethodName(String heading) {
        return decapitilize(toTypeName(heading));
    }

    private String toTypeName(String heading) {
        return Arrays.stream(heading.split("\\s")).map(String::toLowerCase).map(w -> capitilize(w)).collect(Collectors.joining());
    }

    private String capitilize(String w) {
        return Character.isLowerCase(w.charAt(0)) ? (Character.toUpperCase(w.charAt(0)) + w.substring(1)) : w;
    }

    private String decapitilize(String w) {
        return Character.isLowerCase(w.charAt(0)) ? w : (Character.toLowerCase(w.charAt(0)) + w.substring(1));
    }
}
