package com.github.fridujo.retrokompat.tools;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtils {

    public static void deleteFolder(Path path) {
        if (exists(path)) {
            try {
                walkFileTree(path, new DeleteAllVisitor());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static Stream<Path> streamFilesIn(Path folder) {
        try {
            return Files.walk(folder)
                .filter(Files::isRegularFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
