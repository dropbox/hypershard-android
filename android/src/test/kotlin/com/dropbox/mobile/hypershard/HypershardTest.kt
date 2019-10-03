package com.dropbox.mobile.hypershard

import java.io.File
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat

class HypershardTest {
    private val resources = "src/test/resources"
    private val processor: RealHyperShard
    private val files: Set<File>

    init {
        val file = File(resources).also { checkNotNull(it.absoluteFile) }
        processor = RealHyperShard("UiTest", arrayOf(resources))
        files = processor.getFiles(file, ALLOWED_EXTENSIONS)
    }

    @Test
    fun getFiles() {
        assertThat(files.size).isEqualTo(4)
    }

    @Test
    fun test_GIVEN_files_WHEN_filter_swift_THEN_no_results() {
        assertThat(processor.filterFiles(".swift", files)).isEmpty()
    }

    @Test
    fun test_GIVEN_files_WHEN_filter_java_THEN_results_found() {
        assertThat(processor.filterFiles(".java", files).size).isEqualTo(2)
    }

    @Test
    fun test_GIVEN_files_WHEN_filter_kt_THEN_results_found() {
        assertThat(processor.filterFiles(".kt", files).size).isEqualTo(2)
    }

    @Test
    fun test_GIVEN_files_WHEN_processing_java_then_tests_found() {
        val tests = mutableListOf<String>()
        with(tests) {
            addAll(arrayListOf())
        }
        val javaFiles = processor.filterFiles(".java", files)
        for (file in javaFiles) {
            tests.addAll(processor.collectTestsFromJavaFile(file))
        }
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(4)
    }

    @Test
    fun test_GIVEN_files_WHEN_processing_kt_then_tests_found() {
        val tests = mutableListOf<String>()
        with(tests) {
            addAll(arrayListOf())
        }
        val kotlinFiles = processor.filterFiles(".kt", files)
        for (file in kotlinFiles) {
            tests.addAll(processor.collectTestsFromKotlinFile(file))
        }
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(5)
    }

    @Test
    fun test_GIVEN_tests_WHEN_gathering_all_tests_THEN_tests_found() {
        val tests = processor.gatherTests()
        assertThat(tests.size).`as`("Test cases found does not match expected: $tests").isEqualTo(9)

        for (test in tests) {
            assertThat(test).doesNotContain("#")
        }
    }
}
