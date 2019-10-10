/*
 * Copyright (c) 2018, Dropbox, Inc. All rights reserved.
 */

package com.dropbox.android.java;

import android.support.test.runner.AndroidJUnit4;
import com.dropbox.core.test.ui_runner.annotations.UiTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A class with ignored methods, parser should find 1 valid test
 */
@UiTest
@RunWith(AndroidJUnit4.class)
public class FakeIgnoredMethodTest {
    /**
     * lint needs a javadoc comment
     */
    @Test
    public void emptyTest1() {}

    /**
     * lint needs a javadoc comment
     */
    @Ignore
    @Test
    public void emptyTest2() {}
}
