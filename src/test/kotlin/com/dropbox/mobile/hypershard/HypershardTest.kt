package com.dropbox.mobile.hypershard

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlin.test.Test

class HypershardTest {
    private val resources = "src/test/resources"
    private val hypershard: RealHyperShard
    private val files: Set<File>

    companion object {
        private val expectedJavaUiTests = listOf(
            "com.dropbox.android.java.FakeIgnoredMethodUiTest.emptyTest1",
            "com.dropbox.android.java.FakeIgnoredMethodUiTest.emptyTest2",
            "com.dropbox.android.java.FakeIgnoredClassUiTest.emptyTest1",
            "com.dropbox.android.java.FakeIgnoredClassUiTest.emptyTest2"
        )

        private val expectedKotlinUiTests = listOf(
            "com.dropbox.android.kotlin.FakeIgnoredClassUiTest.emptyTest1",
            "com.dropbox.android.kotlin.FakeIgnoredClassUiTest.emptyTest2",
            "com.dropbox.android.kotlin.FakeIgnoredMethodUiTest.emptyTest1",
            "com.dropbox.android.kotlin.FakeIgnoredMethodUiTest.emptyTest2",
            "com.dropbox.android.kotlin.FakeIgnoredMethodUiTest.emptyTest3"
        )

        private val expectedUiTests = expectedJavaUiTests + expectedKotlinUiTests
        private val expectedNonUiTests = listOf(
            "com.dropbox.android.java.FakeIgnoredClassTest.emptyTest1",
            "com.dropbox.android.java.FakeIgnoredClassTest.emptyTest2",
            "com.dropbox.android.kotlin.FakeClassTest.emptyTest1",
            "com.dropbox.android.kotlin.FakeClassTest.emptyTest2"
        )
        private val allTests = expectedUiTests + expectedNonUiTests
        private val allTestFiles = listOf(
            File("src/test/resources/com/dropbox/android/java/FakeIgnoredMethodUiTest.java"),
            File("src/test/resources/com/dropbox/android/java/FakeClassTest.java"),
            File("src/test/resources/com/dropbox/android/java/FakeIgnoredClassUiTest.java"),
            File("src/test/resources/com/dropbox/android/kotlin/FakeClassTest.kt"),
            File("src/test/resources/com/dropbox/android/kotlin/FakeIgnoredClassUiTest.kt"),
            File("src/test/resources/com/dropbox/android/kotlin/FakeIgnoredMethodUiTest.kt")
        )
    }

    init {
        val file = File(resources).also { checkNotNull(it.absoluteFile) }
        hypershard = RealHyperShard(ClassAnnotationValue.Present("UiTest"), listOf(resources))
        files = hypershard.getFiles(file, ALLOWED_EXTENSIONS)
    }

    @Test
    fun `GIVEN hypershard WHEN get all files THEN all are found`() {
        assertThat(files.size).isEqualTo(8)
    }

    @Test
    fun `GIVEN files WHEN filter swift THEN no results`() {
        assertThat(hypershard.filterFiles(".swift", files)).isEmpty()
    }

    @Test
    fun `GIVEN files WHEN filter java THEN results found`() {
        assertThat(hypershard.filterFiles(".java", files).size).isEqualTo(4)
    }

    @Test
    fun `GIVEN files WHEN filter kt THEN results found`() {
        assertThat(hypershard.filterFiles(".kt", files).size).isEqualTo(4)
    }

    @Test
    fun `GIVEN files WHEN processing java UiTest THEN tests found`() {
        val tests = mutableListOf<String>()
        with(tests) {
            addAll(arrayListOf())
        }
        val javaFiles = hypershard.filterFiles(".java", files)
        for (file in javaFiles) {
            tests.addAll(hypershard.collectTestsFromJavaFile(file))
        }
        assertThat(tests.size).isEqualTo(4)
        assertThat(tests).containsExactlyElementsIn(expectedJavaUiTests)
    }

    @Test
    fun `GIVEN files WHEN processing kt UiTest THEN tests found`() {
        val tests = mutableListOf<String>()
        with(tests) {
            addAll(arrayListOf())
        }
        val kotlinFiles = hypershard.filterFiles(".kt", files)
        for (file in kotlinFiles) {
            tests.addAll(hypershard.collectTestsFromKotlinFile(file))
        }
        assertThat(tests.size).isEqualTo(5)
        assertThat(tests).containsExactlyElementsIn(expectedKotlinUiTests)
    }

    @Test
    fun `GIVEN tests WHEN gathering all UiTest THEN tests found`() {
        val tests = hypershard.gatherTests()
        assertThat(tests.size).isEqualTo(9)
        assertThat(tests).containsExactlyElementsIn(expectedUiTests)
    }

    @Test
    fun `GIVEN tests WHEN gathering all tests THEN tests found`() {
        val hypershard = RealHyperShard(ClassAnnotationValue.Empty, listOf(resources))
        val tests = hypershard.gatherTests()
        assertThat(tests.size).isEqualTo(13)
        assertThat(tests).containsExactlyElementsIn(allTests)
    }

    @Test
    fun `GIVEN tests WHEN gathering all test files THEN tests files found`() {
        val hypershard = RealHyperShard(ClassAnnotationValue.Empty, listOf(resources))
        val tests = hypershard.gatherTestFiles()
        assertThat(tests.size).isEqualTo(6)
        assertThat(tests).containsExactlyElementsIn(allTestFiles)
    }
}
