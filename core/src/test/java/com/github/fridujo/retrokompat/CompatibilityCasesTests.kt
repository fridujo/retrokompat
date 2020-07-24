package com.github.fridujo.retrokompat

import com.github.fridujo.retrokompat.tools.asClass
import com.github.fridujo.retrokompat.tools.asJar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompatibilityCasesTests {

    @Test
    fun `similar signatures should not be identified as ambiguity`() {
        val jarPath = """
            public void doStuff(String s) {
            }
            
            public void doStuff(CharSequence s) {
            }
        """.trimIndent().asClass("Some").asJar("v1")

        val errorSet = CompatibilityChecker().check(jarPath, jarPath)

        assertThat(errorSet).isEmpty()
    }

    @Test
    fun `more specific return type is compatible`() {
        val v1 = 
        """
            public CharSequence doStuff() {
                return null;
            }
        """.asClass("Some").asJar("v1")

        val v2 = """
            public String doStuff() {
                return null;
            }
        """.trimIndent().asClass("Some").asJar("v2")

        val errorSet = CompatibilityChecker().check(v1, v2)

        assertThat(errorSet).isEmpty()
    }
}
