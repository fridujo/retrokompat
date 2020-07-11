package com.github.fridujo.retrokompat.tools;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class Urls {

    public static URL fromPath(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
