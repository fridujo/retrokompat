package com.github.fridujo.retrokompat.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.codehaus.plexus.component.annotations.Component;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.maven.artifact.Artifact.*;

@Component(role = DependenciesResolver.class)
class DependenciesResolver extends SimpleArtifactResolver {

    static final Set<String> AUTHORIZED_SCOPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(SCOPE_COMPILE, SCOPE_RUNTIME, SCOPE_PROVIDED)));

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

    private Set<Path> collectDependenciesPaths(ProjectBuildingRequest buildingRequest, ProjectBuildingResult buildingResult, Artifact artifact) throws MojoExecutionException {
        try {
            return buildingResult.getDependencyResolutionResult().getDependencies()
                .stream()
                .filter(d -> AUTHORIZED_SCOPES.contains(d.getScope()))
                .map(Artifacts::buildCoordinate)
                .map(ac -> resolveArtifact(ac, buildingRequest, UncheckedArtifactResolverException::new))
                .map(a -> a.getFile().toPath())
                .collect(Collectors.toSet());
        } catch (UncheckedArtifactResolverException e) {
            throw new MojoExecutionException("Unable to list dependencies of " + artifact, e.cause);
        }
    }
}
