# sql-executor

Tool for interactively executing SQL files containing Nablarch special syntax.

## Prerequisites

It is compatible with only the following browsers.
* FireFox
* Chrome

## Getting Started

1. Clone the tool to a directory of your choice.
1. Make changes to the configuration according to the RDBMS that is used.
1. Default configuration
    * URL・・・jdbc:h2:./h2/db/SAMPLE
    * User name・・・SAMPLE
    * Password・・・SAMPLE
1. To change the connection URL, user, or password, modify the following file.
    * src/main/resources/sqle-db.config
1. To change the JDBC driver, modify the following file. 
    * src/main/resources/sqle-db.xml
1. Run the following batch file to launch the application (the browser will launch):
    * nse-web.bat
