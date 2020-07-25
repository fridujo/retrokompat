package com.github.fridujo.retrokompat.maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.github.fridujo.retrokompat.CompatibilityChecker;
import com.github.fridujo.retrokompat.CompatibilityError;

@Mojo(
    name = "check",
    defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class RetrokompatMojo extends AbstractMojo {

    private final VersionScanner versionScanner;
    private final JarResolver jarResolver;
    private final DependenciesResolver dependenciesResolver;
    private final CompatibilityChecker compatibilityChecker = new CompatibilityChecker();

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    private ArtifactRepository localRepository;
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    private List<ArtifactRepository> remoteRepositories;

    @Inject
    public RetrokompatMojo(VersionScanner versionScanner, JarResolver jarResolver, DependenciesResolver dependenciesResolver) {
        this.versionScanner = versionScanner;
        this.jarResolver = jarResolver;
        this.dependenciesResolver = dependenciesResolver;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (nonJarProject()) return;

        Path currentJarPath = getProjectJarPathOrThrowIfMissing();

        Optional<String> potentialLastVersion = versionScanner.getLastVersion(
            project.getArtifact(),
            localRepository,
            remoteRepositories);
        if (potentialLastVersion.isEmpty()) {
            getLog().warn("No previous versions found from:" +
                Stream.concat(
                    Stream.of(localRepository),
                    remoteRepositories.stream()
                ).map(Object::toString)
                    .collect(Collectors.joining("\n\t", "\n\t", ""))
            );
        } else {
            String lastVersion = potentialLastVersion.get();

            getLog().info("Checking backward compatibility against version " + lastVersion);

            JarWithDependencies v1JarWithDependencies = jarResolver.resolveVersion(
                project.getGroupId(),
                project.getArtifactId(),
                lastVersion,
                mavenSession.getProjectBuildingRequest());

            Set<Path> currentProjectDeps = dependenciesResolver.getDependenciesOfArtifact(mavenSession.getProjectBuildingRequest(), project.getArtifact());
            Set<CompatibilityError> errors;
            try {
                errors = compatibilityChecker.check(
                    v1JarWithDependencies.jarPath,
                    v1JarWithDependencies.dependenciesPaths,
                    currentJarPath,
                    currentProjectDeps);
            } catch (RuntimeException | Error e) {
                throw new MojoExecutionException("Unable to check dependencies of\n" + v1JarWithDependencies.jarPath + "\n" + currentJarPath + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n", e);
            }
            errors.forEach(e -> getLog().error(e.toString()));
            if (!errors.isEmpty()) {
                throw new MojoFailureException("Backward compatibility issues");
            }
        }
    }

    private Path getProjectJarPathOrThrowIfMissing() throws MojoFailureException {
        Build build = project.getBuild();
        Path currentJarPath = Paths.get(build.getDirectory()).resolve(build.getFinalName() + ".jar");
        if (!Files.exists(currentJarPath)) {
            throw new MojoFailureException("Missing jar file, please execute this plugin after the packaging phase");
        }
        return currentJarPath;
    }

    private boolean nonJarProject() {
        String type = project.getArtifact().getType();
        if (!"jar".equals(type)) {
            getLog().info("Skipping non-jar project (" + type + ")");
            return true;
        }
        return false;
    }
}
