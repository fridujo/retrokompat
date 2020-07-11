package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.fridujo.retrokompat.tools.JarMaker;
import com.github.fridujo.retrokompat.tools.PathUtils;

class JarObjectTests {

    private final Path jarPath = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/two_classes"));

    @Test
    void listTypeNames_returns_qualified_names() {
        Set<String> classNames = new JarObject(jarPath).listTypeNames();

        assertThat(classNames)
            .containsExactlyInAnyOrder(
                "test.SampleClass1",
                "test.SampleClass2");
    }

    @Test
    void extractSignatures_returns_public_signatures() {
        Set<Signature> signatures = new JarObject(jarPath).extractSignatures();

        assertThat(signatures).hasSize(2);
    }

    @Nested
    class UsingMavenDependencies {

        private final Path opentest4jPath = getDependencyPath("opentest4j");
        private final Path junitJupiterApiPath = getDependencyPath("junit-jupiter-api");

        @Test
        void jarObject_throws_if_class_cannot_be_loaded() {
            assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new JarObject(junitJupiterApiPath).streamPublicTypes().collect(Collectors.toList()))
                .withMessageStartingWith("Cannot load class [")
                .withMessageEndingWith("] from JAR, maybe a classpath element is missing\n" +
                    "Consider using JarObject(Path jarPath, Set<Path> dependencyPaths) instead of JarObject(Path jarPath)");
        }

        @Test
        void jarObject_works_properly_with_according_classpath() {
            assertThat(new JarObject(opentest4jPath).streamPublicTypes().map(c -> c.getSimpleName()))
                .contains("AssertionFailedError");
        }

        private Path getDependencyPath(String artifactId) {
            return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .filter(p -> p.contains(artifactId))
                .map(Paths::get)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching path in classpath"));
        }
    }
}
