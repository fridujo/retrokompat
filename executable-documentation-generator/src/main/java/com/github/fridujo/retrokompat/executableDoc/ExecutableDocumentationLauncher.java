package com.github.fridujo.retrokompat.executableDoc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import picocli.CommandLine;

public class ExecutableDocumentationLauncher {

    public static void main(String[] args) {
        Options options = new Options();
        new CommandLine(options).parseArgs(args);
        options = options.postProcess();

        createDestinationDirectoryIfNeeded(options);

        ExecutableDocumentationGenerator generator = new ExecutableDocumentationGenerator(options);

        Arrays.stream(options.documentationFiles)
            .peek(p -> {
                if (!Files.exists(p)) {
                    throw new IllegalArgumentException("File " + p + " not found");
                }
            }).forEach(generator::process);
    }

    private static void createDestinationDirectoryIfNeeded(Options options) {
        Path destination = options.destination;
        if (!Files.exists(destination)) {
            try {
                Files.createDirectories(destination);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
