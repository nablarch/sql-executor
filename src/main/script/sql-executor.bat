cd /d %~dp0

start java -jar sql-executor.jar -diConfig classpath:config.xml -requestPath nse -userId testUser -g
cmd /c start http://localhost:7979/index.html