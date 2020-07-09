package com.github.fridujo.retrokompat.tools;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    public static Path forClassPath(String classPath) {
        try {
            return Paths.get(ClassLoader.getSystemResource(classPath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
