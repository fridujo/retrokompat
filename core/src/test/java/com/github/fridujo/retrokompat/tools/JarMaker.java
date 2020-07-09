package com.github.fridujo.retrokompat.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JarMaker {

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

        Set<Path> javaFilePaths = FileUtils.streamFilesIn(sourceFolderPath)
            .filter(p -> p.toString().endsWith(".java"))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromPaths(javaFilePaths);
        List<String> options = List.of("-d", outputPath.toString());
        compiler.getTask(null, fileManager, null, options, null, javaFileObjects)
            .call();
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
