# 📄 PRD — MakeItWash

> **Versione:** 0.2 — Draft  
> **Team:** Endless-Team  
> **Engine:** Java + LibGDX  
> **Ultimo aggiornamento:** Marzo 2026

---

## 1. Visione del Prodotto

MakeItWash è un gioco gestionale 2D in cui il giocatore costruisce e ottimizza
un ristorante di sushi. Il nome è un riferimento diretto al lavaggio del riso,
prima fase della produzione. Il giocatore piazza macchinari, nastri trasportatori
e robot all'interno del ristorante per automatizzare la produzione di sushi,
soddisfare i clienti e sopravvivere alla crescente domanda giorno dopo giorno.

Lo stile visivo si ispira a **Factorio**: visuale top-down con effetto di
profondità/tridimensionalità, grafica 2D con isometria leggera o ombre che
simulano il volume.

Il gioco è progettato per essere **cross-platform fin dall'inizio**: Desktop
e Android condividono tutta la logica di gioco nel modulo `core/`, con solo
i layer di input e UI adattati per piattaforma.

---

## 2. Obiettivo di Gioco

Il giocatore deve superare un numero definito di **giornate lavorative**,
soddisfacendo tutti i clienti che entrano nel ristorante.

- Ogni giorno porta più clienti, ordini più complessi e meno tempo
- I clienti insoddisfatti riducono la reputazione
- I guadagni permettono di acquistare nuovi macchinari e ampliare il ristorante
- Il gioco termina (vittoria) completando tutte le giornate con reputazione > 0
- Il gioco termina (sconfitta) se la reputazione scende a zero

---

## 3. Meccaniche Core

### 3.1 — Costruzione
- Il ristorante è una **griglia** di celle (es. 20x15)
- Il giocatore si muove liberamente sulla griglia
- Interagisce con un **menu di costruzione** per piazzare elementi
- Gli elementi occupano 1 o più celle della griglia
- Gli elementi possono essere rimossi

### 3.2 — Produzione
La produzione di sushi segue una **catena di lavorazione**:

```
Riso → Lavaggio → Cottura → Assemblaggio → Impiattamento → Consegna al tavolo
```

Ogni step richiede una macchina specifica. I nastri trasportatori collegano
le macchine. I robot eseguono azioni specifiche (raccogliere, portare, piazzare).

### 3.3 — Clienti
- I clienti arrivano all'inizio di ogni giornata in ondate
- Ogni cliente ha un **ordine** (tipo di sushi) e una **pazienza** (timer)
- Se l'ordine non viene evaso in tempo → cliente insoddisfatto → -reputazione
- Più ordini soddisfatti = più denaro = più espansione

### 3.4 — Progressione
| Giornata | Difficoltà | Novità sbloccata |
|----------|-----------|-----------------|
| 1-3 | Tutorial | Nastri base, lavaggio riso |
| 4-6 | Facile | Robot portatori, cottura |
| 7-10 | Media | Assemblaggio automatico |
| 11-15 | Difficile | Ordini complessi, clienti VIP |
| 16+ | Hardcore | Tutto insieme + eventi casuali |

### 3.5 — Economia
- Ogni sushi consegnato genera **¥ (yen)**
- I macchinari costano yen per essere acquistati
- Espandere la griglia (comprare spazio) costa yen
- I robot e i nastri consumano **energia** da pagare nella bolletta dopo ogni giornata

### 3.6 — Input multi-piattaforma

| Azione | Desktop | Android |
|--------|---------|---------|
| Muovere il giocatore | WASD / Frecce | Joystick virtuale (touch) |
| Selezionare una cella | Click sinistro mouse | Tap |
| Aprire menu costruzione | E / Click destro | Pulsante HUD dedicato |
| Ruotare macchina | R | Pulsante rotazione nell'overlay |
| Zoom | Rotella mouse | Pinch to zoom |
| Pausa | ESC | Pulsante pausa HUD |

> **Principio di design:** nessuna meccanica deve richiedere hover o input
> non replicabili su touch. Le celle della griglia devono avere dimensione
> minima di **64x64px** per essere tappabili comodamente su mobile.

---

## 4. Entità del Gioco

### Entità Giocatore
| Entità | Descrizione |
|--------|-------------|
| `Player` | Personaggio controllato dal giocatore, si muove sulla griglia, piazza/rimuove elementi |

### Macchine (statiche sulla griglia)
| Entità | Descrizione |
|--------|-------------|
| `WashingMachine` | Lava il riso (già in codebase) |
| `RiceCooker` | Cuoce il riso |
| `SushiAssembler` | Assembla riso + ingrediente → sushi |
| `PlatingStation` | Impiattta il sushi |
| `DeliveryCounter` | Banco di consegna ai clienti |
| `Refrigerator` | Conserva gli ingredienti e i piatti già pronti |

### Trasporto
| Entità | Descrizione |
|--------|-------------|
| `ConveyorBelt` | Sposta oggetti da una cella all'altra automaticamente |
| `Robot` | Esegue azioni specifiche (raccogliere, depositare, smistare) |

### Clienti
| Entità | Descrizione |
|--------|-------------|
| `Customer` | Ha un ordine, una pazienza, si siede a un tavolo |
| `Table` | Cella dove il cliente aspetta l'ordine |

### Oggetti trasportabili
| Entità | Descrizione |
|--------|-------------|
| `Rice` | Riso (grezzo, lavato, cotto) |
| `Ingredient` | Pesce, avocado, ecc. |
| `SushiItem` | Prodotto finale (nigiri, maki, ecc.) |

---

## 5. Schermate (Screens)

| Screen | Descrizione |
|--------|-------------|
| `MenuScreen` | Schermata principale con titolo e play |
| `GameScreen` | Schermata di gioco principale |
| `BuildMenuScreen` | Overlay/pannello per selezionare cosa costruire |
| `PauseScreen` | Pausa con opzioni |
| `DayResultScreen` | Fine giornata: riepilogo guadagni e reputazione |
| `GameOverScreen` | Sconfitta in caso di bancarotta |
| `VictoryScreen` | Vittoria (opzionale, da definire) |

---

## 6. Requisiti Tecnici

- **Linguaggio:** Java 17+ (Desktop), Java 11+ compatibile (Android SDK)
- **Framework:** LibGDX 1.12.1
- **Build:** Gradle 9.x con moduli separati per piattaforma
- **Target:** Desktop (Windows/macOS/Linux), Android (API 21+, Android 5.0+)
- **Risoluzione base:** 1280x720 (desktop) — viewport adattivo su Android
- **Renderer:** SpriteBatch per sprite, ShapeRenderer per debug griglia
- **UI:** Scene2D (LibGDX) per menu e HUD — layout responsive per touch
- **Camera:** OrthographicCamera con zoom e pan
- **Salvataggio:** `Preferences` LibGDX (cross-platform, nessuna dipendenza esterna)

### Struttura moduli Gradle

```
MakeItWash/
├── core/          → logica di gioco (condivisa tra tutte le piattaforme)
├── desktop/       → launcher LWJGL3 per PC
└── android/       → activity Android + AndroidManifest.xml
```

### Requisiti Android
- `minSdkVersion`: 21 (Android 5.0 Lollipop)
- `targetSdkVersion`: 34
- Permessi richiesti: nessuno (gioco offline)
- Orientamento: **landscape forzato**
- Supporto controller fisici bluetooth: opzionale (v2.0)

---

## 7. Fuori Scope (v1.0)

- Multiplayer
- Versione iOS
- Editor di livelli
- Modalità endless
- Salvataggio cloud
- Controller fisici

---

## 8. Metriche di Successo

- Il giocatore completa almeno 5 giornate senza crash su Desktop e Android
- La produzione è ottimizzabile (non c'è un solo modo per vincere)
- Il feedback visivo rende chiaro lo stato di ogni macchina
- Il gioco è avviabile e giocabile in meno di 30 secondi dall'apertura
- Su Android: nessun lag su dispositivi con almeno 3GB RAM (2020+)
