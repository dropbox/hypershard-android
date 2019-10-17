/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */

package com.dropbox.android.kotlin

import android.support.test.runner.AndroidJUnit4
import com.dropbox.core.test.ui_runner.annotations.UiTest
import kotlin.test.Test
import org.junit.Ignore
import org.junit.runner.RunWith

/**
 * A class with ignored methods, parser should find 3 valid tests
 */
@UiTest
@RunWith(AndroidJUnit4::class)
class FakeIgnoredMethodUiTest {
    /**
     * lint needs a javadoc comment
     */
    @Test
    fun emptyTest1() {
    }

    /**
     * lint needs a javadoc comment
     */
    @Ignore
    @Test
    fun emptyTest2() {
    }

    /**
     * lint needs a javadoc comment
     */
    @Test
    fun emptyTest3() {
    }
}
