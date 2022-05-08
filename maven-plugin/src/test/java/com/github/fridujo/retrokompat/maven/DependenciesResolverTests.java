package com.github.fridujo.retrokompat.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.fridujo.retrokompat.maven.tools.TestArtifacts.buildAetherDependency;
import static com.github.fridujo.retrokompat.maven.tools.TestArtifacts.buildArtifact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DependenciesResolverTests {

    private final ProjectBuilder projectBuilder = mock(ProjectBuilder.class, RETURNS_DEEP_STUBS);
    private final ArtifactResolver artifactResolver = mock(ArtifactResolver.class);

    private final DependenciesResolver dependenciesResolver = new DependenciesResolver(projectBuilder, artifactResolver);

    @Test
    void getDependenciesOfArtifact_build_it_to_retrieve_transitive_dependencies() throws MojoExecutionException, ProjectBuildingException, ArtifactResolverException {
        DefaultProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        Artifact artifact = buildArtifact("test", "test", "0.0.1-SNAPSHOT");
        List<Dependency> aetherDependencies = Arrays.asList(
            new Dependency(new DefaultArtifact("test:test:1"), "compile"),
            new Dependency(new DefaultArtifact("test:test:2"), "runtime"),
            new Dependency(new DefaultArtifact("test:test:3"), "provided"),
            new Dependency(new DefaultArtifact("test:test:4"), "test")
        );
        when(projectBuilder.build(artifact, buildingRequest).getDependencyResolutionResult().getDependencies()).thenReturn(aetherDependencies);
        when(artifactResolver.resolveArtifact(any(), any(ArtifactCoordinate.class))).thenAnswer(iom -> {
            ArtifactResult ar = mock(ArtifactResult.class, RETURNS_DEEP_STUBS);
            when(ar.getArtifact().getFile().toPath()).thenReturn(Paths.get("test" + iom.<ArtifactCoordinate>getArgument(1).getVersion()));
            return ar;
        });

        Set<Path> dependencies = dependenciesResolver.getDependenciesOfArtifact(buildingRequest, artifact);

        assertThat(dependencies).containsOnly(
            Paths.get("test1"),
            Paths.get("test2"),
            Paths.get("test3")
        );
    }

    @Test
    void getDependenciesOfArtifact_throws_if_projectBuilder_throws() throws ProjectBuildingException {
        DefaultProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        Artifact artifact = buildArtifact("test", "test", "0.0.1-SNAPSHOT");
        ProjectBuildingException projectBuildingException = new ProjectBuildingException(UUID.randomUUID().toString(), "test", new Exception());
        when(projectBuilder.build(artifact, buildingRequest)).thenThrow(projectBuildingException);

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> dependenciesResolver.getDependenciesOfArtifact(buildingRequest, artifact))
            .withMessage("Unable to build version test:test:pom:0.0.1-SNAPSHOT")
            .withCause(projectBuildingException);
    }

    @Test
    void getDependenciesOfArtifact_throws_if_artifactResolver_throws() throws ProjectBuildingException, ArtifactResolverException {
        DefaultProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        Artifact artifact = buildArtifact("test", "test", "0.0.1-SNAPSHOT");
        List<Dependency> aetherDependencies = Arrays.asList(
            buildAetherDependency("test:test:1", "compile")
        );
        when(projectBuilder.build(artifact, buildingRequest).getDependencyResolutionResult().getDependencies()).thenReturn(aetherDependencies);
        ArtifactResolverException artifactResolverException = new ArtifactResolverException(UUID.randomUUID().toString(), null);
        when(artifactResolver.resolveArtifact(any(), any(ArtifactCoordinate.class))).thenThrow(artifactResolverException);

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> dependenciesResolver.getDependenciesOfArtifact(buildingRequest, artifact))
            .withMessage("Unable to list dependencies of test:test:pom:0.0.1-SNAPSHOT")
            .withCause(artifactResolverException);
    }
}
