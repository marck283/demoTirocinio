#!/bin/bash

if [ $# -ge 1 ]; then
	#java -jar "$1"/tirocinio.main.jar
	GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"
	./gradlew run
else
	echo "Usage: ./start.sh <folder of module's jar file>"
fi
