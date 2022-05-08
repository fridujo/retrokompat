package com.github.fridujo.retrokompat.maven.tools;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import static java.nio.file.FileVisitResult.CONTINUE;

public class GitExtension implements ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Path.class && parameterContext.isAnnotated(GitClone.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        GitClone annotation = parameterContext.getParameter().getAnnotation(GitClone.class);
        String rawUri = annotation.uri();
        URI uri;
        try {
            uri = URI.create(rawUri);
        } catch (IllegalArgumentException e) {
            uri = URI.create("ssh://" + rawUri);
        }

        String path = uri.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith(".git")) {
            path = path.substring(0, path.length() - ".git".length());
        }
        Path whereToClone = Paths.get("").resolve("target").resolve("git").resolve(path).toAbsolutePath();
        deleteDirectory(whereToClone);

        // Repository needs to be closed, otherwise .git directory cannot be deleted
        LOGGER.info("Cloning " + rawUri + " into " + whereToClone);
        try (Git git = Git.cloneRepository()
            .setURI(rawUri)
            .setDirectory(whereToClone.toFile())
            .call()) {
            String tag = annotation.tag();
            if (!GitClone.NO_VALUE.equals(tag)) {
                LOGGER.info("Switching to tag " + tag);
                git.checkout().setName("refs/tags/" + tag).call();
            }
        } catch (GitAPIException | JGitInternalException e) {
            throw new ParameterResolutionException("Unable to clone given URI: " + rawUri + " into " + whereToClone, e);
        }
        return whereToClone;
    }

    private void deleteDirectory(Path path) {
        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        return deleteAndContinue(file);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return deleteAndContinue(dir);
                    }

                    private FileVisitResult deleteAndContinue(Path path) throws IOException {
                        path.toFile().setWritable(true);
                        Files.delete(path);
                        return CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
