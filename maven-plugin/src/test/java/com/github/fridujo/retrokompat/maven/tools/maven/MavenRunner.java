package com.github.fridujo.retrokompat.maven.tools.maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MavenRunner {

    public static void installCurrentProject() {
        Path currentDirectory = Paths.get("").toAbsolutePath();
        Path topLevelMavenDirectory = findTopLevelMavenDirectory(currentDirectory);
        try (CloseableVerifier verifier = new CloseableVerifier(topLevelMavenDirectory)) {
            verifier.execute(List.of("-DskipTests"), "install");
        }
    }

    public static Path findTopLevelMavenDirectory(Path mavenProjectDirectory) {
        if (Files.exists(mavenProjectDirectory.getParent().resolve("pom.xml"))) {
            return findTopLevelMavenDirectory(mavenProjectDirectory.getParent());
        } else {
            return mavenProjectDirectory;
        }
    }
}
