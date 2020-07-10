package com.github.fridujo.retrokompat.maven.tools.maven;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

public class MavenBuilder {

    public static Plugin buildPlugin(String groupId, String artifactId, String version, PluginExecution execution) {
        Plugin plugin = new Plugin();
        plugin.setGroupId(groupId);
        plugin.setArtifactId(artifactId);
        plugin.setVersion(version);
        plugin.setExecutions(List.of(execution));
        return plugin;
    }

    public static PluginExecution buildPluginExecutionForGoal(String goal) {
        PluginExecution execution = new PluginExecution();
        execution.setGoals(List.of(goal));
        return execution;
    }
}
