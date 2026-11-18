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
1. To build with Java 11, 17 or 21, apply the settings described in "Building with Java 11 / 17 / 21" below.
1. Run the following command to launch the application.
    * mvn compile exec:java

## Building with Java 11 / 17 / 21

The pom.xml of this tool inherits the parent POM of Nablarch 5 (nablarch-parent), in which the Java version to compile for is set to `1.6` by the `maven.compiler.source` and `maven.compiler.target` properties.
Since javac of JDK 12 or later does not support version 6, building as is with Java 17 or Java 21 results in a compilation error.

Explicitly set `maven.compiler.source` and `maven.compiler.target` according to the Java version to be used.
Specifying the `java.version` property does not change the Java version to build with (the parent POM does not refer to `java.version`).

* Setting in pom.xml (example for Java 17): add the following after the `<parent>` element.
    ```xml
    <properties>
      <maven.compiler.source>17</maven.compiler.source>
      <maven.compiler.target>17</maven.compiler.target>
    </properties>
    ```
* Setting on the command line (example for Java 17)
    ```
    mvn -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 compile exec:java
    ```
