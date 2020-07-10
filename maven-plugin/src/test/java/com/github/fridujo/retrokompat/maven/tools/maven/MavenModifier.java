package com.github.fridujo.retrokompat.maven.tools.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;

public class MavenModifier {
    private final Path pomPath;
    private final MavenProject mavenProject;

    public MavenModifier(Path path) {
        pomPath = Files.isDirectory(path) ? path.resolve("pom.xml") : path;
        this.mavenProject = Maven.buildProject(pomPath);
    }

    public MavenProject getProject() {
        return mavenProject;
    }

    public void flush() {
        MavenXpp3Writer writer = new MavenXpp3Writer();

        try (OutputStream os = Files.newOutputStream(pomPath)) {
            writer.write(os, mavenProject.getModel());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public MavenModifier addPlugin(Plugin plugin) {
        mavenProject.getBuild().addPlugin(plugin);
        return this;
    }

    public MavenModifier changePluginVersion(String artifactId, String newVersion) {
        Plugin plugin = mavenProject.getBuild()
            .getPlugins()
            .stream()
            .filter(p -> artifactId.equals(p.getArtifactId()))
            .findFirst()
            .orElseGet(() -> mavenProject.getBuild()
                .getPluginManagement()
                .getPlugins()
                .stream()
                .filter(p -> artifactId.equals(p.getArtifactId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No plugin matching " + artifactId)));
        plugin.setVersion(newVersion);
        return this;
    }
}
