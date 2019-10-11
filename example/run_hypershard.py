import os
import subprocess
import urllib

if __name__ == "__main__":
    '''
    Shows usage of Hypershard as a CLI tool

    Input:
        java -jar hypershard.jar UiTest src/test/resources
    Output:
        com.dropbox.android.java.FakeIgnoredClassTest.emptyTest1
        com.dropbox.android.java.FakeIgnoredClassTest.emptyTest2
        com.dropbox.android.java.FakeIgnoredMethodTest.emptyTest1
        com.dropbox.android.java.FakeIgnoredMethodTest.emptyTest2
        com.dropbox.android.kotlin.FakeIgnoredClassTest.emptyTest1
        com.dropbox.android.kotlin.FakeIgnoredClassTest.emptyTest2
        com.dropbox.android.kotlin.FakeIgnoredMethodTest.emptyTest1
        com.dropbox.android.kotlin.FakeIgnoredMethodTest.emptyTest2
        com.dropbox.android.kotlin.FakeIgnoredMethodTest.emptyTest3
    '''
    file_name = "hypershard.jar"

    if not os.path.isfile(file_name):    
        url_opener = urllib.URLopener()
        url_opener.retrieve("https://search.maven.org/remotecontent?filepath=com/dropbox/mobile/hypershard/hypershard/1.0.0/hypershard-1.0.0.jar", file_name)
    hypershard_command = "java -jar hypershard.jar UiTest src/test/resources"
    print "Input: \n", hypershard_command
    output = subprocess.check_output(hypershard_command.split())
    print "Output: \n" , output

    '''
    Pseudocode from here on..
    Modify the test names so that it looks like "package.class#method"
    CI Service should then shard the list of tests from the output and spin up N nodes, e.g. 3 nodes are spun up each with 3 tests
    In each shard 
        - Save tests to the device's SD card
        - adb shell am instrument -w -e testFile /sdcard/tmp/testFile.txt com.android.foo/com.android.test.runner.AndroidJUnitRunner
    '''
