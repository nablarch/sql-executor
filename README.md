# sql-executor

Nablarch特殊構文を含むSQLファイルを対話的に実行するツールです。

DB設定、ビルド、配布、操作方法の詳細は、解説書の[Nablarch SQL Executor](https://nablarch.github.io/docs/LATEST/doc/development_tools/toolbox/SqlExecutor/SqlExecutor.html)を参照してください。

## 前提条件

下記ブラウザでの実行にのみ対応しています。
* Firefox
* Chrome

## Getting Started

1. 本ツールを任意のディレクトリにcloneしてください。
1. 使用するRDBMSに応じて設定変更を行ってください。
  1. デフォルト設定
      * URL・・・jdbc:h2:./h2/db/SAMPLE
      * ユーザ名・・・SAMPLE
      * パスワード・・・SAMPLE
  1. 接続URLやユーザ、パスワードを変更する場合、以下のファイルを修正してください。
      * src/main/resources/db.config
  1. JDBCドライバを変更する場合、以下のファイルを修正してくだささい。
      * pom.xml
      * src/main/resources/db.xml
1. Java 11、17、21でビルドする場合は、後述の「Java 11 / 17 / 21でビルドする場合」の設定を行ってください。
1. 以下のコマンドを実行し、アプリケーションを起動してください。
    * mvn compile exec:java

## Java 11 / 17 / 21でビルドする場合

本ツールのpom.xmlはNablarch 5の親POM(nablarch-parent)を継承しており、親POMではコンパイル対象のJavaバージョンが`maven.compiler.source`と`maven.compiler.target`プロパティで`1.6`に設定されています。
JDK 12以降のjavacはバージョン6をサポートしていないため、Java 17やJava 21でそのままビルドするとコンパイルエラーになります。

使用するJavaのバージョンに合わせて、`maven.compiler.source`と`maven.compiler.target`を明示的に設定してください。
`java.version`プロパティを指定しても、ビルドするJavaのバージョンは変わりません(親POMは`java.version`を参照していません)。

* pom.xmlで設定する場合(Java 17の例)・・・`<parent>`要素の後に以下を追加してください。
    ```xml
    <properties>
      <maven.compiler.source>17</maven.compiler.source>
      <maven.compiler.target>17</maven.compiler.target>
    </properties>
    ```
* コマンドラインで設定する場合(Java 17の例)
    ```
    mvn -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 compile exec:java
    ```
