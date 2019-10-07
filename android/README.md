# About
Hypershard is a fast and simple test collector that works on Java and Kotlin source files. Using Hypershard, we are able to send a subset of the tests to separate nodes.

At Dropbox, we run Android UI tests separately from JVM tests. Using historical data, we shard our UI tests such that each shard is balanced e.g. each shard ends approximately at the same time.

We do this by running tests using [ADB Instrumentation test options](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner) `-e testFile` where each shard has its own list of tests.

# Download
```groovy
implementation 'com.dropbox.mobile.hypershard:hypershard:1.0.0'
```

The jar is executable and can be downloaded from Maven Central.

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).

# Usage
The argument is a vararg, you can pass in as many directories as you want, separated by spaces
```
java -jar hypershard-1.0.0-all.jar UiTest /Users/changd/dev/xplat/android/dbapp/Dropbox/test/src/uitests
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
