cd /d %~dp0

start mvn compile exec:java
timeout 3
cmd /c start http://localhost:7979/index.html