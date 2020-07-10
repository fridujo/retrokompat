package com.github.fridujo.retrokompat.maven;

import static com.github.fridujo.retrokompat.maven.tools.maven.MavenBuilder.buildPlugin;
import static com.github.fridujo.retrokompat.maven.tools.maven.MavenBuilder.buildPluginExecutionForGoal;

import java.nio.file.Path;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.fridujo.retrokompat.maven.tools.GitClone;
import com.github.fridujo.retrokompat.maven.tools.GitExtension;
import com.github.fridujo.retrokompat.maven.tools.maven.CloseableVerifier;
import com.github.fridujo.retrokompat.maven.tools.maven.MavenExtension;
import com.github.fridujo.retrokompat.maven.tools.maven.MavenModifier;
import com.github.fridujo.retrokompat.maven.tools.maven.MavenRunner;

@ExtendWith({GitExtension.class, MavenExtension.class})
class RetrokompatMavenPluginE2ETests {

    private final MavenProject currentMavenProject;

    RetrokompatMavenPluginE2ETests(MavenProject currentMavenProject) {
        this.currentMavenProject = currentMavenProject;
    }

    @BeforeAll
    static void beforeAll() {
        MavenRunner.installCurrentProject();
    }

    @Test
    void backward_compatible_project(@GitClone(
        uri = "https://github.com/apache/maven-dependency-analyzer.git",
        tag = "maven-dependency-analyzer-1.11.1") Path projectPath) {
        new MavenModifier(projectPath)
            .addPlugin(buildPlugin(
                "com.github.fridujo",
                "retrokompat-maven-plugin",
                currentMavenProject.getVersion(),
                buildPluginExecutionForGoal("check")
            ))
            .flush();

        try (CloseableVerifier verifier = new CloseableVerifier(projectPath)) {
            verifier.execute(
                List.of(
                    "-DskipTests",
                    "-Dmaven.javadoc.skip=true",
                    "-Drat.skip",
                    "-Dmaven.site.skip=true"
                ),
                "verify");
            verifier.verifyErrorFreeLog();
            verifier.verifyTextInLog("[INFO] Checking backward compatibility against version 1.11.0");
        }
    }

    @Test
    void not_backward_compatible_project(@GitClone(
        uri = "https://github.com/fridujo/classpath-junit-extension.git") Path projectPath) {
        new MavenModifier(projectPath)
            .addPlugin(buildPlugin(
                "com.github.fridujo",
                "retrokompat-maven-plugin",
                currentMavenProject.getVersion(),
                buildPluginExecutionForGoal("check")
            ))
            .changePluginVersion("maven-javadoc-plugin", "3.2.0")
            .flush();

        try (CloseableVerifier verifier = new CloseableVerifier(projectPath)) {
            verifier.execute(
                List.of(
                    "-DskipTests",
                    "-Dmaven.source.skip",
                    "-Dmaven.javadoc.skip=true"
                ),
                "verify");
            verifier.verifyTextInLog("[INFO] Checking backward compatibility against version 1.0.0");
            verifier.verifyTextInLog("[ERROR] new version removes type com.github.fridujo.junit.extension.classpath.ModifiedClasspath");
            verifier.verifyBuildFailed();
        }
    }
}
