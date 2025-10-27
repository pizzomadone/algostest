#!/bin/bash

# Script per compilare il progetto

echo "Compilazione in corso..."

# Crea la directory per i file compilati
mkdir -p bin

# Compila tutti i file Java
javac -d bin -sourcepath src src/com/flowchart/Main.java src/com/flowchart/model/*.java src/com/flowchart/view/*.java src/com/flowchart/executor/*.java

if [ $? -eq 0 ]; then
    echo "Compilazione completata con successo!"
    echo "Per eseguire l'applicazione, usa: ./run.sh"
else
    echo "Errore durante la compilazione!"
    exit 1
fi
