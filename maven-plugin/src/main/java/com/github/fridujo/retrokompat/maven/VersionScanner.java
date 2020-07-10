package com.github.fridujo.retrokompat.maven;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = VersionScanner.class)
class VersionScanner {

    @Requirement(role = org.apache.maven.artifact.metadata.ArtifactMetadataSource.class)
    private ArtifactMetadataSource artifactMetadataSource;

    Optional<String> getLastVersion(Artifact artifact, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) throws MojoExecutionException {
        ArtifactVersion currentComparableVersion = new DefaultArtifactVersion(artifact.getVersion());
        Artifact requestArtifact = buildRequestArtifact(artifact);

        List<ArtifactVersion> versions = listAvailableVersions(artifact, localRepository, remoteRepositories, currentComparableVersion, requestArtifact);

        if (versions.isEmpty()) {
            return Optional.empty();
        }

        // Sort so that the highest version is first
        versions.sort(Comparator.<ArtifactVersion>naturalOrder().reversed());
        return Optional.of(versions.get(0).toString());
    }

    private List<ArtifactVersion> listAvailableVersions(Artifact artifact, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories, ArtifactVersion currentComparableVersion, Artifact requestArtifact) throws MojoExecutionException {
        try {
            return artifactMetadataSource.retrieveAvailableVersions(requestArtifact, localRepository, remoteRepositories)
                .stream()
                .filter(v -> v.compareTo(currentComparableVersion) < 0)
                .collect(Collectors.toList());
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException("Unable to list versions of artifact: " + artifact, e);
        }
    }

    private Artifact buildRequestArtifact(Artifact artifact) {
        Artifact requestArtifact = ArtifactUtils.copyArtifact(artifact);
        if (isSnapshot(artifact.getVersion())) {
            requestArtifact.setVersion(toReleaseVersion(artifact.getVersion()));
        }
        return requestArtifact;
    }

    private boolean isSnapshot(String version) {
        return version.regionMatches(true, version.length() - Artifact.SNAPSHOT_VERSION.length(),
            Artifact.SNAPSHOT_VERSION, 0, Artifact.SNAPSHOT_VERSION.length());
    }

    private String toReleaseVersion(String version) {
        return version.substring(0, version.length() - Artifact.SNAPSHOT_VERSION.length() - 1);
    }
}
