# Hypershard

A fast and easy CLI tool that leverages AST (Abstract Syntax Tree) to parse test files for the purposes of test collection. 

Apps that have a significant number of UI tests – like Paper or Dropbox apps – need to be sharded to run efficiently on CI. Unfortunately, UI test sharding generally requires you to build the entire application. Hypershard removes this first build by performing analysis of the Abstract Syntax Tree to output the list of all available tests.

Java/Kotlin/Android is available

Swift/iOS coming soon
