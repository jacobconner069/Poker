$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\*.java test\*.java
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp out PokerLogicTest
