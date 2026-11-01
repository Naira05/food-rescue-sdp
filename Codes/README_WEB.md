# Food Rescue – Web UI

Folder layout (next to `src`):
  web/index.html  web/style.css  web/app.js
  src/Server.java
  lib/sqlite-jdbc-<version>.jar   (you already use this)

Run (from the project root, where `web/` lives):
  javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")
  java  -cp "out:lib/*" Server            (Windows: use ; instead of :)
Open http://localhost:8080
Admin: admin@foodrescue.org / admin123
