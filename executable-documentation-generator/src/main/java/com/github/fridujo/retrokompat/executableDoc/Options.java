package com.github.fridujo.retrokompat.executableDoc;

import java.nio.file.Path;

import picocli.CommandLine;

class Options {
    @CommandLine.Option(names = {"-f", "--files"}, required = true, paramLabel = "FILES", description = "documentation files")
    Path[] documentationFiles;

    @CommandLine.Option(names = {"-d", "--destination"}, required = true, paramLabel = "FILE", description = "Path where to generate test classes")
    Path destination;

    @CommandLine.Option(names = {"-p", "--package"}, defaultValue  = "executableDoc", paramLabel = "STRING", description = "Package where to generate test classes")
    String destinationPackage = "executableDoc";

    Options postProcess() {
        for (int i = 0; i < documentationFiles.length; i++) {
            documentationFiles[i] = documentationFiles[i].toAbsolutePath();
        }
        destination = destination.toAbsolutePath();
        return this;
    }
}
