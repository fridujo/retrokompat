package com.github.fridujo.retrokompat.tools;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;

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

    public static void createFolder(Path path) {
        path.toFile().mkdirs();
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
