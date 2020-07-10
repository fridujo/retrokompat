package com.github.fridujo.retrokompat.maven;

import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;

class UncheckedArtifactResolverException extends RuntimeException {

    final ArtifactResolverException cause;

    UncheckedArtifactResolverException(ArtifactResolverException cause) {
        this.cause = cause;
    }
}
