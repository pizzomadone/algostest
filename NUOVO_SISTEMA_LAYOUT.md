# 🎯 NUOVO SISTEMA DI LAYOUT (SEMPLICE)

## ✅ **HO BUTTATO VIA IL CODICE COMPLICATO**

Il `LayoutEngine` con 2 passaggi e alberi complessi **NON FUNZIONAVA BENE**.

Ho creato un sistema **MOLTO PIÙ SEMPLICE** che calcola e posiziona direttamente.

---

## 📐 **COME FUNZIONA ORA**

### **Classe principale: `SimpleLayoutCalculator`**

Quando inserisci un blocco IF, il sistema:

### **1. Trova il merge point (pallino blu)**
```java
Block mergePoint = findMergePoint(decision);
```

### **2. Calcola dimensioni del ramo SINISTRO (SI)**
Visita tutti i blocchi nel ramo fino al pallino:
```
Ramo SI:
├─ Blocco1 (120x60)
├─ IF annidato (300x200)  ← Calcola ricorsivamente
└─ Blocco2 (120x60)
───────────────────────────
Totale: larghezza=300, altezza=320
```

### **3. Calcola dimensioni del ramo DESTRO (NO)**
Stesso procedimento:
```
Ramo NO:
├─ Blocco3 (120x60)
└─ Blocco4 (120x60)
───────────────────────────
Totale: larghezza=120, altezza=120
```

### **4. Posiziona i rami in base alle dimensioni**
```
Centro del rombo: x=400

Ramo SI (larghezza 300):
  X = 400 - 60 - 300/2 = 190

Ramo NO (larghezza 120):
  X = 400 + 60 - 120/2 = 400
```

### **5. Posiziona il pallino centrato sotto il più basso**
```
Altezza massima rami: max(320, 120) = 320

Pallino:
  X = 400 (centrato)
  Y = 200 + 320 + 40 = 560
```

---

## 🔍 **ESEMPIO CON IF ANNIDATO**

```
START
  ↓
IF outer (x=400, y=200)
├─ SI (x=190)
│  ├─ Blocco A (x=190, y=300)
│  ├─ IF inner (x=190, y=400)
│  │  ├─ SI (x=130)
│  │  │  └─ Blocco B (x=130, y=500)
│  │  ├─ NO (x=250)
│  │  │  └─ Blocco C (x=250, y=500)
│  │  └─ Merge inner (x=190, y=600)
│  └─ Blocco D (x=190, y=700)
├─ NO (x=460)
│  └─ Blocco E (x=460, y=300)
└─ Merge outer (x=400, y=800)
  ↓
END
```

---

## 📊 **DIFFERENZE CON IL VECCHIO SISTEMA**

| **Aspetto** | **Vecchio (LayoutEngine)** | **Nuovo (SimpleLayoutCalculator)** |
|-------------|----------------------------|-------------------------------------|
| Complessità | 3 passaggi + alberi | 1 passaggio ricorsivo |
| Righe codice | ~600 | ~400 |
| Bug | Tanti | Dovrebbero essere meno |
| Debug | Difficile | Facile (print su ogni step) |
| Capibilità | 😵 | 😊 |

---

## 🐛 **DEBUG ATTIVO**

Il nuovo sistema stampa TUTTO quello che fa:

```
=== SIMPLE LAYOUT STARTING ===
Laying out START (Inizio) at 400,50
  No special handling
Laying out DECISION (x>0) at 400,200
  DECISION BLOCK - laying out branches
  Left branch: 300x320
  Right branch: 120x120
  Left branch X: 190
  Right branch X: 460
    Branch block PROCESS at 190,300
  Merge point at: 400,560
=== SIMPLE LAYOUT DONE ===
```

**Così puoi vedere esattamente dove ogni blocco viene posizionato!**

---

## 🚀 **COME TESTARLO**

### 1. **Compila:**
```bash
javac -d build -sourcepath src src/com/flowchart/Main.java
```

### 2. **Esegui con output:**
```bash
cd build
java com.flowchart.Main 2>&1 | tee ../layout_log.txt
```

### 3. **Prova questi scenari:**
- Crea un IF
- Aggiungi un blocco nel ramo SI
- Aggiungi un IF dentro il ramo SI
- Aggiungi blocchi nei rami dell'IF annidato
- Guarda `layout_log.txt` per vedere le coordinate

### 4. **Controlla le coordinate:**
Le coordinate devono essere:
- ✅ X diverso per rami SI e NO
- ✅ Y crescente verso il basso
- ✅ Pallino centrato sotto i rami
- ✅ Nessuna sovrapposizione

---

## 🔧 **SE ANCORA NON FUNZIONA**

### Problema: I blocchi si sovrappongono
→ Aumenta `BRANCH_HORIZONTAL_OFFSET` in `LayoutConstants.java`

### Problema: I rami sono troppo vicini
→ Aumenta `MIN_BRANCH_SPACING` in `LayoutConstants.java`

### Problema: Il pallino è troppo vicino
→ Aumenta `MERGE_POINT_SPACING` in `LayoutConstants.java`

### Problema: I blocchi sono troppo attaccati verticalmente
→ Aumenta `VERTICAL_SPACING` in `LayoutConstants.java`

---

## 💡 **COSA HO IMPARATO**

1. **Semplice è meglio**: Un sistema complicato è più difficile da debuggare
2. **Logging è essenziale**: Senza vedere cosa fa il codice, è impossibile capire i bug
3. **Calcoli diretti funzionano**: Non serve creare strutture intermedie complesse

---

## ✨ **QUESTO DOVREBBE FUNZIONARE MOLTO MEGLIO**

Il sistema è:
- ✅ Più semplice
- ✅ Più leggibile
- ✅ Più debuggabile
- ✅ Con logging dettagliato

**Prova e fammi sapere cosa vedi nel log!**

Se ancora non funziona, manda il contenuto di `layout_log.txt` e vediamo
esattamente dove le coordinate sono sbagliate.
