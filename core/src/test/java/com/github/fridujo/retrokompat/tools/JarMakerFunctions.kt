package com.github.fridujo.retrokompat.tools

import java.nio.file.Path

fun String.asClass(name: String): String =
    """
        public class $name {
            ${this.trimIndent().replace("\n", "\n            ").trim()}
        }
    """.trimIndent()

fun String.asJar(name: String): Path {
    return JarMaker.compileAndPackage(name, setOf(this))
}
