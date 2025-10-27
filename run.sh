#!/bin/bash

# Script per eseguire l'applicazione

# Verifica se il progetto è stato compilato
if [ ! -d "bin" ]; then
    echo "Il progetto non è stato ancora compilato!"
    echo "Esegui prima: ./compile.sh"
    exit 1
fi

echo "Avvio dell'applicazione..."
java -cp bin com.flowchart.Main
