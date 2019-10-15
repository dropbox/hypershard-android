/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */

package com.dropbox.android.kotlin

import android.support.test.runner.AndroidJUnit4
import com.dropbox.core.test.ui_runner.annotations.UiTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A deliberately ignored class with a few tests inside
 */
@RunWith(AndroidJUnit4::class)
class FakeClassTest {
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
