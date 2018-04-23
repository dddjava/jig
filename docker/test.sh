#!/usr/bin/env bash

git clone https://github.com/irof/Jig.git
cd Jig
./gradlew build
java -jar package-diagram-cli/build/libs/package-diagram-cli.jar --output.diagram.name=package-diagram.png

