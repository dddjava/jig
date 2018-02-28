#!/usr/bin/env bash
./gradlew clean :package-diagram-cli:build
mv ./package-diagram-cli/build/libs/package-diagram-cli.jar ./work
