#!/usr/bin/env sh
set -eu
mkdir -p out
javac -d out src/*.java test/*.java
exec java -cp out:src PokerLogicTest
