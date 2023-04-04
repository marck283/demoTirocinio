#!/bin/bash

GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"

#Uno dei file JAR delle dipendenze di questo applicativo è firmato con una firma differente
#da quella del JAR di questo applicativo, quindi, prima di eseguirlo, devo rimuovere tutti
#i file che identificano le firme.
#sudo zip -d ./build/install/tirocinio/lib/tirocinio-1.0.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'

#Creo le cartelle e i file necessari qui per non avere problemi con il sistema operativo.
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/audio"
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/images"
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/video"
sudo mkdir -p "./src/main/resources/it/disi/unitn/output/partial"
sudo touch "./inputFile.txt" #Modifica il timestamp del file inputFile.txt, creandolo se necessario.
java -jar ./build/install/tirocinio-shadow/lib/tirocinio-shadow-1.0.jar