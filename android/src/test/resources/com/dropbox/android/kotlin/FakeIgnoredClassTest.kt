/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */

package com.dropbox.android.kotlin

import android.support.test.runner.AndroidJUnit4
import com.dropbox.core.test.ui_runner.annotations.UiTest
import jdk.nashorn.internal.ir.annotations.Ignore
import kotlin.test.Test
import org.junit.Ignore
import org.junit.runner.RunWith

/**
 * A deliberately ignored class with a few tests inside
 */
@UiTest
@Ignore
@RunWith(AndroidJUnit4::class)
class FakeIgnoredClassTest {
    /**
     * lint needs a javadoc comment
     */
    @Test
    fun emptyTest1() {
    }

    /**
     * lint needs a javadoc comment
     */
    @Test
    fun emptyTest2() {
    }
}
