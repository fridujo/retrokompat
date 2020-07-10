package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.fridujo.retrokompat.tools.JarMaker;
import com.github.fridujo.retrokompat.tools.PathUtils;

class CompatibilityCheckerTests {

    @Test
    void comparing_compatible_versions() {
        Path jarV1 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v1"));
        Path jarV2 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v2"));

        Set<CompatibilityError> errors = new CompatibilityChecker().check(jarV1, jarV2);

        assertThat(errors).isEmpty();
    }

    @Test
    void comparing_incompatible_versions() {
        Path jarV2 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v2"));
        Path jarV3 = JarMaker.compileAndPackage(PathUtils.forClassPath("jar_files/v3"));

        Set<CompatibilityError> errors = new CompatibilityChecker().check(jarV2, jarV3);

        assertThat(errors.stream().map(Object::toString)).containsExactlyInAnyOrder(
            "new version removes type com.github.Dog",
            "new version removes values from com.github.FoodType : [COOKED_MEAT]",
            "new version is missing public abstract void com.github.Animal.pet() throws com.github.NotPettableException",
            "new version is missing public void com.github.Cat.receiveFood(com.github.FoodType,java.lang.String) throws com.github.WrongFoodTypeException",
            "new version is missing public void com.github.Cat.pet() throws com.github.NotPettableException",
            "new version adds ambiguity for public java.lang.String com.github.Cat.doThat(java.lang.String)"
        );
    }
}
