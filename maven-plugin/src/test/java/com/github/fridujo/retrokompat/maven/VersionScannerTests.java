package com.github.fridujo.retrokompat.maven;

import static com.github.fridujo.retrokompat.maven.tools.TestArtifacts.buildArtifact;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.github.fridujo.retrokompat.maven.tools.ReflectionUtils;

class VersionScannerTests {

    private final ArtifactMetadataSource artifactMetadataSource = mock(ArtifactMetadataSource.class);
    private final ArtifactRepository localArtifactRepository = mock(ArtifactRepository.class);
    private final List<ArtifactRepository> remoteArtifactRepositories = List.of(mock(ArtifactRepository.class));

    private final VersionScanner versionScanner = new VersionScanner();

    private static List<ArtifactVersion> versions(String... versions) {
        List<ArtifactVersion> versionList = Arrays.stream(versions).map(DefaultArtifactVersion::new).collect(toList());
        Collections.shuffle(versionList);
        return versionList;
    }

    @BeforeEach
    void setUp() {
        ReflectionUtils.setField(versionScanner, "artifactMetadataSource", artifactMetadataSource);
    }

    @Test
    void getLastVersion_returns_empty_if_no_version_is_available() throws ArtifactMetadataRetrievalException, MojoExecutionException {
        when(artifactMetadataSource.retrieveAvailableVersions(any(), eq(localArtifactRepository), eq(remoteArtifactRepositories)))
            .thenReturn(emptyList());
        Artifact artifact = buildArtifact("test", "test", "0.0.1-SNAPSHOT");

        Optional<String> lastVersion = versionScanner.getLastVersion(artifact, localArtifactRepository, remoteArtifactRepositories);

        assertThat(lastVersion).isEmpty();
    }

    @Test
    void getLastVersion_returns_empty_if_no_version_prior_to_the_one_given_is_available() throws ArtifactMetadataRetrievalException, MojoExecutionException {
        when(artifactMetadataSource.retrieveAvailableVersions(any(), eq(localArtifactRepository), eq(remoteArtifactRepositories)))
            .thenReturn(versions("1.3.1", "1.3.2", "1.4", "2.0.0"));
        Artifact artifact = buildArtifact("test", "test", "1.3.0");

        Optional<String> lastVersion = versionScanner.getLastVersion(artifact, localArtifactRepository, remoteArtifactRepositories);

        assertThat(lastVersion).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "1.1.0-SNAPSHOT, 1.0",
        "1.2.9-SNAPSHOT, 1.2.8",
        "1.2.9, 1.2.8",
        "1.3.0-SNAPSHOT, 1.2.9",
        "1.3.0, 1.2.9"
    })
    void getLastVersion_returns_the_highest_version_prior_to_the_one_given(String currentVersion, String expectedAsLast) throws ArtifactMetadataRetrievalException, MojoExecutionException {
        when(artifactMetadataSource.retrieveAvailableVersions(any(), eq(localArtifactRepository), eq(remoteArtifactRepositories)))
            .thenReturn(versions("1.0", "1.2.8", "1.2.9", "1.3.0", "1.3.1", "1.3.2", "1.4", "2.0.0"));
        Artifact artifact = buildArtifact("test", "test", currentVersion);

        Optional<String> lastVersion = versionScanner.getLastVersion(artifact, localArtifactRepository, remoteArtifactRepositories);

        assertThat(lastVersion).contains(expectedAsLast);
    }

    @ParameterizedTest
    @CsvSource({
        "1.0.0, 1.0.0",
        "3.4.7-SNAPSHOT, 3.4.7",
    })
    void artifactMetadataSource_is_queried_by_removing_the_SNAPSHOT_suffix(String currentVersion, String resolutionVersion) throws ArtifactMetadataRetrievalException, MojoExecutionException {
        when(artifactMetadataSource.retrieveAvailableVersions(any(), eq(localArtifactRepository), eq(remoteArtifactRepositories)))
            .thenReturn(emptyList());
        Artifact artifact = buildArtifact("test", "test", currentVersion);

        versionScanner.getLastVersion(artifact, localArtifactRepository, remoteArtifactRepositories);

        ArgumentCaptor<Artifact> artifactArgumentCaptor = ArgumentCaptor.forClass(Artifact.class);
        verify(artifactMetadataSource).retrieveAvailableVersions(artifactArgumentCaptor.capture(), any(), any());
        assertThat(artifactArgumentCaptor.getValue().getVersion()).isEqualTo(resolutionVersion);
    }

    @Test
    void getLastVersion_throws_when_resolution_fails() throws ArtifactMetadataRetrievalException {
        when(artifactMetadataSource.retrieveAvailableVersions(any(), eq(localArtifactRepository), eq(remoteArtifactRepositories)))
            .thenThrow(new ArtifactMetadataRetrievalException("test", null, null));
        Artifact artifact = buildArtifact("test", "test", "1.0.0");

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> versionScanner.getLastVersion(artifact, localArtifactRepository, remoteArtifactRepositories))
            .withMessage("Unable to list versions of artifact: test:test:pom:1.0.0")
            .withCauseExactlyInstanceOf(ArtifactMetadataRetrievalException.class);
    }
}
