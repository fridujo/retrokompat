package com.github.fridujo.retrokompat;

import com.github.fridujo.retrokompat.tools.JarMaker;
import com.github.fridujo.retrokompat.tools.PathUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static com.github.fridujo.retrokompat.tools.PathUtils.getDependencyPath;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class CompatibilityCheckerTests {

    private final Path opentest4jPath = getDependencyPath("opentest4j");

    @Test
    void comparing_compatible_versions() {
        Path jarV1 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v1"));
        Path jarV2 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v2"));

        Set<CompatibilityError> errors = new CompatibilityChecker().check(jarV1, emptySet(), jarV2, Collections.singleton(opentest4jPath));

        assertThat(errors).isEmpty();
    }

    @Test
    void comparing_incompatible_versions() {
        Path jarV2 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v2"));
        Path jarV3 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v3"));

        Set<CompatibilityError> errors = new CompatibilityChecker().check(jarV2, Collections.singleton(opentest4jPath), jarV3, emptySet());

        assertThat(errors.stream().map(Object::toString)).containsExactlyInAnyOrder(
            "new version removes type com.github.Dog",
            "new version removes values from com.github.FoodType : [COOKED_MEAT]",
            "new version is missing public abstract void com.github.Animal.pet() throws com.github.NotPettableException",
            "new version is missing public void com.github.Cat.receiveFood(com.github.FoodType,java.lang.String) throws com.github.WrongFoodTypeException",
            "new version is missing public void com.github.Cat.pet() throws com.github.NotPettableException"
        );
    }
}
