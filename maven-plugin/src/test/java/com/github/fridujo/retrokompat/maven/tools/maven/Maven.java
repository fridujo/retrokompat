package com.github.fridujo.retrokompat.maven.tools.maven;

import com.github.fridujo.retrokompat.maven.tools.Processes;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;

class Maven {

    private static final String M2_HOME = "M2_HOME";
    private static final Pattern MAVEN_HOME_LINE_PATTERN = Pattern.compile("^" + Pattern.quote("Maven home: ") + "(?<home>.+)$", Pattern.MULTILINE);

    static MavenProject buildProject(Path pomPath) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (InputStream is = Files.newInputStream(pomPath)) {
            Model model = reader.read(is);
            MavenProject mavenProject = new MavenProject(model);
            mavenProject.setFile(pomPath.toAbsolutePath().toFile());
            return mavenProject;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    static Optional<Path> getMavenHome() {
        Optional<Path> mavenHomeFromSysProps = Optional.ofNullable(getSystemVar(M2_HOME))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .filter(Files::exists)
            .filter(Files::isDirectory);
        if (mavenHomeFromSysProps.isPresent()) {
            return mavenHomeFromSysProps;
        } else {
            return getMavenHomeFromCli();
        }
    }

    private static Optional<Path> getMavenHomeFromCli() {
        Processes.ProcessResult result = Processes.launch("mvn -B -version");
        if (result.exitCode != 0) {
            return empty();
        }
        Matcher matcher = MAVEN_HOME_LINE_PATTERN.matcher(result.output);
        if (matcher.find()) {
            String homeStr = matcher.group("home");
            return Optional.of(Paths.get(homeStr).normalize().toAbsolutePath());
        } else {
            return empty();
        }
    }


    private static String getSystemVar(String name) {
        return Optional.ofNullable(System.getProperty(name)).orElse(System.getenv(name));
    }
}
