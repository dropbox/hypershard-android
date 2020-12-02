![Build, Test and Lint](https://github.com/dropbox/hypershard-android/workflows/Build,%20Test%20and%20Lint/badge.svg)

# About
A fast and easy CLI tool that leverages AST (Abstract Syntax Tree) to parse test files for the purposes of test collection.

Apps that have a significant number of UI tests – like Paper or Dropbox apps – need to be sharded to run efficiently on CI. Unfortunately, UI test sharding generally requires you to build the entire application. Hypershard removes this first build by performing analysis of the Abstract Syntax Tree to output the list of all available tests.

At Dropbox, we run Android UI tests separately from JVM tests. Using historical data, we shard our UI tests such that each shard is balanced e.g. each shard ends approximately at the same time.

We do this by running tests using [ADB Instrumentation test options](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner) `-e testFile` where each shard has its own list of tests.

# Download
The `all` jar is executable and can be downloaded from [Maven Central](https://search.maven.org/search?q=g:com.dropbox.mobile.hypershard)

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).

Another use case could be to use Hypershard as a dependency in your project

```groovy
implementation 'com.dropbox.mobile.hypershard:hypershard:1.1.2'
```


# Usage
```
java -jar hypershard-1.1.2-all.jar --help
```

Here's an [example Python script](example/run_hypershard.py) that uses Hypershard as a CLI tool.

# Building
This command will build the jar with dependencies
```
./gradlew install
```

# Testing
```
./gradlew check
```

# Contributing
This is a standalone gradle project, you can open this project to start contributing
