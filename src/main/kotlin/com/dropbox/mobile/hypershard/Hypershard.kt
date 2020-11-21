/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */
package com.dropbox.mobile.hypershard

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
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

    /**
     * Gathers all the Java and Kotlin test files for a set of test directories.
     *
     * @return list of files which contain tests
     */
    fun gatherTestFiles(): List<File>
}

/**
 * @see [HyperShard]
 *
 * @property annotationValue annotation name at the class level e.g. UiTest (@ not needed)
 * @property dirs list of directories to parse. Hypershard will walk down from the root.
 */
class RealHyperShard(
    private val annotationValue: ClassAnnotationValue,
    private val notAnnotationValue: ClassAnnotationValue,
    private val dirs: List<String>
) : HyperShard {

    /**
     * @see [HyperShard.gatherTests]
     */
    override fun gatherTests(): List<String> {
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
     * @see [HyperShard.gatherTests]
     */
    override fun gatherTestFiles(): List<File> {
        val testFiles = mutableListOf<File>()
        for (dir in dirs) {
            val projectDir = File(dir)
            check(projectDir.exists()) {
                "Invalid directory: $projectDir"
            }

            // Get the full list of source files first, so we only walk the directory once
            val sourceFiles = getFiles(projectDir, ALLOWED_EXTENSIONS)
            val javaFiles = filterFiles(JAVA_EXTENSION, sourceFiles).filter { isJavaTest(it) }
            val ktFiles = filterFiles(KOTLIN_EXTENSION, sourceFiles).filter { isKotlinTest(it) }
            testFiles += javaFiles + ktFiles
        }
        return testFiles
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
        checkIsJava(file)

        val tests = ArrayList<String>()
        object : VoidVisitorAdapter<Any>() {
            override fun visit(cu: CompilationUnit, arg: Any?) {
                super.visit(cu, arg)
                val types = cu.types
                for (type in types) {
                    val classOrInterfaceDeclaration =
                        type as? ClassOrInterfaceDeclaration ?: continue
                    val shouldProcessTestMethod =
                        shouldProcessTestMethods(classOrInterfaceDeclaration, annotationValue,
                            notAnnotationValue)
                    if (shouldProcessTestMethod) {
                        val methods = type.methods
                        for (method in methods) {
                            val annotationTest = method.getAnnotationByName("Test")
                            if (annotationTest.isPresent) {
                                tests.add("${cu.packageDeclaration.get().name}." +
                                    "${type.name}.${method.name}"
                                )
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
     * @param file a .java file
     */
    private fun isJavaTest(file: File): Boolean {
        checkIsJava(file)

        var isTest = false
        object : VoidVisitorAdapter<Any>() {
            override fun visit(cu: CompilationUnit, arg: Any?) {
                if (isTest) {
                    return
                }
                super.visit(cu, arg)
                val types = cu.types
                for (type in types) {
                    val classOrInterfaceDeclaration =
                        type as? ClassOrInterfaceDeclaration ?: continue
                    val shouldProcessTestMethod =
                        shouldProcessTestMethods(classOrInterfaceDeclaration, annotationValue,
                            notAnnotationValue)
                    if (shouldProcessTestMethod) {
                        val methods = type.methods
                        for (method in methods) {
                            val annotationTest = method.getAnnotationByName("Test")
                            if (annotationTest.isPresent) {
                                isTest = true
                                return
                            }
                        }
                    }
                }
            }
        }.visit(JavaParser.parse(file), null)

        return isTest
    }

    private fun checkIsJava(file: File) {
        check(file.extension == JAVA_EXTENSION) {
            "Only java files are supported: $file"
        }
    }

    /**
     * Returns true if we found the class annotation or if's meant to be empty
     */
    private fun shouldProcessTestMethods(
        classOrInterfaceDeclaration: ClassOrInterfaceDeclaration,
        annotationValue: ClassAnnotationValue,
        notAnnotationValue: ClassAnnotationValue
    ): Boolean {
        val hasAnnotationValue = when (annotationValue) {
            is ClassAnnotationValue.Present -> {
                hasAnnotation(classOrInterfaceDeclaration, annotationValue)
            }
            is ClassAnnotationValue.Empty -> true
        }

        val hasNotAnnotationValue = when (notAnnotationValue) {
            is ClassAnnotationValue.Present -> {
                hasAnnotation(classOrInterfaceDeclaration, notAnnotationValue)
            }
            is ClassAnnotationValue.Empty -> false
        }

        return hasAnnotationValue && !hasNotAnnotationValue
    }

    private fun hasAnnotation(
        classOrInterfaceDeclaration: ClassOrInterfaceDeclaration,
        annotationValue: ClassAnnotationValue.Present
    ): Boolean {
        val annotationUiTest =
            classOrInterfaceDeclaration.getAnnotationByName(annotationValue.annotationName)
        return annotationUiTest.isPresent
    }

    /**
     * AST Java parsing to retrieve all the fully qualified test names in a file
     *
     * @param file a .kt file
     */
    internal fun collectTestsFromKotlinFile(file: File): List<String> {
        checkIsKotlin(file)

        val tests = ArrayList<String>()
        val parser = Parser()
        val code = String(Files.readAllBytes(file.toPath()))
        val ktFile = parser.parsePsiFile(code)
        val allDeclarations = Arrays.stream(ktFile.children).toList()
        for (declaration in allDeclarations) {
            val ktClass = declaration as? KtClass ?: continue

            val shouldProcessTestMethods = shouldProcessTestMethods(ktClass, annotationValue,
                notAnnotationValue)
            if (!shouldProcessTestMethods) {
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

    /**
     * AST Java parsing to retrieve all the fully qualified test names in a file
     *
     * @param file a .kt file
     */
    internal fun isKotlinTest(file: File): Boolean {
        checkIsKotlin(file)

        val parser = Parser()
        val code = String(Files.readAllBytes(file.toPath()))
        val ktFile = parser.parsePsiFile(code)
        val allDeclarations = Arrays.stream(ktFile.children).toList()
        for (declaration in allDeclarations) {
            val ktClass = declaration as? KtClass ?: continue

            val shouldProcessTestMethods = shouldProcessTestMethods(ktClass, annotationValue,
                notAnnotationValue)
            if (!shouldProcessTestMethods) {
                continue
            }

            for (element in declaration.children) {
                val ktClassBody = element as? KtClassBody ?: continue

                for (fn in ktClassBody.children) {
                    val ktNamedFunction = fn as? KtNamedFunction ?: continue
                    // the fqName includes the test name with the full package name and test name
                    // all delimited by periods, this allows the test to be ran because we need
                    // the hashtag
                    for (fnAnnotationEntry in ktNamedFunction.annotationEntries) {
                        if (fnAnnotationEntry.shortName.toString() == "Test") {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun checkIsKotlin(file: File) {
        check(file.extension == KOTLIN_EXTENSION) {
            "Only kotlin files are supported: $file"
        }
    }

    /**
     * Returns true if we found the class annotation or if's meant to be empty
     */
    private fun shouldProcessTestMethods(
        ktClass: KtClass,
        annotationValue: ClassAnnotationValue,
        notAnnotationValue: ClassAnnotationValue
    ): Boolean {
        val hasAnnotationValue = when (annotationValue) {
            is ClassAnnotationValue.Present -> {
                hasAnnotation(ktClass, annotationValue)
            }
            is ClassAnnotationValue.Empty -> true
        }

        val hasNotAnnotationValue = when (notAnnotationValue) {
            is ClassAnnotationValue.Present -> {
                hasAnnotation(ktClass, notAnnotationValue)
            }
            is ClassAnnotationValue.Empty -> false
        }
        return hasAnnotationValue && !hasNotAnnotationValue
    }

    private fun hasAnnotation(
        ktClass: KtClass,
        annotationValue: ClassAnnotationValue.Present
    ): Boolean {
        for (annotationEntry in ktClass.annotationEntries) {
            if (annotationEntry.shortName.toString() == annotationValue.annotationName) {
                return true
            }
        }
        return false
    }
}

/**
 * Entry point for Hypershard CLI
 */
class HypershardCommand :
    CliktCommand(
        help = "Hypershard is a fast and simple test collector that uses the Kotlin and Java " +
            "ASTs. Hypershard CLI will print full qualified test names found in dir(s)."
    ) {
    val annotationName by option(
        help = "Class annotation name to process. For example, if this was set to 'UiTest', " +
            "then Hypershard will only process classes annotated with @UiTest."
    )
        .default("")
    val notAnnotationName by option(
        help = "Class annotation name *not* to process. For example, if this was set to 'UiTest'," +
            " then Hypershard will *not* process classes annotated with @UiTest."
    )
        .default("")
    val dirs by argument(
        name = "dirs", help = "Dir(s) to process. " +
            "The location of the test classes to parse"
    )
        .multiple()

    override fun run() {
        val annotationValue = when (annotationName) {
            "" -> ClassAnnotationValue.Empty
            else -> ClassAnnotationValue.Present(annotationName)
        }
        val notAnnotationValue = when (notAnnotationName) {
            "" -> ClassAnnotationValue.Empty
            else -> ClassAnnotationValue.Present(notAnnotationName)
        }
        val hyperShard = RealHyperShard(annotationValue, notAnnotationValue, dirs)
        hyperShard.gatherTests().forEach(::println)
    }
}

fun main(args: Array<String>) {
    HypershardCommand().main(args)
}
