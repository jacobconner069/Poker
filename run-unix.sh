#!/usr/bin/env sh
set -eu
mkdir -p out
javac -d out src/*.java
exec java -cp "out:src" App
