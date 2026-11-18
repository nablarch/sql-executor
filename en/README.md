# sql-executor

Tool for interactively executing SQL files containing Nablarch special syntax.

For details on database settings, build, distribution and operation, see [Nablarch SQL Executor](https://nablarch.github.io/docs/LATEST/doc/development_tools/toolbox/SqlExecutor/SqlExecutor.html) in the documentation.

## Prerequisites

It is compatible with only the following browsers.
* Firefox
* Chrome

## Getting Started

1. Clone the tool to a directory of your choice.
1. Make changes to the configuration according to the RDBMS that is used.
  1. Default configuration
      * URL・・・jdbc:h2:./h2/db/SAMPLE
      * User name・・・SAMPLE
      * Password・・・SAMPLE
  1. To change the connection URL, user, or password, modify the following file.
      * src/main/resources/db.config
  1. To change the JDBC driver, modify the following files.
      * pom.xml
      * src/main/resources/db.xml
1. To build with Java 11, 17 or 21, see [Java version settings](https://nablarch.github.io/docs/LATEST/doc/development_tools/toolbox/SqlExecutor/SqlExecutor.html#java-version-settings) in the documentation for the required settings.
1. Run the following command to launch the application.
    * mvn compile exec:java
