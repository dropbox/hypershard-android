package com.dropbox.mobile.hypershard

import java.io.File
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat

class HypershardTest {
    private val resources = "src/test/resources"
    private val hypershard: RealHyperShard
    private val files: Set<File>

    init {
        val file = File(resources).also { checkNotNull(it.absoluteFile) }
        hypershard = RealHyperShard(ClassAnnotationValue.Present("UiTest"), listOf(resources))
        files = hypershard.getFiles(file, ALLOWED_EXTENSIONS)
    }

    @Test
    fun `GIVEN hypershard WHEN get all files THEN all are found`() {
        assertThat(files.size).isEqualTo(6)
    }

    @Test
    fun `GIVEN files WHEN filter swift THEN no results`() {
        assertThat(hypershard.filterFiles(".swift", files)).isEmpty()
    }

    @Test
    fun `GIVEN files WHEN filter java THEN results found`() {
        assertThat(hypershard.filterFiles(".java", files).size).isEqualTo(3)
    }

    @Test
    fun `GIVEN files WHEN filter kt THEN results found`() {
        assertThat(hypershard.filterFiles(".kt", files).size).isEqualTo(3)
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
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(4)
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
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(5)
    }

    @Test
    fun `GIVEN tests WHEN gathering all UiTest THEN tests found`() {
        val tests = hypershard.gatherTests()
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(9)
    }

    @Test
    fun `GIVEN tests WHEN gathering all tests THEN tests found`() {
        val hypershard = RealHyperShard(ClassAnnotationValue.Empty(), listOf(resources))
        val tests = hypershard.gatherTests()
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests")
            .isEqualTo(13)
    }
}
