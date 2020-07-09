package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.fridujo.retrokompat.tools.JarMaker;
import com.github.fridujo.retrokompat.tools.PathUtils;

class JarObjectTests {

    private final Path jarPath = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/two_classes"));

    @Test
    void listClassNames_returns_qualified_names() {
        Set<String> classNames = new JarObject(jarPath).listClassNames();

        assertThat(classNames)
            .containsExactlyInAnyOrder(
                "test.SampleClass1",
                "test.SampleClass2");
    }

    @Test
    void extractSignatures_returns_public_signatures() {
        Set<Signature> signatures = new JarObject(jarPath).extractSignatures();

        assertThat(signatures.stream().map(s -> s.executable)).hasSize(2);
    }
}
