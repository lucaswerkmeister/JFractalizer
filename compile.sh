#!/usr/bin/env bash

set -e

shopt -s globstar

(
    cd 'JFractalizer - Core'
    javac -d bin src/**/*.java
)

(
    cd 'JFractalizer - Default Plugin'
    javac -d bin -cp '../JFractalizer - Core/bin' src/**/*.java
    cp -r src/META-INF bin/
)
