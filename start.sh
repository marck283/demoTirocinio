#!/bin/bash

export GOOGLE_APPLICATION_CREDENTIALS="./application_default_credentials.json"
if [ ! -f $GOOGLE_APPLICATION_CREDENTIALS ]; then
  echo "Errore: file application_default_credentials.json non esistente.">&2;
  exit 1;
fi

if [ $# -ne 5 ]; then
  echo "Errore: il numero di argomenti forniti a questo script non puo' essere diverso da 5.">&2;
  echo "Utilizzo: ./start.sh <percorso del file JAR del programma> <valore booleano \"true\" o \"false\"> <larghezza>
  <altezza> <valore booleano \"true\" o \"false\">. Si noti che il primo valore booleano indica al programma se utilizzare
  una rete neurale o meno, mentre il secondo indica se l'utente intenda eseguire il programma usando una versione di
  FFmpeg appena compilata.">&2;
  echo "NOTA: sia il valore dell'altezza che quello della larghezza non possono essere negativi.">&2;
fi

java -jar ./tirocinio-shadow-1.0.jar $1 $2 $3 $4 $5
