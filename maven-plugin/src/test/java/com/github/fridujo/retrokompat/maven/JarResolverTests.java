package com.github.fridujo.retrokompat.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.junit.jupiter.api.Test;

class JarResolverTests {

    private final ArtifactResolver artifactResolver = mock(ArtifactResolver.class, RETURNS_DEEP_STUBS);
    private final DependenciesResolver dependenciesResolver = mock(DependenciesResolver.class);

    private final JarResolver jarResolver = new JarResolver(artifactResolver, dependenciesResolver);

    @Test
    void resolveVersion_assemble_jar_and_dependencies() throws MojoExecutionException, ArtifactResolverException {
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        Path jarPath = Paths.get(UUID.randomUUID().toString());
        when(artifactResolver.resolveArtifact(any(), any(ArtifactCoordinate.class)).getArtifact().getFile().toPath()).thenReturn(jarPath);
        Set<Path> dependenciesPaths = IntStream.range(0, 3).mapToObj(i -> Paths.get(UUID.randomUUID().toString())).collect(Collectors.toSet());
        when(dependenciesResolver.getDependenciesOfArtifact(any(), any(Artifact.class))).thenReturn(
            dependenciesPaths
        );

        JarWithDependencies jarWithDependencies = jarResolver.resolveVersion("test", "test", "1.0.0", request);

        assertThat(jarWithDependencies.jarPath).isEqualTo(jarPath);
        assertThat(jarWithDependencies.dependenciesPaths).containsExactlyInAnyOrderElementsOf(dependenciesPaths);
    }

    @Test
    void resolveVersion_throws_when_artifactResolver_throws() throws ArtifactResolverException {
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        ArtifactResolverException artifactResolverException = new ArtifactResolverException("bla bla", null);
        when(artifactResolver.resolveArtifact(any(), any(ArtifactCoordinate.class))).thenThrow(artifactResolverException);

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> jarResolver.resolveVersion("test", "test", "1.0.0", request))
            .withMessage("Unable to fetch version test:test:jar:1.0.0")
            .withCause(artifactResolverException);
    }

    @Test
    void resolveVersion_throws_when_dependenciesResolver_throws() throws ArtifactResolverException, MojoExecutionException {
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
        when(artifactResolver.resolveArtifact(any(), any(ArtifactCoordinate.class)).getArtifact().getFile().toPath())
            .thenReturn(Paths.get("test"));
        String message = UUID.randomUUID().toString();
        when(dependenciesResolver.getDependenciesOfArtifact(any(), any(Artifact.class))).thenThrow(new MojoExecutionException(message));

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> jarResolver.resolveVersion("test", "test", "1.0.0", request))
            .withMessage(message);
    }
}
