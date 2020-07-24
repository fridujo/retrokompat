package com.github.fridujo.retrokompat.tools;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class PathUtils {

    public static Path forClassPath(String classPath) {
        try {
            return Paths.get(ClassLoader.getSystemResource(classPath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getDependencyPath(String artifactId) {
        return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
            .filter(p -> p.contains(artifactId))
            .map(Paths::get)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No matching path in classpath"));
    }
}
