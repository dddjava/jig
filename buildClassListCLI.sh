#!/usr/bin/env bash
./gradlew clean :class-list-cli:build
mv ./class-list-cli/build/libs/class-list-cli.jar ./work
