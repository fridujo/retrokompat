package com.github.fridujo.retrokompat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarObject {

    public static final String CLASS_EXTENSION = ".class";
    private final Path jarPath;

    public JarObject(Path jarPath) {
        this.jarPath = jarPath;
    }

    public Set<String> listClassNames() {
        Set<String> classNames = new LinkedHashSet<>();
        try (ZipInputStream zin = new ZipInputStream(jarPath.toUri().toURL().openStream())) {
            for (; ; ) {
                ZipEntry entry = zin.getNextEntry();
                if (entry == null) {
                    break;
                }
                String name = entry.getName();
                if (name.endsWith(CLASS_EXTENSION)) {
                    classNames.add(name.substring(0, name.length() - CLASS_EXTENSION.length()).replace('/', '.'));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return classNames;
    }
}
