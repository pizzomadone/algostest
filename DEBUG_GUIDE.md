# Guida al Debug e Sistemazione del Rendering

## 🎯 IL PROBLEMA

Il rendering dei blocchi IF non funziona correttamente. Probabilmente:
- I blocchi si sovrappongono
- Le posizioni non sono calcolate correttamente
- I rami non hanno la larghezza giusta
- Il LayoutEngine non funziona come dovrebbe

---

## 🔧 COME DEBUGGARE (PASSO PER PASSO)

### 1. **Verifica se il LayoutEngine viene chiamato**

Avvia l'applicazione dal terminale:
```bash
cd build
java com.flowchart.Main 2>&1 | tee debug.log
```

Quando aggiungi un blocco IF, dovresti vedere:
```
=== LAYOUT ENGINE CALLED ===
Pass 1: Calculating dimensions...
Root required width: XXX, height: YYY
Pass 2: Assigning positions...
Pass 3: Applying positions to blocks...
  Block START (Inizio) positioned at: 400,50
  Block END (Fine) positioned at: 400,150
=== LAYOUT ENGINE DONE ===
```

**SE NON VEDI QUESTO OUTPUT**: Il LayoutEngine non viene chiamato!
- Controlla `FlowchartTree.recalculateLayout()`
- Verifica che `layoutEngine.performLayout(root)` sia chiamato

---

### 2. **Analizza le posizioni calcolate**

Nel log, guarda le coordinate X,Y di ogni blocco:
```
Block DECISION (x>0) positioned at: 400,200
Block MERGE () positioned at: 450,300
```

**COSA CONTROLLARE:**
- ✅ I blocchi hanno coordinate crescenti in Y (vanno verso il basso)?
- ✅ I blocchi nei rami SI/NO hanno X diverso dal centro?
- ✅ Il merge point ha Y maggiore dei blocchi nei rami?

**SE LE COORDINATE SONO SBAGLIATE**: Il LayoutEngine calcola male!

---

### 3. **Controlla il buildLayoutTree**

Il problema potrebbe essere che il LayoutEngine non costruisce correttamente l'albero.

Aggiungi debug in `LayoutEngine.buildLayoutTree()`:
```java
private LayoutNode buildLayoutTree(Block block, Set<Block> visited) {
    System.out.println("Building tree for: " + block.getType());
    // ... resto del codice
}
```

**COSA CONTROLLARE:**
- Vengono visitati tutti i blocchi?
- I blocchi MERGE vengono inclusi?
- Le decisioni hanno i rami left e right?

---

### 4. **Verifica calculateDimensions**

Aggiungi debug alla fine di ogni metodo calculate*:
```java
private void calculateLinearDimensions(LayoutNode node) {
    // ... calcoli ...
    System.out.println("LINEAR: width=" + width + " height=" + height);
    node.setRequiredWidth(width);
    node.setRequiredHeight(height);
}
```

**SE LE DIMENSIONI SONO 0 O SBAGLIATE**: Il calcolo non funziona!

---

### 5. **Controlla assignPositions per i rami**

Il metodo `assignDecisionPositions` è critico. Aggiungi:
```java
private void assignDecisionPositions(LayoutNode node, int x, int y) {
    System.out.println("DECISION at " + x + "," + y);
    System.out.println("  Left branch width: " + (node.getLeftBranch() != null ? node.getLeftBranch().getRequiredWidth() : 0));
    System.out.println("  Right branch width: " + (node.getRightBranch() != null ? node.getRightBranch().getRequiredWidth() : 0));

    // ... calcoli delle posizioni ...

    System.out.println("  Left branch positioned at: " + leftX);
    System.out.println("  Right branch positioned at: " + rightX);
}
```

---

## 📚 COSA STUDIARE PER SISTEMARLO

### 1. **Concetti di Layout**
- **Layout a due passaggi**: Bottom-up (calcola dimensioni) + Top-down (assegna posizioni)
- **Tree traversal**: DFS (Depth-First Search) per visitare tutti i nodi
- **Coordinate systems**: Come Java Swing usa coordinate (0,0 = top-left)

### 2. **Debugging in Java**
- `System.out.println()` per debug veloce
- Java Debugger (jdb) per breakpoint
- IntelliJ IDEA debugger (se usi IDE)

### 3. **Strutture dati**
- Alberi (Tree): Come rappresentare gerarchie
- Grafi (Graph): FlowchartTree è un grafo, non un albero puro
- Visitatori (Visitor pattern): Per attraversare strutture complesse

---

## 🐛 IL VERO PROBLEMA (Probabile)

**SOSPETTO**: Il `LayoutEngine` che ho creato **NON STA FUNZIONANDO**.

Potrebbe essere che:
1. `buildLayoutTree()` non costruisce correttamente l'albero per le decisioni
2. `calculateDimensions()` non calcola le dimensioni giuste per i rami
3. `assignDecisionPositions()` posiziona male i rami

**IL BUG PIÙ PROBABILE**: Quando costruisco il layout tree per le decisioni,
probabilmente sto perdendo i blocchi nei rami perché la traversal del grafo
non funziona correttamente.

---

## 💡 SOLUZIONE ALTERNATIVA (PIÙ SEMPLICE)

Invece di usare il LayoutEngine complicato, potremmo tornare a un sistema
**più semplice e incrementale**:

1. **Ogni blocco IF conosce le sue dimensioni**:
   - Calcola larghezza massima dei due rami
   - Calcola altezza massima dei due rami

2. **Posizionamento diretto**:
   - Posiziona rombo al centro
   - Posiziona ramo SI a sinistra: `x - larghezzaRamoSI - offset`
   - Posiziona ramo NO a destra: `x + offset`
   - Posiziona merge sotto: `y + altezzaMassimaRami + spacing`

3. **Niente alberi complicati**:
   - Solo calcoli diretti sulle connessioni
   - Più facile da debuggare

---

## 🚀 COME PROCEDERE ORA

### OPZIONE A: Debug il LayoutEngine
1. Aggiungi tutti i print che ti ho mostrato
2. Esegui l'applicazione e salva il log
3. Analizza dove le coordinate vanno sbagliate
4. Sistema i calcoli nel metodo che sbaglia

### OPZIONE B: Torna al vecchio sistema
```bash
git checkout HEAD~2  # Torna a 2 commit fa
```
Poi sistema solo il vecchio `layoutBlockRecursive` aggiungendo:
- Calcolo corretto della larghezza dei rami
- Posizionamento X basato sulla larghezza del ramo

### OPZIONE C: Sistema manuale semplificato
Creo un sistema nuovo MOLTO PIÙ SEMPLICE senza LayoutEngine,
solo con calcoli diretti durante l'inserimento dei blocchi.

---

## 📞 DIMMI COSA PREFERISCI

1. Vuoi che aggiunga più debug e analizziamo insieme il log?
2. Vuoi che torni al vecchio sistema e lo sistemi in modo semplice?
3. Vuoi che crei un sistema nuovo completamente diverso e più semplice?
4. Vuoi una spiegazione video/interattiva del problema?

**Mandami uno screenshot o descrivi esattamente cosa vedi** quando inserisci
un IF dentro un IF, così capisco meglio il problema visivo!
