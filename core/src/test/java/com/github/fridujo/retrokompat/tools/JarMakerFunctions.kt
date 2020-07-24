package com.github.fridujo.retrokompat.tools

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.tools.Diagnostic
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

fun String.asClass(name: String): String =
    """
        public class $name {
            ${this.trimIndent().replace("\n", "\n            ").trim()}
        }
    """.trimIndent()

fun String.asJar(name: String): Path {
    val targetDirectory = Paths.get("").resolve("target")
    val containingFolder = targetDirectory.resolve("generated-classes").resolve(name)
    if (Files.exists(containingFolder)) {
        FileUtils.deleteFolder(containingFolder)
    }
    Files.createDirectories(containingFolder);

    val className = this.trim().substringBefore(" {").substring(13)
    val classFile = containingFolder.resolve("$className.java")

    classFile.toFile().let {
        it.createNewFile()
        it.writeText(this)
    }

    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = compiler.getStandardFileManager(null, null, null)
    val javaFileObjects = fileManager.getJavaFileObjects(classFile)
    val options = listOf("-d", containingFolder.toString())
    check(compiler.getTask(null, fileManager, { d: Diagnostic<out JavaFileObject?>? -> println(d) }, options, null, javaFileObjects)
        .call()) { "Compilation error" }

    return JarMaker.createJarFile(containingFolder)
}
