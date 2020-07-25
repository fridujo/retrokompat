package com.github.fridujo.retrokompat.executabledoc;

import com.github.fridujo.retrokompat.CompatibilityChecker;
import com.github.fridujo.retrokompat.CompatibilityError;
import com.github.fridujo.retrokompat.tools.JarMaker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RunnableTestCase implements Runnable {

    private final List<String> v1Sources = new ArrayList<>();
    private final List<String> v2Sources = new ArrayList<>();
    private final List<String> unqualifiedSources = new ArrayList<>();
    Boolean v2CodeFragment = null;
    Boolean compatible = null;
    final List<String> errorMessages = new ArrayList<>();

    @Override
    public void run() {
        Path v1JarPath = JarMaker.compileAndPackage("v1", v1Sources);
        Path v2JarPath = JarMaker.compileAndPackage("v2", v2Sources);

        Set<CompatibilityError> errors = new CompatibilityChecker().check(v1JarPath, v2JarPath);

        if (compatible) {
            assertThat(errors).isEmpty();
        } else {
            assertThat(errors.stream().map(Object::toString))
                .containsExactlyInAnyOrderElementsOf(errorMessages);
        }
    }

    public void addSource(String source) {
        if (v2CodeFragment != null && v2CodeFragment) {
            v2Sources.add(source);
        } else if (v2CodeFragment != null) {
            v1Sources.add(source);
        } else {
            unqualifiedSources.add(source);
        }
    }
}
