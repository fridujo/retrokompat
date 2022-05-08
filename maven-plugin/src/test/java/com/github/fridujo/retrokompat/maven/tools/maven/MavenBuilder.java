package com.github.fridujo.retrokompat.maven.tools.maven;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

import java.util.Collections;

public class MavenBuilder {

    public static Plugin buildPlugin(String groupId, String artifactId, String version, PluginExecution execution) {
        Plugin plugin = new Plugin();
        plugin.setGroupId(groupId);
        plugin.setArtifactId(artifactId);
        plugin.setVersion(version);
        plugin.setExecutions(Collections.singletonList(execution));
        return plugin;
    }

    public static PluginExecution buildPluginExecutionForGoal(String goal) {
        PluginExecution execution = new PluginExecution();
        execution.setGoals(Collections.singletonList(goal));
        return execution;
    }
}
