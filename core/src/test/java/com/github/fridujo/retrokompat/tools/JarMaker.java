package com.github.fridujo.retrokompat.tools;

import com.github.fridujo.markdown.junit.engine.support.Sources;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class JarMaker {

    public static Path compileAndPackage(String jarName, Iterable<String> sourceContents) {
        Path targetDirectory = Paths.get("").resolve("target");
        Path containingFolder = targetDirectory.resolve("generated-classes").resolve(jarName);
        if (Files.exists(containingFolder)) {
            FileUtils.deleteFolder(containingFolder);
        }
        try {
            Files.createDirectories(containingFolder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        sourceContents.forEach(s -> writeSourceToFile(s, containingFolder));

        Path outputPath = compile(containingFolder);

        return createJarFile(outputPath);
    }

    private static void writeSourceToFile(String sourceContent, Path containingFolder) {
        String className = Sources.extractName(sourceContent).orElseThrow(() -> new IllegalArgumentException("Could not extract type name"));
        Path classFile = containingFolder.resolve(className + ".java");

        try {
            Files.createFile(classFile);
            Files.write(classFile, sourceContent.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path compileAndPackage(Path sourceFolderPath) {
        Path outputPath = compile(sourceFolderPath);

        return createJarFile(outputPath);
    }

    public static Path compile(Path sourceFolderPath) {
        Path outputPath = Paths.get("")
            .resolve("target")
            .resolve("compiled")
            .resolve(sourceFolderPath.getFileName())
            .toAbsolutePath();
        FileUtils.deleteFolder(outputPath);
        FileUtils.createFolder(outputPath);

        Set<File> javaFilePaths = FileUtils.streamFilesIn(sourceFolderPath)
            .filter(p -> p.toString().endsWith(".java"))
            .map(Path::toFile)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromFiles(javaFilePaths);
        List<String> options = Arrays.asList("-d", outputPath.toString());
        if (!compiler.getTask(null, fileManager, d -> System.out.println(d), options, null, javaFileObjects)
            .call()) {
            throw new IllegalStateException("Compilation error");
        }
        return outputPath;
    }

    public static Path createJarFile(Path outputPath) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Path jarPath = outputPath.getParent().resolve(outputPath.getFileName() + ".jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()), manifest)) {
            FileUtils.streamFilesIn(outputPath)
                .filter(p -> p.toString().endsWith(".class"))
                .map(Path::toFile)
                .forEach(f -> {
                    String entryName = f.getPath().substring(outputPath.toFile().getPath().length() + 1).replace("\\", "/");
                    JarEntry entry = new JarEntry(entryName);
                    entry.setTime(f.lastModified());
                    try {
                        jos.putNextEntry(entry);

                        try (FileInputStream fin = new FileInputStream(f);
                             BufferedInputStream in = new BufferedInputStream(fin)) {
                            byte[] buffer = new byte[1024];
                            for (; ; ) {
                                int count = in.read(buffer);
                                if (count == -1) {
                                    break;
                                }
                                jos.write(buffer, 0, count);
                            }
                        }
                        jos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return jarPath;
    }


}
