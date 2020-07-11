package com.github.fridujo.retrokompat;

import static java.util.Collections.emptySet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
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

import com.github.fridujo.retrokompat.tools.Urls;

public class JarObject {

    public static final String CLASS_EXTENSION = ".class";
    private final Path jarPath;
    private final URLClassLoader childClassLoader;

    public JarObject(Path jarPath) {
        this(jarPath, emptySet());
    }

    public JarObject(Path jarPath, Set<Path> dependencyPaths) {
        this.jarPath = jarPath;

        URL[] urls = Stream.concat(
            Stream.of(jarPath),
            dependencyPaths.stream()
        )
            .map(p -> Urls.fromPath(p))
            .toArray(URL[]::new);

        this.childClassLoader = new URLClassLoader(urls, JarObject.class.getClassLoader());
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
                if (name.endsWith(CLASS_EXTENSION)
                    && !name.equals("module-info.class")) {
                    classNames.add(name.substring(0, name.length() - CLASS_EXTENSION.length()).replace('/', '.'));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return classNames;
    }

    public Set<Signature> extractSignatures() {
        return streamPublicTypes()
            .flatMap(c -> extractSignatures(c).stream())
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
                } catch (NoClassDefFoundError e) {
                    throw new RuntimeException("Cannot load class [" + className + "] from JAR, maybe a classpath element is missing\n" +
                        "Consider using JarObject(Path jarPath, Set<Path> dependencyPaths) instead of JarObject(Path jarPath)", e);
                }
            });
    }

    private Set<Signature> extractSignatures(Class<?> loadedClass) {
        Set<Signature> signatures = new LinkedHashSet<>();
        Arrays.stream(loadedClass.getMethods())
            .filter(m -> m.getDeclaringClass().getClassLoader() != null)
            .map(executable -> new Signature(loadedClass, executable))
            .forEach(signatures::add);

        Arrays.stream(loadedClass.getConstructors())
            .map(Signature::new)
            .forEach(signatures::add);
        return signatures;
    }
}
