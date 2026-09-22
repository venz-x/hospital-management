#!/bin/bash
# Compiles every .java file into out/ and starts the Swing application.
cd "$(dirname "$0")" || exit 1

mkdir -p out

echo "Compiling..."
javac -d out $(find src -name "*.java") || exit 1

echo "Starting Hospital Management System..."
java -cp out Main "$@"
