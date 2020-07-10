package com.github.fridujo.retrokompat.maven;

import java.nio.file.Path;
import java.util.Set;

class JarWithDependencies {

    final Path jarPath;
    final Set<Path> dependenciesPaths;

    JarWithDependencies(Path jarPath, Set<Path> dependenciesPaths) {
        this.jarPath = jarPath;
        this.dependenciesPaths = dependenciesPaths;
    }
}
