package com.github.fridujo.retrokompat.maven.tools;

import java.util.HashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.eclipse.aether.artifact.DefaultArtifact;

public class TestArtifacts {

    public static Artifact buildArtifact(String groupId, String artifactId, String version) {
        ArtifactHandlerManager artifactHandlerManager = new DefaultArtifactHandlerManager();
        ReflectionUtils.setField(artifactHandlerManager, "artifactHandlers", new HashMap<>());

        DefaultArtifactFactory artifactFactory = new DefaultArtifactFactory();
        ReflectionUtils.setField(artifactFactory, "artifactHandlerManager", artifactHandlerManager);

        return artifactFactory.createProjectArtifact(groupId, artifactId, version);
    }

    public static org.eclipse.aether.graph.Dependency buildAetherDependency(String gav, String scope) {
        return new org.eclipse.aether.graph.Dependency(new DefaultArtifact(gav), scope);
    }

    public static org.apache.maven.model.Dependency buildMavenModelDependency(String groupId, String artifactId, String version, String scope) {
        org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setScope(scope);
        return dependency;
    }
}
