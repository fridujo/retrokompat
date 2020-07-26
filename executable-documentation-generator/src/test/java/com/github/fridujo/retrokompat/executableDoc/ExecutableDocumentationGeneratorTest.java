package com.github.fridujo.retrokompat.executableDoc;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExecutableDocumentationGeneratorTest {

    @Test
    void generate_based_on_md(@TempDir Path tempDir) {
        Path mardownFile = getClassPath("test.md");
        Options options = new Options();
        options.destination = tempDir;
        new ExecutableDocumentationGenerator(options).process(mardownFile);
        System.out.println("Writing to " + tempDir);
    }

    private Path getClassPath(String fileName) {
        try {
            return Paths.get(ExecutableDocumentationGeneratorTest.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
