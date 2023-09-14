#!/bin/bash

#NOTA: la creazione di cartelle in un sistema Linux non dovrebbe essere permessa per utenti che non possono eseguire
#comandi bash con privilegi elevati, quindi, prima di eseguire questo script, l'utente si deve assicurare di poter eseguire
#tali comandi.

export GOOGLE_APPLICATION_CREDENTIALS="./src/main/application_default_credentials.json"
if [ ! -f $GOOGLE_APPLICATION_CREDENTIALS ]; then
  echo "Errore: file application_default_credentials.json non esistente.">&2;
fi

#Uno dei file JAR delle dipendenze di questo applicativo è firmato con una firma differente
#da quella del JAR di questo applicativo, quindi, prima di eseguirlo, devo rimuovere tutti
#i file che identificano le firme.
#sudo zip -d ./build/install/tirocinio/lib/tirocinio-1.0.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'

java -jar ./build/install/tirocinio-shadow/lib/tirocinio-shadow-1.0.jar $1 $2 $3 $4
