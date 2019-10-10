# About
A fast and easy CLI tool that leverages AST (Abstract Syntax Tree) to parse test files for the purposes of test collection.

Apps that have a significant number of UI tests – like Paper or Dropbox apps – need to be sharded to run efficiently on CI. Unfortunately, UI test sharding generally requires you to build the entire application. Hypershard removes this first build by performing analysis of the Abstract Syntax Tree to output the list of all available tests.

At Dropbox, we run Android UI tests separately from JVM tests. Using historical data, we shard our UI tests such that each shard is balanced e.g. each shard ends approximately at the same time.

We do this by running tests using [ADB Instrumentation test options](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner) `-e testFile` where each shard has its own list of tests.

[![Build Status](https://travis-ci.org/dropbox/hypershard-android.svg?branch=master)](https://travis-ci.org/dropbox/hypershard-android)

# Download
The jar is executable and can be downloaded from Maven Central: https://search.maven.org/search?q=g:com.dropbox.mobile.hypershard

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).

Another use case could be to use Hypershard as a dependency in your project

```groovy
implementation 'com.dropbox.mobile.hypershard:hypershard:1.0.0'
```


# Usage
The argument is a vararg, you can pass in as many directories as you want, separated by spaces
```
java -jar hypershard-1.0.0.jar $annotation $path

# @param annotation - test class annotation to look for e.g. UiTest
# @param path - The location of the test classes to parse e.g. /path/to/tests
```
The output is a list of fully qualified tests separated by new lines.

# Building
This command will build the jar with dependencies
```
./gradlew shadowJar
```

# Testing
```
./gradlew test
```

# Contributing
This is a standalone gradle project, you can open this project to start contributing
