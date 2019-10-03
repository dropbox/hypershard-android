/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */
package com.dropbox.mobile.hypershard

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.io.File
import java.nio.file.Files
import java.util.Arrays
import kastree.ast.psi.Parser
import kotlin.streams.toList
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtNamedFunction

private const val JAVA_EXTENSION = "java"
private const val KOTLIN_EXTENSION = "kt"
internal val ALLOWED_EXTENSIONS = arrayOf(
    JAVA_EXTENSION,
    KOTLIN_EXTENSION
)

/**
 * Hypershard is a fast and simple test collector that uses the Kotlin and Java ASTs
 */
interface HyperShard {
    /**
     * Gathers all the Java and Kotlin tests for a set of test directories.
     *
     * @return list of fully qualified test methods [package.class.method]
     */
    fun gatherTests(): List<String>
}

/**
 * @see [HyperShard]
 *
 * @property annotationName annotation name at the class level e.g. UiTest (@ not needed)
 * @property dirs list of directories to parse. Hypershard will walk down from the root.
 */
class RealHyperShard(
    private val annotationName: String,
    private val dirs: Array<String>
) : HyperShard {

    /**
     * @see [HyperShard.gatherTests]
     */
    override fun gatherTests(): List<String> {
        checkUsage(dirs)

        val tests = mutableListOf<String>()
        for (dir in dirs) {
            val projectDir = File(dir)
            check(projectDir.exists()) {
                "Invalid directory: $projectDir"
            }

            // Get the full list of source files first, so we only walk the directory once
            val sourceFiles = getFiles(projectDir, ALLOWED_EXTENSIONS)
            val javaFiles = filterFiles(JAVA_EXTENSION, sourceFiles)
            val ktFiles = filterFiles(KOTLIN_EXTENSION, sourceFiles)
            for (file in javaFiles) {
                tests.addAll(collectTestsFromJavaFile(file))
            }
            for (file in ktFiles) {
                tests.addAll(collectTestsFromKotlinFile(file))
            }
        }
        return tests
    }

    /**
     * Walks down the directory path and gathers all files in the directory structure that match
     * the extension of file we're looking for
     */
    internal fun getFiles(projectDir: File, extensions: Array<String>): Set<File> {
        val files = hashSetOf<File>()
        DirExplorer(
            /**
             * Returns true if the file extension matches
             */
            object : DirExplorer.Filter {
                override fun interested(level: Int, path: String, file: File): Boolean {
                    return extensions.any {
                        path.endsWith(".$it")
                    }
                }
            },
            /**
             * Adds the interested file to the files list
             */
            object : DirExplorer.FileHandler {
                override fun handle(level: Int, path: String, file: File) {
                    files.add(file)
                }
            }
        ).explore(projectDir)
        return files
    }

    /**
     * Returns a list of files with a specific extension
     */
    internal fun filterFiles(ext: String, set: Set<File>): List<File> =
        set.filter { it.name.endsWith(ext) }

    /**
     * AST Java parsing to retrieve all the fully qualified test names in a file
     *
     * @param file a .java file
     */
    internal fun collectTestsFromJavaFile(file: File): List<String> {
        check(file.extension == JAVA_EXTENSION) {
            "Only java files are supported: $file"
        }

        val tests = ArrayList<String>()
        object : VoidVisitorAdapter<Any>() {
            override fun visit(cu: CompilationUnit, arg: Any?) {
                super.visit(cu, arg)
                val types = cu.types
                for (type in types) {
                    val classOrInterfaceDeclaration =
                            type as? ClassOrInterfaceDeclaration ?: continue

                    val annotationUiTest =
                            classOrInterfaceDeclaration.getAnnotationByName(annotationName)
                    if (annotationUiTest.isPresent) {
                        val methods = type.methods
                        for (method in methods) {
                            val annotationTest = method.getAnnotationByName("Test")
                            if (annotationTest.isPresent) {
                                tests.add(
                                        String.format("%s.%s.%s",
                                                cu.packageDeclaration.get().name,
                                                type.name,
                                                method.name))
                            }
                        }
                    }
                }
            }
        }.visit(JavaParser.parse(file), null)

        return tests
    }

    /**
     * AST Java parsing to retrieve all the fully qualified test names in a file
     *
     * @param file a .kt file
     */
    internal fun collectTestsFromKotlinFile(file: File): List<String> {
        check(file.extension == KOTLIN_EXTENSION) {
            "Only kotlin files are supported: $file"
        }

        val tests = ArrayList<String>()
        val parser = Parser()
        val code = String(Files.readAllBytes(file.toPath()))
        val ktFile = parser.parsePsiFile(code)
        val allDeclarations = Arrays.stream(ktFile.children).toList()
        for (declaration in allDeclarations) {
            val ktClass = declaration as? KtClass ?: continue

            var uiTestFound = false
            for (annotationEntry in ktClass.annotationEntries) {
                if (annotationEntry.shortName.toString() == annotationName) {
                    uiTestFound = true
                    break
                }
            }
            if (!uiTestFound) {
                continue
            }

            for (element in declaration.children) {
                val ktClassBody = element as? KtClassBody ?: continue

                for (fn in ktClassBody.children) {
                    val ktNamedFunction = fn as? KtNamedFunction ?: continue
                    // the fqName includes the test name with the full package name and test name
                    // all delimited by periods, this allows the test to be ran because we need
                    // the hashtag
                    val fqName = fn.fqName.toString()
                    var methodTestFound = false
                    for (fnAnnotationEntry in ktNamedFunction.annotationEntries) {
                        if (fnAnnotationEntry.shortName.toString() == "Test") {
                            methodTestFound = true
                            break
                        }
                    }
                    if (methodTestFound) {
                        tests.add(fqName)
                    }
                }
            }
        }
        return tests
    }
}

fun main(args: Array<String>) {
    checkUsage(args)
    val annotationName = args[0]
    val dirs = args.sliceArray(1 until args.size)
    val processor = RealHyperShard(annotationName, dirs)
    processor.gatherTests().stream().forEach(::println)
}

private fun checkUsage(dirs: Array<String>) {
    check(dirs.isNotEmpty()) {
        "Usage: " +
            "java -jar hypershard-x.y.z-all.jar " +
            "annotationName dir1 dir2 dir3 ..."
    }
}
