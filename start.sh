#!/bin/bash

GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"

#Uno dei file JAR delle dipendenze di questo applicativo è firmato con una firma differente
#da quella del JAR di questo applicativo, quindi, prima di eseguirlo, devo rimuovere tutti
#i file che identificano le firme.
zip -d ./build/install/tirocinio/lib/tirocinio-1.0.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
java -jar ./build/install/tirocinio/lib/tirocinio-1.0.jar
