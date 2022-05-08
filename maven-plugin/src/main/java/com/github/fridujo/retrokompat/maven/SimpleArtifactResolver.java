package com.github.fridujo.retrokompat.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;

import java.util.function.Function;

class SimpleArtifactResolver {

    private final ArtifactResolver artifactResolver;

    SimpleArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    <T extends Exception> Artifact resolveArtifact(ArtifactCoordinate artifactCoordinate,
                                                   ProjectBuildingRequest buildingRequest,
                                                   Function<ArtifactResolverException, T> exceptionMapper
    ) throws T {
        try {
            return artifactResolver.resolveArtifact(buildingRequest, artifactCoordinate).getArtifact();
        } catch (ArtifactResolverException e) {
            throw exceptionMapper.apply(e);
        }
    }
}
