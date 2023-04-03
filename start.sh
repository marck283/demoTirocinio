#!/bin/bash

GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"

#Uno dei file JAR delle dipendenze di questo applicativo è firmato con una firma differente
#da quella del JAR di questo applicativo, quindi, prima di eseguirlo, devo rimuovere tutti
#i file che identificano le firme.
sudo zip -d ./build/install/tirocinio/lib/tirocinio-1.0.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/audio"
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/images"
sudo mkdir -p "./src/main/resources/it/disi/unitn/input/video"
sudo mkdir -p "./src/main/resources/it/disi/unitn/output/partial"
java -jar ./build/install/tirocinio/lib/tirocinio-1.0.jar
# To be inserted in LoadBalancer file: io.grpc.internal.PickFirstLoadBalancerProvider