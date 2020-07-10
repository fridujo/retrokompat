package com.github.fridujo.retrokompat.maven.tools.maven;

import java.nio.file.Paths;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class MavenExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == MavenProject.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        MavenModifier mavenModifier = new MavenModifier(Paths.get("").resolve("pom.xml"));
        return mavenModifier.getProject();
    }
}
