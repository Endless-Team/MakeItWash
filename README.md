# 🫧 MakeItWash

> Un gioco gestionale 2D a tema sushi, ispirato a Factorio — costruisci, automatizza, sopravvivi.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12.1-red)
![Platform](https://img.shields.io/badge/platform-Desktop%20%7C%20Android-blue)
![Status](https://img.shields.io/badge/status-in%20sviluppo-yellow)

---

## 🎮 Di cosa si tratta

**MakeItWash** è un gioco gestionale sviluppato da [Endless-Team](https://github.com/Endless-Team).
Il nome è un riferimento al lavaggio del riso, prima fase della produzione del sushi.
Il giocatore piazza macchinari, nastri trasportatori e robot per automatizzare
la produzione, soddisfare i clienti e sopravvivere a giornate sempre più intense.

---

## 📚 Documentazione

| Documento | Descrizione |
|-----------|-------------|
| [📄 PRD](doc/PRD.md) | Product Requirements Document — visione, meccaniche, entità, requisiti tecnici |
| [📋 User Stories](doc/UserStories.md) | Requisiti dal punto di vista del giocatore, con priorità e sprint |
| [📚 Guida LibGDX](doc/LibGDXGuide.md) | Guida pratica all'uso di LibGDX per questo progetto (Desktop + Android) |

---

## 🚀 Come avviarlo

### Requisiti

- Java 17+
- Gradle (o usa il wrapper incluso `gradlew.bat`)

### Desktop

```bash
git clone https://github.com/Endless-Team/MakeItWash.git
cd MakeItWash
./gradlew desktop:run
```

Su Windows:

```powershell
gradlew.bat desktop:run
```

### Android

```powershell
gradlew.bat android:installDebug
```

---

## 🏗️ Struttura del progetto

```
MakeItWash/
├── core/       → logica di gioco condivisa (entità, schermate, mondo)
├── desktop/    → launcher per PC (LWJGL3)
├── android/    → launcher per Android
└── doc/        → documentazione (PRD, User Stories, Guida LibGDX)
```

---

## 👥 Team

Sviluppato con ❤️ da **Endless-Team**
