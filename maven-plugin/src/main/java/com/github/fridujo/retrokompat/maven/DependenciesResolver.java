package com.github.fridujo.retrokompat.maven;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_PROVIDED;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = DependenciesResolver.class)
class DependenciesResolver extends SimpleArtifactResolver {

    static final Set<String> AUTHORIZED_SCOPES = Set.of(SCOPE_COMPILE, SCOPE_RUNTIME, SCOPE_PROVIDED);

    private final ProjectBuilder projectBuilder;

    @Inject
    DependenciesResolver(ProjectBuilder projectBuilder, ArtifactResolver artifactResolver) {
        super(artifactResolver);
        this.projectBuilder = projectBuilder;
    }

    Set<Path> getDependenciesOfArtifact(ProjectBuildingRequest buildingRequest, Artifact artifact) throws MojoExecutionException {
        ProjectBuildingResult buildingResult = buildProject(buildingRequest, artifact);
        return collectDependenciesPaths(buildingRequest, buildingResult, artifact);
    }

    private ProjectBuildingResult buildProject(ProjectBuildingRequest buildingRequest, Artifact artifact) throws MojoExecutionException {
        ProjectBuildingResult v1BuildingResult;
        try {
            buildingRequest.setResolveDependencies(true);
            v1BuildingResult = projectBuilder.build(artifact, buildingRequest);
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Unable to build version " + artifact, e);
        }
        return v1BuildingResult;
    }

    Set<Path> getDependenciesOfProject(ProjectBuildingRequest buildingRequest, MavenProject project) throws MojoExecutionException {
        try {
            return mapDependenciesToPaths(
                project.getDependencies(),
                d -> d.getScope(),
                Artifacts::buildCoordinate,
                buildingRequest);
        } catch (UncheckedArtifactResolverException e) {
            throw new MojoExecutionException("Unable to list dependencies of " + project.getArtifact(), e.cause);
        }
    }

    private Set<Path> collectDependenciesPaths(ProjectBuildingRequest buildingRequest, ProjectBuildingResult buildingResult, Artifact artifact) throws MojoExecutionException {
        try {
            return mapDependenciesToPaths(
                buildingResult.getDependencyResolutionResult().getDependencies(),
                d -> d.getScope(),
                Artifacts::buildCoordinate,
                buildingRequest);
        } catch (UncheckedArtifactResolverException e) {
            throw new MojoExecutionException("Unable to list dependencies of " + artifact, e.cause);
        }
    }

    private <DEPENDENCY> Set<Path> mapDependenciesToPaths(List<DEPENDENCY> dependencies,
                                                          Function<DEPENDENCY, String> scopeExtractor,
                                                          Function<DEPENDENCY, ArtifactCoordinate> artifactCoordinateMapper,
                                                          ProjectBuildingRequest buildingRequest) throws UncheckedArtifactResolverException {
        return dependencies
            .stream()
            .filter(d -> AUTHORIZED_SCOPES.contains(scopeExtractor.apply(d)))
            .map(d -> artifactCoordinateMapper.apply(d))
            .map(ac -> resolveArtifact(ac, buildingRequest, UncheckedArtifactResolverException::new))
            .map(a -> a.getFile().toPath())
            .collect(Collectors.toSet());
    }
}
