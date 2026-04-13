# 🍣 SushiDays — libGDX

Gioco casual di gestione ristorante sushi per **PC (Desktop)** e **Android**.
Sviluppato con [libGDX 1.12.1](https://libgdx.com/) e Java 17.

---

## Requisiti

| Tool | Versione minima |
|------|----------------|
| JDK  | 17 |
| Android SDK | API 24+ (solo build Android) |
| Android NDK | r21+ (solo build Android) |
| Gradle | 8.4 (incluso nel wrapper) |

---

## Struttura del progetto

```
sushidays/
├── core/                    # Codice di gioco (piattaforma-indipendente)
│   └── src/com/sushidays/
│       ├── SushiDaysGame.java          # Classe principale
│       ├── screens/                    # Tutte le schermate
│       │   ├── BaseScreen.java
│       │   ├── MainMenuScreen.java
│       │   ├── GameScreen.java
│       │   ├── PauseScreen.java
│       │   ├── DayEndScreen.java
│       │   ├── MissionScreen.java
│       │   ├── ShopScreen.java
│       │   └── OptionsScreen.java
│       ├── systems/                    # Sistemi di gioco
│       │   ├── DayManager.java
│       │   ├── CustomerManager.java
│       │   ├── CookingSystem.java
│       │   ├── InventorySystem.java
│       │   ├── MissionSystem.java
│       │   ├── RecipeRegistry.java
│       │   ├── UpgradeRegistry.java
│       │   └── AudioManager.java
│       ├── entities/                   # Modelli dati
│       │   ├── Customer.java
│       │   ├── Dish.java
│       │   ├── Ingredient.java
│       │   ├── Recipe.java
│       │   ├── CookingStep.java
│       │   └── Upgrade.java
│       └── utils/
│           ├── Constants.java
│           ├── GameState.java
│           └── AssetLoader.java
├── desktop/                 # Launcher PC
│   └── src/com/sushidays/DesktopLauncher.java
├── android/                 # Launcher Android
│   ├── src/com/sushidays/AndroidLauncher.java
│   └── AndroidManifest.xml
├── assets/                  # Asset condivisi
│   ├── sounds/              # File audio (.ogg) — opzionali, il gioco funziona senza
│   └── fonts/               # Font BitmapFont — opzionali
├── build.gradle             # Root build
├── settings.gradle
└── gradle.properties
```

---

## Come avviare (Desktop)

```bash
# Clona / estrai il progetto
cd sushidays

# Avvia direttamente con Gradle (scarica le dipendenze la prima volta)
./gradlew desktop:run

# Oppure compila un fat-jar eseguibile
./gradlew desktop:jar
java -jar desktop/build/libs/sushidays-desktop.jar
```

Su **Windows** usa `gradlew.bat` al posto di `./gradlew`.

Su **Mac Apple Silicon (M1/M2/M3)** il gioco funziona automaticamente con i natives arm64. Il codice configura automaticamente `useGlfwAsync()` per evitare il problema del primo thread.

---

## Come buildare per Android

1. Assicurati di avere Android SDK installato e `ANDROID_HOME` configurato.
2. Esegui:

```bash
./gradlew android:assembleDebug
```

L'APK di debug si trova in `android/build/outputs/apk/debug/android-debug.apk`.

Per un APK di release firmato:
```bash
./gradlew android:assembleRelease
```

---

## Asset audio (opzionali)

Il gioco funziona senza file audio — `AssetLoader` li carica solo se presenti.
Per aggiungere la musica, metti file `.ogg` in `assets/sounds/`:

| File             | Utilizzo                |
|------------------|------------------------|
| menu.ogg         | Musica menu principale  |
| game.ogg         | Musica giornata normale |
| rush.ogg         | Musica rush hour        |
| coin.ogg         | Sfx moneta raccolta     |
| cut.ogg          | Sfx taglio              |
| bell.ogg         | Sfx cliente arrivato    |
| error.ogg        | Sfx errore step         |
| success.ogg      | Sfx piatto completato   |
| button.ogg       | Sfx click pulsante      |

---

## Gameplay rapido

1. **Menu Principale** → premi GIOCA
2. **Mission Screen** → seleziona una missione a sinistra, esegui il mini-gioco a destra, ritira la ricompensa, poi premi "INIZIA LA GIORNATA!"
3. **Game Screen** → clicca una ricetta nel menu a sinistra per iniziare la preparazione, esegui gli step di cucina, consegna i piatti cliccando sui clienti
4. **Fine giornata** → riepilogo automatico → vai al negozio o al giorno successivo
5. **Negozio** → compra upgrade e cosmetici con le monete guadagnate

### Controlli Desktop
| Azione | Input |
|--------|-------|
| Selezionare ricetta | Click sul nome ricetta |
| Step SLICE | Click + trascina veloce |
| Step ROLL | Click + movimento circolare |
| Step PRESS | Click + tieni premuto |
| Step SHAKE | Click + movimenti rapidi |
| Step POUR | Click + trascina verticalmente |
| Step WAIT | Click al momento giusto |
| Consegnare piatto | Click sul cliente |
| Pausa | ESC / pulsante II |

---

## Aggiungere nuove ricette

Apri `RecipeRegistry.java` e aggiungi una nuova `Recipe` alla lista statica:

```java
Recipe miaRicetta = new Recipe(
    "mia_ricetta_id",   // ID univoco
    "Nome del Piatto",  // nome visualizzato
    25,                 // prezzo base in monete
    5,                  // giorno di sblocco
    new Color(...)      // colore del piatto
)
.addStep(new CookingStep(StepType.SLICE, "Taglia il pesce!", Type.SALMON))
.addStep(new CookingStep(StepType.PRESS, "Compatta il riso!", Type.RICE));

ALL_RECIPES.add(miaRicetta);
```

---

## Aggiungere nuovi upgrade

Apri `UpgradeRegistry.java` e aggiungi alla lista statica:

```java
ALL.add(new Upgrade(
    "mio_upgrade",          // ID univoco (usato in GameState)
    "Nome Upgrade",         // nome visualizzato
    "Descrizione effetto",  // descrizione
    200,                    // costo in monete
    UpgradeType.FUNCTIONAL, // o COSMETIC
    3                       // giorno di sblocco
));
```

Poi aggiungi la logica dell'effetto in `GameState.recalculateUpgrades()`.

---

## Roadmap futura

- [ ] Sostituire ShapeRenderer con TextureAtlas e sprite veri
- [ ] Animazioni clienti (walk cycle)
- [ ] Effetti particellari per cottura
- [ ] Sistema di achievements
- [ ] Leaderboard locale
- [ ] Supporto multi-lingua (i18n)
- [ ] Più tipi di ristorante (temaki bar, ramen, onigiri)

---

## Note sul Gradle Wrapper

Il file `gradle/wrapper/gradle-wrapper.jar` non è incluso nello ZIP perché binario.
Hai due opzioni per ottenerlo:

**Opzione A — con Gradle già installato (raccomandato):**
```bash
cd sushidays
gradle wrapper --gradle-version=8.4
```

**Opzione B — download diretto:**
```bash
cd sushidays/gradle/wrapper
curl -L "https://services.gradle.org/distributions/gradle-8.4-bin.zip" -o gradle-8.4-bin.zip
# oppure usa IntelliJ IDEA / Android Studio che lo scarica automaticamente
```

**Opzione C — usa IntelliJ IDEA o Android Studio:**
Apri il progetto → File → Open → seleziona la cartella `sushidays` → 
IntelliJ scarica automaticamente il wrapper e tutte le dipendenze.
