package com.github.fridujo.retrokompat.executableDoc;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

class ExecutableDocumentationGenerator {
    
    private final Options options;

    ExecutableDocumentationGenerator(Options options) {
        this.options = options;
    }

    void process(Path path) {
        System.out.println("Processing " + path);

        Node document = parseMarkdown(path);

        ExecutableDocumentationVisitor visitor = new ExecutableDocumentationVisitor(options.destination, options.destinationPackage);
        document.accept(visitor);
        visitor.finalizeRecorded(0);
    }

    private Node parseMarkdown(Path path) {
        Parser parser = Parser.builder().build();
        try (Reader reader = Files.newBufferedReader(path)) {
            return parser.parseReader(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
