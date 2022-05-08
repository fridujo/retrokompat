package com.github.fridujo.retrokompat.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.codehaus.plexus.component.annotations.Component;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Set;

@Component(role = JarResolver.class)
class JarResolver extends SimpleArtifactResolver {

    private final DependenciesResolver dependenciesResolver;

    @Inject
    JarResolver(ArtifactResolver artifactResolver, DependenciesResolver dependenciesResolver) {
        super(artifactResolver);
        this.dependenciesResolver = dependenciesResolver;
    }

    JarWithDependencies resolveVersion(String groupId, String artifactId, String version, ProjectBuildingRequest buildingRequestTemplate) throws MojoExecutionException {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(buildingRequestTemplate);

        ArtifactCoordinate artifactCoordinate = Artifacts.buildCoordinate(groupId, artifactId, version);
        Artifact artifact = resolveArtifact(
            artifactCoordinate,
            buildingRequest,
            e -> new MojoExecutionException("Unable to fetch version " + artifactCoordinate, e));

        Path jarPath = artifact.getFile().toPath();
        Set<Path> dependenciesPaths = dependenciesResolver.getDependenciesOfArtifact(buildingRequest, artifact);

        return new JarWithDependencies(jarPath, dependenciesPaths);
    }
}
