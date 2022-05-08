package com.github.fridujo.retrokompat.maven;

import com.github.fridujo.retrokompat.CompatibilityChecker;
import com.github.fridujo.retrokompat.maven.tools.ReflectionUtils;
import com.github.fridujo.retrokompat.maven.tools.maven.RecordedLog;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RetrokompatMojoTests {

    private final MavenProject project = mock(MavenProject.class, RETURNS_DEEP_STUBS);
    private final MavenSession mavenSession = mock(MavenSession.class);
    private final ArtifactRepository localRepository = mock(ArtifactRepository.class);
    private final RecordedLog log = new RecordedLog();

    private final VersionScanner versionScanner = mock(VersionScanner.class);
    private final JarResolver jarResolver = mock(JarResolver.class);
    private final DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);
    private final CompatibilityChecker compatibilityChecker = mock(CompatibilityChecker.class);

    private final RetrokompatMojo mojo = new RetrokompatMojo(versionScanner, jarResolver, dependenciesResolver);

    RetrokompatMojoTests() {
        ReflectionUtils.setField(mojo, "project", project);
        ReflectionUtils.setField(mojo, "mavenSession", mavenSession);
        ReflectionUtils.setField(mojo, "localRepository", localRepository);
        ReflectionUtils.setField(mojo, "remoteRepositories", emptyList());
        ReflectionUtils.setField(mojo, "compatibilityChecker", compatibilityChecker);

        mojo.setLog(log);
    }

    @Test
    void execution_is_skipped_if_non_jar_type() throws MojoFailureException, MojoExecutionException {
        when(project.getArtifact().getType()).thenReturn("pom");

        mojo.execute();

        verify(versionScanner, never()).getLastVersion(any(), any(), any());
        assertThat(log.getInfos()).contains("Skipping non-jar project (pom)");
    }

    @Test
    void execution_fails_if_no_jar_file_is_available() {
        when(project.getArtifact().getType()).thenReturn("jar");
        when(project.getBuild().getDirectory()).thenReturn("test");
        when(project.getBuild().getFinalName()).thenReturn("test");

        assertThatExceptionOfType(MojoFailureException.class)
            .isThrownBy(() -> mojo.execute())
            .withMessage("Missing jar file, please execute this plugin after the packaging phase");
    }

    @Test
    void execution_is_skipped_if_no_previous_version(@TempDir Path tempDir) throws IOException, MojoExecutionException, MojoFailureException {
        String jarFinalName = UUID.randomUUID().toString();
        Files.createFile(tempDir.resolve(jarFinalName + ".jar"));
        when(project.getArtifact().getType()).thenReturn("jar");
        when(project.getBuild().getDirectory()).thenReturn(tempDir.toString());
        when(project.getBuild().getFinalName()).thenReturn(jarFinalName);
        when(localRepository.toString()).thenReturn("mock of localRepository");

        when(versionScanner.getLastVersion(any(), any(), any())).thenReturn(Optional.empty());

        mojo.execute();

        verify(jarResolver, never()).resolveVersion(any(), any(), any(), any());
        assertThat(log.getWarns()).contains("No previous versions found from:\n\tmock of localRepository");
    }

    @Test
    void execution_fails_properly_if_compatibilityChecker_throws_unchecked(@TempDir Path tempDir) throws IOException, MojoExecutionException, MojoFailureException {
        String jarFinalName = UUID.randomUUID().toString();
        Files.createFile(tempDir.resolve(jarFinalName + ".jar"));
        when(project.getArtifact().getType()).thenReturn("jar");
        when(project.getBuild().getDirectory()).thenReturn(tempDir.toString());
        when(project.getBuild().getFinalName()).thenReturn(jarFinalName);
        when(localRepository.toString()).thenReturn("mock of localRepository");

        when(versionScanner.getLastVersion(any(), any(), any())).thenReturn(Optional.of("4.3.2.1"));
        when(jarResolver.resolveVersion(any(), any(), any(), any())).thenReturn(new JarWithDependencies(null, null));

        RuntimeException prankException = new RuntimeException("Bazinga! " + UUID.randomUUID());
        when(compatibilityChecker.check(any(), any(), any(), any())).thenThrow(prankException);

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> mojo.execute())
            .withMessageContaining("Unable to check dependencies")
            .withMessageContaining(prankException.getClass().getSimpleName())
            .withMessageContaining(prankException.getMessage())
            .withCause(prankException);

        assertThat(log.getInfos()).contains("Checking backward compatibility against version 4.3.2.1");
    }
}
