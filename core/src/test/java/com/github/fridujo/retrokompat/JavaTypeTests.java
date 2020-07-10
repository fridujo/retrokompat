package com.github.fridujo.retrokompat;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JavaTypeTests {

    @ParameterizedTest(name = "{0} is less specific than {1} : {2}")
    @CsvSource({
        "java.lang.Object, java.lang.CharSequence, true",
        "java.lang.Object, java.lang.String, true",
        "java.lang.CharSequence, java.lang.String, true",
        "java.lang.Object, javax.naming.directory.Attribute, true",
        "java.io.Serializable, javax.naming.directory.Attribute, true",

        "java.lang.String, java.lang.CharSequence, false",
        "javax.naming.directory.Attribute, java.io.Serializable, false",
    })
    void isLessSpecific_returns_true_if_in_hierarchy(Class<?> upper, Class<?> lower, boolean expected) {
        assertThat(new JavaType(upper).isLessSpecific(new JavaType(lower))).isEqualTo(expected);
    }

    @Test
    void listTypesInHierarchy_returns_all_types() {
        Set<String> hierarchy = JavaType.listTypesInHierarchy(C3.class);

        assertThat(hierarchy).containsExactlyInAnyOrder(
            "com.github.fridujo.retrokompat.JavaTypeTests$I1",
            "com.github.fridujo.retrokompat.JavaTypeTests$I2",
            "com.github.fridujo.retrokompat.JavaTypeTests$I3",
            "com.github.fridujo.retrokompat.JavaTypeTests$C1",
            "com.github.fridujo.retrokompat.JavaTypeTests$C2",
            "java.lang.Cloneable",
            "java.io.Serializable",
            "java.lang.Object"
        );
    }

    interface I1 {
    }

    interface I2 {
    }

    interface I3 {
    }

    class C1 implements I2, Cloneable {

    }

    class C2 extends C1 implements I3 {

    }

    class C3 extends C2 implements I1, Serializable {

    }
}
