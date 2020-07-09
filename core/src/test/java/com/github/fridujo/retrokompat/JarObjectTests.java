package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.fridujo.retrokompat.tools.JarMaker;
import com.github.fridujo.retrokompat.tools.PathUtils;

class JarObjectTests {

    @Test
    void listClassNames_returns_qualified_names() {
        Path jarPath = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/two_classes"));

        Set<String> classNames = new JarObject(jarPath).listClassNames();

        assertThat(classNames)
            .containsExactlyInAnyOrder(
                "test.SampleClass1",
                "test.SampleClass2");
    }
}
