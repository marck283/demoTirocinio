#!/bin/bash

GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"
zip -d ./build/install/tirocinio/lib/tirocinio-1.0.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
java -jar ./build/install/tirocinio/lib/tirocinio-1.0.jar
