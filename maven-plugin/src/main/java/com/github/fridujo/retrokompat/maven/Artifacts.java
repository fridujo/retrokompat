package com.github.fridujo.retrokompat.maven;

import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;

interface Artifacts {

    static ArtifactCoordinate buildCoordinate(org.eclipse.aether.graph.Dependency d) {
        return buildCoordinate(
            d.getArtifact().getGroupId(),
            d.getArtifact().getArtifactId(),
            d.getArtifact().getVersion()
        );
    }

    static ArtifactCoordinate buildCoordinate(String groupId, String artifactId, String version) {
        DefaultArtifactCoordinate artifactCoordinate = new DefaultArtifactCoordinate();
        artifactCoordinate.setGroupId(groupId);
        artifactCoordinate.setArtifactId(artifactId);
        artifactCoordinate.setVersion(version);
        return artifactCoordinate;
    }
}
