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
 * A deliberately ignored class with a few tests inside
 */
@RunWith(AndroidJUnit4.class)
public class FakeIgnoredClassTest {
    /**
     * lint needs a javadoc comment
     */
    @Test
    public void emptyTest1() {}

    /**
     * lint needs a javadoc comment
     */
    @Test
    public void emptyTest2() {}
}
