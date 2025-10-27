# Editor di Diagrammi a Blocchi

Un'applicazione Java con interfaccia grafica per creare ed eseguire algoritmi utilizzando diagrammi a blocchi (flowchart).

## Caratteristiche

- **Interfaccia grafica intuitiva** con supporto drag & drop
- **Blocchi disponibili:**
  - Inizio (START)
  - Fine (END)
  - Processo (operazioni e assegnazioni)
  - Decisione (condizioni if/else)
  - Input (lettura dati dall'utente)
  - Output (visualizzazione dati)
  - Ciclo FOR (iterazione con contatore)
  - Ciclo WHILE (ripetizione con condizione precondizionale)
  - Ciclo DO-WHILE (ripetizione con condizione postcondizionale)
- **Modalità di esecuzione:**
  - Esecuzione completa (tutti i blocchi in sequenza)
  - Esecuzione passo-passo (un blocco alla volta)
- **Visualizzazione in tempo reale:**
  - **Evidenziazione migliorata**: i blocchi in esecuzione si illuminano in giallo brillante con bordo oro spesso
  - Output dell'esecuzione
  - Tabella delle variabili con i loro valori in tempo reale
- **Editor interattivo:**
  - Drag & drop per spostare i blocchi
  - Click destro per creare connessioni tra blocchi
  - Doppio click per modificare il testo dei blocchi
  - **Testo leggibile**: font bold 13pt per massima chiarezza
  - **Icone identificative**: blocchi Input/Output mostrano "I" o "O" per identificazione immediata
  - **Bordi spessi**: tutti i blocchi hanno bordi 2px per migliore visibilità

## Requisiti

- Java Development Kit (JDK) 8 o superiore
- Sistema operativo: Linux, macOS, o Windows

## Installazione e Compilazione

### Linux/macOS

1. Clona o scarica il repository
2. Apri un terminale nella directory del progetto
3. Compila il progetto:
```bash
./compile.sh
```

### Windows

1. Apri il Prompt dei comandi nella directory del progetto
2. Compila il progetto:
```cmd
javac -d bin -sourcepath src src/com/flowchart/Main.java src/com/flowchart/model/*.java src/com/flowchart/view/*.java src/com/flowchart/executor/*.java
```

## Esecuzione

### Linux/macOS
```bash
./run.sh
```

### Windows
```cmd
java -cp bin com.flowchart.Main
```

## Come Usare l'Applicazione

### 1. Creare Blocchi

Clicca sui pulsanti colorati nella palette a sinistra per aggiungere blocchi al canvas:
- **Inizio** (verde scuro): punto di partenza dell'algoritmo
- **Fine** (rosso scuro): punto di termine dell'algoritmo
- **Processo** (blu): esegue operazioni matematiche o assegnazioni
- **Decisione** (arancione): valuta una condizione (if/else)
- **Input** (viola): legge un valore dall'utente
- **Output** (viola scuro): mostra un valore all'utente
- **Ciclo FOR** (teal): ripete il corpo per un numero definito di volte
- **Ciclo WHILE** (verde acqua): ripete il corpo finché la condizione è vera
- **Ciclo DO-WHILE** (verde scuro): esegue il corpo almeno una volta, poi ripete finché la condizione è vera

### 2. Posizionare e Modificare i Blocchi

- **Spostare un blocco**: click sinistro e trascina
- **Modificare il testo**: doppio click sul blocco
- **Selezionare un blocco**: click sinistro
- **Eliminare un blocco**: selezionalo e clicca su "Elimina"

### 3. Collegare i Blocchi

1. Click destro sul blocco sorgente (da cui parte la connessione)
2. Tieni premuto e trascina verso il blocco destinazione
3. Rilascia il mouse sul blocco destinazione
4. Per i blocchi **Decisione** e **Cicli**, scegli l'etichetta:
   - **Decisione**: SI, NO, o personalizza
   - **Cicli**: CORPO (SI) per il corpo del ciclo, ESCI (NO) per uscire dal ciclo

### 4. Eseguire il Diagramma

Nel pannello di controllo a destra:
- **Esegui Tutto**: esegue l'intero algoritmo automaticamente
- **Passo-Passo**:
  - Primo click: inizia l'esecuzione
  - Click successivi: esegue un blocco alla volta
- **Stop**: ferma l'esecuzione in corso
- **Pulisci Output**: cancella l'output dell'esecuzione

Durante l'esecuzione:
- **Il blocco corrente si illumina**: sfondo giallo brillante con bordo oro spesso (5px) per massima visibilità
- L'output appare nell'area "Output Esecuzione"
- Le variabili e i loro valori appaiono nella tabella "Variabili" in tempo reale
- I blocchi Input/Output mostrano chiaramente "I" o "O" per identificazione immediata

## Sintassi dei Blocchi

### Blocco Processo
Esegue operazioni e assegnazioni di variabili:
```
x = 10
y = x + 5
risultato = x * y
```

Operatori supportati: `+`, `-`, `*`, `/`

### Blocco Decisione
Valuta condizioni booleane:
```
x > 0
y == 5
x != y
x >= 10
y <= 100
x < y
```

Operatori di confronto supportati: `>`, `<`, `>=`, `<=`, `==`, `!=`

**Operatori logici** (NUOVO!):
```
x > 0 AND x < 10        (entrambe le condizioni devono essere vere)
x == 0 OR x == 10       (almeno una delle condizioni deve essere vera)
x > 5 AND y < 3         (combina più condizioni con AND)
x < 0 OR x > 100        (vera se x è negativo O maggiore di 100)
```

Operatori logici supportati:
- **AND**: entrambe le condizioni devono essere vere
- **OR**: almeno una delle condizioni deve essere vera
- Si possono combinare più condizioni: `x > 0 AND x < 10 AND x != 5`

### Blocco Input
Legge un valore dall'utente:
```
x          (legge un valore nella variabile x)
numero     (legge un valore nella variabile numero)
```

L'applicazione tenterà di convertire automaticamente i numeri (interi o decimali).

### Blocco Output
Mostra un valore all'utente:
```
x          (mostra il valore della variabile x)
"Hello"    (mostra il testo Hello)
risultato  (mostra il valore della variabile risultato)
```

### Blocco Ciclo FOR
Ripete il corpo per un numero definito di volte:
```
i=0; i<10; i=i+1    (inizializza i a 0, continua mentre i<10, incrementa i di 1 ad ogni iterazione)
```

Sintassi: `inizializzazione; condizione; incremento`
- **inizializzazione**: eseguita una sola volta all'inizio (es: `i=0`)
- **condizione**: valutata prima di ogni iterazione (es: `i<10`)
- **incremento**: eseguito alla fine di ogni iterazione (es: `i=i+1`)

Il ciclo FOR ha due connessioni:
- **CORPO (SI)**: verso il blocco da eseguire ripetutamente
- **ESCI (NO)**: verso il blocco successivo quando il ciclo termina

### Blocco Ciclo WHILE
Ripete il corpo finché la condizione è vera:
```
i < 10          (continua a ripetere finché i è minore di 10)
x != 0          (continua finché x è diverso da 0)
```

La condizione viene valutata **prima** di ogni iterazione. Se è falsa dall'inizio, il corpo non viene mai eseguito.

Il ciclo WHILE ha due connessioni:
- **CORPO (SI)**: verso il blocco da eseguire ripetutamente
- **ESCI (NO)**: verso il blocco successivo quando la condizione diventa falsa

### Blocco Ciclo DO-WHILE
Esegue il corpo almeno una volta, poi ripete finché la condizione è vera:
```
i < 10          (continua a ripetere finché i è minore di 10)
```

La condizione viene valutata **dopo** ogni iterazione. Il corpo viene eseguito almeno una volta, anche se la condizione è falsa dall'inizio.

Il ciclo DO-WHILE ha due connessioni:
- **CORPO (SI)**: verso il blocco da eseguire ripetutamente
- **ESCI (NO)**: verso il blocco successivo quando la condizione diventa falsa

**Nota importante sui cicli**: Per creare un ciclo funzionante, devi:
1. Creare il blocco ciclo
2. Collegarlo al primo blocco del corpo (etichetta: CORPO/SI)
3. Collegare l'ultimo blocco del corpo di nuovo al blocco ciclo (questa connessione farà ripetere)
4. Collegare il blocco ciclo al blocco successivo (etichetta: ESCI/NO) per uscire quando la condizione è falsa

## Esempi di Algoritmi

### Esempio 1: Somma di Due Numeri
1. Aggiungi un blocco **Inizio**
2. Aggiungi un blocco **Input** con testo `a`
3. Aggiungi un blocco **Input** con testo `b`
4. Aggiungi un blocco **Processo** con testo `somma = a + b`
5. Aggiungi un blocco **Output** con testo `somma`
6. Aggiungi un blocco **Fine**
7. Collega i blocchi in sequenza (click destro → trascina → rilascia)
8. Clicca su "Esegui Tutto" o "Passo-Passo"

### Esempio 2: Numero Positivo o Negativo
1. Aggiungi un blocco **Inizio**
2. Aggiungi un blocco **Input** con testo `numero`
3. Aggiungi un blocco **Decisione** con testo `numero > 0`
4. Aggiungi due blocchi **Output**:
   - Uno con testo `"Positivo"`
   - Uno con testo `"Negativo o Zero"`
5. Aggiungi un blocco **Fine**
6. Collega:
   - Inizio → Input
   - Input → Decisione
   - Decisione → Output "Positivo" (etichetta: SI)
   - Decisione → Output "Negativo o Zero" (etichetta: NO)
   - Entrambi gli Output → Fine
7. Esegui l'algoritmo

### Esempio 3: Somma dei Primi N Numeri (con Ciclo FOR)
1. Aggiungi un blocco **Inizio**
2. Aggiungi un blocco **Input** con testo `n`
3. Aggiungi un blocco **Processo** con testo `somma = 0`
4. Aggiungi un blocco **Ciclo FOR** con testo `i=1; i<=n; i=i+1`
5. Aggiungi un blocco **Processo** con testo `somma = somma + i`
6. Aggiungi un blocco **Output** con testo `somma`
7. Aggiungi un blocco **Fine**
8. Collega:
   - Inizio → Input
   - Input → Processo (somma = 0)
   - Processo → Ciclo FOR
   - Ciclo FOR → Processo (somma = somma + i) con etichetta **SI**
   - Processo (somma = somma + i) → Ciclo FOR (chiude il ciclo)
   - Ciclo FOR → Output con etichetta **NO**
   - Output → Fine
9. Esegui l'algoritmo e inserisci un numero (es: 10)
10. Il risultato sarà la somma 1+2+3+...+10 = 55

### Esempio 4: Contatore con WHILE
1. Aggiungi un blocco **Inizio**
2. Aggiungi un blocco **Processo** con testo `i = 0`
3. Aggiungi un blocco **Ciclo WHILE** con testo `i < 5`
4. Aggiungi un blocco **Output** con testo `i`
5. Aggiungi un blocco **Processo** con testo `i = i + 1`
6. Aggiungi un blocco **Fine**
7. Collega:
   - Inizio → Processo (i = 0)
   - Processo → Ciclo WHILE
   - Ciclo WHILE → Output con etichetta **SI**
   - Output → Processo (i = i + 1)
   - Processo (i = i + 1) → Ciclo WHILE (chiude il ciclo)
   - Ciclo WHILE → Fine con etichetta **NO**
8. Esegui per vedere i numeri da 0 a 4

## Menu

### File
- **Nuovo**: Crea un nuovo diagramma vuoto
- **Esci**: Chiude l'applicazione

### Aiuto
- **Istruzioni**: Mostra le istruzioni dettagliate
- **Info**: Informazioni sull'applicazione

## Struttura del Progetto

```
algostest/
├── src/
│   └── com/
│       └── flowchart/
│           ├── Main.java              # Classe principale
│           ├── model/                 # Modello dei dati
│           │   ├── Block.java         # Classe blocco
│           │   ├── BlockType.java     # Tipi di blocchi
│           │   ├── Connection.java    # Connessioni tra blocchi
│           │   └── FlowchartModel.java # Modello del diagramma
│           ├── view/                  # Interfaccia grafica
│           │   ├── BlockPalette.java  # Palette dei blocchi
│           │   ├── ControlPanel.java  # Pannello di controllo
│           │   ├── FlowchartCanvas.java # Canvas per disegnare
│           │   └── MainFrame.java     # Finestra principale
│           └── executor/              # Esecuzione
│               └── FlowchartExecutor.java # Esecutore algoritmi
├── bin/                               # File compilati (.class)
├── compile.sh                         # Script di compilazione
├── run.sh                             # Script di esecuzione
└── README.md                          # Questo file

```

## Limitazioni

- **Cicli**: L'implementazione attuale supporta cicli creando connessioni circolari, ma può richiedere un blocco decisione per uscire dal ciclo. Attenzione ai cicli infiniti!
- **Valutazione espressioni**: L'interprete supporta espressioni matematiche semplici con un solo operatore alla volta
- **Tipi di dati**: Supporta numeri (interi e decimali) e stringhe
- **Funzioni**: Non sono supportate funzioni personalizzate

## Suggerimenti

- **Organizzazione**: Disponi i blocchi in modo chiaro dall'alto verso il basso per una migliore leggibilità
- **Nomi variabili**: Usa nomi significativi per le variabili (es: `somma` invece di `x`)
- **Testing**: Usa l'esecuzione passo-passo per debugging e capire il flusso dell'algoritmo
- **Connessioni**: Assicurati che ogni blocco (tranne Fine) abbia una connessione in uscita
- **Blocchi Decisione**: Devono avere sempre due connessioni in uscita (SI e NO)

## Risoluzione Problemi

### L'applicazione non si compila
- Verifica di avere JDK installato: `java -version` e `javac -version`
- Assicurati di essere nella directory corretta del progetto
- Su Linux/macOS, verifica che gli script siano eseguibili: `chmod +x *.sh`

### L'esecuzione non funziona
- Assicurati che ci sia un blocco **Inizio** nel diagramma
- Verifica che tutti i blocchi siano collegati correttamente
- Controlla che la sintassi nei blocchi sia corretta

### I blocchi non si vedono
- Prova a fare scroll nel canvas
- Verifica che i blocchi siano stati effettivamente aggiunti (dovrebbero apparire nella posizione predefinita in alto a sinistra)

## Licenza

Questo progetto è stato creato per scopi educativi.

## Contributi

Feedback e suggerimenti sono benvenuti!
