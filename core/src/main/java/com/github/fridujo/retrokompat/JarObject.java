package com.github.fridujo.retrokompat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarObject {

    public static final String CLASS_EXTENSION = ".class";
    private final Path jarPath;
    private final URLClassLoader childClassLoader;

    public JarObject(Path jarPath) {
        this.jarPath = jarPath;
        try {
            this.childClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, JarObject.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> listTypeNames() {
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

    public Set<Signature> extractSignatures() {
        ClassLoader appClassLoader = Signature.class.getClassLoader();
        ClassLoader systemClassLoader = appClassLoader.getParent();

        return streamPublicTypes()
            .flatMap(c -> extractSignatures(c, systemClassLoader).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Stream<Class<?>> streamPublicTypes() {
        return streamTypes()
            .filter(c -> Modifier.isPublic(c.getModifiers()));
    }

    public Stream<Class<?>> streamTypes() {
        return listTypeNames().stream()
            .map(className -> {
                try {
                    return childClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private Set<Signature> extractSignatures(Class<?> loadedClass, ClassLoader systemClassLoader) {
        Set<Signature> signatures = new LinkedHashSet<>();
        Arrays.stream(loadedClass.getMethods())
            .filter(m -> m.getDeclaringClass().getClassLoader() != null)
            .map(Signature::new)
            .forEach(signatures::add);

        Arrays.stream(loadedClass.getConstructors())
            .map(Signature::new)
            .forEach(signatures::add);
        return signatures;
    }
}
