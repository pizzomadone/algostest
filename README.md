# Editor Diagrammi a Blocchi - Versione 2.0

**Sistema di Layout Verticale Automatico**

## 🚀 Avvio Rapido

```bash
# Compila
javac -d bin -sourcepath src src/com/flowchart/Main.java

# Avvia
java -cp bin com.flowchart.Main
```

## ✨ Come Usare

### Creare un Algoritmo
1. **Click sulla freccia** tra INIZIO e FINE
2. **Scegli tipo blocco** dal menu (Processo, Input, Output, etc.)
3. **Inserisci testo** → Il blocco si inserisce automaticamente!
4. **Ripeti** cliccando sulle altre frecce

### Modificare/Eliminare
- **Doppio click** su blocco → modifica testo
- **Click + Delete** → elimina blocco (layout si sistema automaticamente)

### Eseguire
- 🟢 **Esegui Tutto**: esegue l'intero algoritmo
- 🔵 **Passo-Passo**: esegue un blocco alla volta
- 🔴 **Stop**: ferma l'esecuzione

### Zoom
- Pulsanti **+** e **-** in basso a destra

## 💡 Esempio Rapido

1. Click freccia → Scegli "Input" → Scrivi: `x`
2. Click freccia → Scegli "Processo" → Scrivi: `y = x * 2`
3. Click freccia → Scegli "Output" → Scrivi: `y`
4. Click "Esegui Tutto" → Inserisci un numero → Vedi il risultato!

## 📝 Sintassi

- **Processo**: `x = 10`, `y = x + 5`
- **Decisione**: `x > 0`, `y == 5`, `x > 0 AND y < 10`
- **Input**: `nome`, `età`
- **Output**: `x`, `"Risultato: " + x`
- **Ciclo FOR**: `i = 0; i < 10; i++`
- **Ciclo WHILE**: `x < 100`

## 🎯 Caratteristiche

✓ Layout verticale automatico
✓ Inserimento tramite click su frecce
✓ Auto-spacing intelligente
✓ Zoom integrato
✓ Esecuzione passo-passo
✓ Visualizzazione variabili in tempo reale
