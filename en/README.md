# sql-executor

Nablarch特殊構文を含むSQLファイルを対話的に実行するツールです。

## 前提条件

下記ブラウザでの実行にのみ対応しています。
* FireFox
* Chrome

## Getting Started

1. 本ツールを任意のディレクトリにcloneしてください。
1. 使用するRDBMSに応じて設定変更を行ってください。
  1. デフォルト設定
    * URL・・・jdbc:h2:./h2/db/SAMPLE
    * ユーザ名・・・SAMPLE
    * パスワード・・・SAMPLE
  1. 接続URLやユーザ、パスワードを変更する場合、以下のファイルを修正してください。
    * src/main/resources/sqle-db.config
  1. JDBCドライバを変更する場合、以下のファイルを修正してくだささい。
    * src/main/resources/sqle-db.xml
1. 以下のバッチファイルを実行し、アプリケーションを起動してください(ブラウザが起動します)。
  * nse-web.bat
