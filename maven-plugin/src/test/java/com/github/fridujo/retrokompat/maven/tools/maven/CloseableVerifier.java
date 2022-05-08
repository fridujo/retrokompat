package com.github.fridujo.retrokompat.maven.tools.maven;

import com.github.fridujo.retrokompat.maven.tools.LoggerFactory;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.shared.utils.io.FileUtils;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CloseableVerifier implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableVerifier.class);

    private static final String m2Home = Maven.getMavenHome().map(Path::toString).orElse(null);

    static {
        if (m2Home != null) {
            // This is to avoid Verifier from using null as defaultMavenHome and
            // trying to launch Maven from the classpath only
            System.setProperty("maven.home", m2Home);
        }
    }

    public final Verifier verifier;

    public CloseableVerifier(Path path) {
        try {
            verifier = new Verifier(path.toString(), null, false, false);
            verifier.setAutoclean(false);
            verifier.setSystemProperty("maven.multiModuleProjectDirectory", m2Home);
            Field defaultMavenHomeField = Verifier.class.getDeclaredField("defaultMavenHome");
            defaultMavenHomeField.setAccessible(true);
            LOGGER.info("using MavenVerifier with m2Home: " + defaultMavenHomeField.get(verifier));
        } catch (VerificationException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        verifier.resetStreams();
    }

    public void execute(List<String> cliOptions, String... goals) {
        List<String> goalList = Arrays.asList(goals);
        verifier.setCliOptions(cliOptions);
        try {
            verifier.executeGoals(goalList);
        } catch (VerificationException e) {
            if (e.getMessage().startsWith("Exit code was non-zero")) {
                LOGGER.info("Maven build failed: " + e.getMessage().replace("\n", "\n" + repeat(" ", 8) + "> "));
            } else {
                throw new IllegalStateException("Maven failed to execute " + goalList + cliOptions + "\n" + e.getMessage().replace("\n", "\n>>>\t"), e);
            }
        }
    }

    private String repeat(String token, int times) {
        return new String(new char[times]).replace("\0", token);
    }

    public void verifyErrorFreeLog() {
        try {
            verifier.verifyErrorFreeLog();
        } catch (VerificationException e) {
            throw new AssertionFailedError(e.getMessage(), e);
        }
    }

    public void verifyBuildFailed() {
        verifierLogContains(
            "[INFO] ------------------------------------------------------------------------",
            "[INFO] BUILD FAILURE",
            "[INFO] ------------------------------------------------------------------------");
    }

    public void verifierLogContains(String... lines) {
        Arrays.stream(lines).forEach(this::verifyTextInLog);
    }

    public void verifyTextInLog(String text) {
        try {
            verifier.verifyTextInLog(text);
        } catch (VerificationException e) {
            Path logPath = Paths.get(verifier.getBasedir()).toAbsolutePath().resolve(verifier.getLogFileName());
            String logContent;
            try {
                logContent = FileUtils.fileRead(logPath.toFile());
            } catch (IOException ioe) {
                throw new UncheckedIOException("Unable to read " + logPath, ioe);
            }
            throw new AssertionFailedError("Given text is not in log: " + text + "\n\n" + logContent, e);
        }
    }


}
