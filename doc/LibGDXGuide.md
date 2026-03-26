# 📚 Guida LibGDX per MakeItWash

> Guida pratica all'uso di LibGDX per un gioco gestionale 2D.  
> Calibrata sulla struttura del progetto MakeItWash e sul livello del team (Java esperti, LibGDX zero).

---

## Indice

1. [Come funziona LibGDX](#1-come-funziona-libgdx)
2. [Il ciclo di vita del gioco](#2-il-ciclo-di-vita-del-gioco)
3. [Screen — gestione delle schermate](#3-screen--gestione-delle-schermate)
4. [SpriteBatch — disegnare texture](#4-spritebatch--disegnare-texture)
5. [Texture e TextureRegion](#5-texture-e-textureregion)
6. [OrthographicCamera — la telecamera](#6-orthographiccamera--la-telecamera)
7. [ShapeRenderer — debug e forme geometriche](#7-shaperenderer--debug-e-forme-geometriche)
8. [Input — tastiera, mouse, touch](#8-input--tastiera-mouse-touch)
9. [Scene2D — UI e HUD](#9-scene2d--ui-e-hud)
10. [BitmapFont — testo a schermo](#10-bitmapfont--testo-a-schermo)
11. [AssetManager — caricare le risorse](#11-assetmanager--caricare-le-risorse)
12. [La griglia di gioco — implementazione pratica](#12-la-griglia-di-gioco--implementazione-pratica)
13. [Gestione della memoria — dispose()](#13-gestione-della-memoria--dispose)
14. [Struttura consigliata per MakeItWash](#14-struttura-consigliata-per-makeitwash)

---

## 1. Come funziona LibGDX

LibGDX è un framework che astrae le differenze tra piattaforme (desktop, Android, iOS).
Invece di chiamare direttamente OpenGL, usi classi di alto livello come `SpriteBatch` e `Texture`.

Il progetto è diviso in due moduli:
- **`core/`** → tutta la logica di gioco (indipendente dalla piattaforma)
- **`desktop/`** → solo il launcher, crea la finestra e avvia il core

**Regola fondamentale:** tutto il codice di gioco va in `core/`. Non mettere mai
codice di gioco in `desktop/`.

---

## 2. Il ciclo di vita del gioco

`MainGame` estende `Game` e ha questi metodi:

```java
public class MainGame extends Game {

    @Override
    public void create() {
        // Chiamato UNA VOLTA all'avvio
        // Inizializza risorse globali (AssetManager, font, ecc.)
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        // Chiamato OGNI FRAME (tipicamente 60 fps)
        // Non scrivere logica qui: delega alla Screen corrente
        super.render(); // obbligatorio: chiama screen.render()
    }

    @Override
    public void dispose() {
        // Chiamato alla chiusura del gioco
        // Libera le risorse globali
    }
}
```

---

## 3. Screen — gestione delle schermate

Ogni schermata (menu, gioco, pausa) è una classe separata che implementa `Screen`
o estende `ScreenAdapter` (più comodo: implementa solo i metodi che ti servono).

```java
public class GameScreen extends ScreenAdapter {

    private final MainGame game; // riferimento per cambiare screen

    public GameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Chiamato quando questa screen diventa attiva
        // Inizializza SpriteBatch, camera, ecc. QUI (non nel costruttore)
    }

    @Override
    public void render(float delta) {
        // delta = secondi dall'ultimo frame (es. 0.016f a 60fps)
        // 1. Aggiorna la logica con delta
        // 2. Pulisci lo schermo
        // 3. Disegna
    }

    @Override
    public void resize(int width, int height) {
        // Gestisci il ridimensionamento della finestra
    }

    @Override
    public void dispose() {
        // Libera le risorse create in show()
    }
}
```

**Cambiare schermata:**
```java
// Da qualsiasi punto del codice con accesso a game:
game.setScreen(new DayResultScreen(game));
// La vecchia screen riceve automaticamente hide() e poi dispose() se non riutilizzata
```

---

## 4. SpriteBatch — disegnare texture

`SpriteBatch` è il modo principale per disegnare in LibGDX. Raggruppa le chiamate
di disegno in un unico "batch" per motivi di performance.

**Regola d'oro:** tutto ciò che disegni deve stare tra `batch.begin()` e `batch.end()`.

```java
// In show():
SpriteBatch batch = new SpriteBatch();

// In render():
Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // pulisci schermo

batch.setProjectionMatrix(camera.combined); // usa le coordinate della camera
batch.begin();
    batch.draw(texture, x, y, width, height);
    // altri draw...
batch.end();
```

**Non creare un nuovo SpriteBatch ogni frame** — è costoso. Crealo una volta in `show()`.

---

## 5. Texture e TextureRegion

Una `Texture` è un'immagine caricata in memoria GPU.

```java
// Caricamento (in show() o tramite AssetManager):
Texture robotTexture = new Texture(Gdx.files.internal("entities/robot.png"));

// Disegno:
batch.draw(robotTexture, x, y, 64, 64); // x, y, larghezza, altezza

// IMPORTANTE: libera la memoria quando non serve più
robotTexture.dispose();
```

**TextureRegion** — ritaglio di una texture (spritesheet):
```java
// Se hai un'unica immagine con tutte le texture del gioco (TextureAtlas):
TextureRegion region = new TextureRegion(robotTexture, 0, 0, 32, 32);
// oppure da un atlas:
TextureAtlas atlas = new TextureAtlas("game.atlas");
TextureRegion robotIdle = atlas.findRegion("robot_idle");
```

**Consiglio per MakeItWash:** metti tutte le texture in un unico file `atlas`
(generabile con LibGDX TexturePacker) e caricale tutte con un solo `TextureAtlas`.

---

## 6. OrthographicCamera — la telecamera

La camera definisce cosa vedi a schermo. Per un gestionale 2D si usa `OrthographicCamera`.

```java
// In show():
OrthographicCamera camera = new OrthographicCamera();
camera.setToOrtho(false, 1280, 720); // false = y va verso l'alto

// Seguire il giocatore (in render(), prima del batch):
camera.position.set(player.x, player.y, 0);
camera.update();

// Zoom:
camera.zoom = 1.5f; // > 1 = zoom out, < 1 = zoom in
camera.update();

// Convertire coordinate mouse → coordinate mondo:
Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(mousePos);
// mousePos.x e mousePos.y ora sono le coordinate nel mondo di gioco
```

**Viewport** — gestisce il ridimensionamento della finestra:
```java
// In show():
Viewport viewport = new FitViewport(1280, 720, camera);

// In resize():
viewport.update(width, height, true);
```

---

## 7. ShapeRenderer — debug e forme geometriche

Utile per disegnare la griglia, barre della pazienza, hitbox di debug.

```java
ShapeRenderer shapeRenderer = new ShapeRenderer();

// In render() — per disegnare la griglia:
shapeRenderer.setProjectionMatrix(camera.combined);
shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.DARK_GRAY);
    for (int x = 0; x <= GRID_WIDTH; x++) {
        shapeRenderer.line(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
    }
    for (int y = 0; y <= GRID_HEIGHT; y++) {
        shapeRenderer.line(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
    }
shapeRenderer.end();

// Per riempire una cella (es. macchina selezionata):
shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(new Color(1, 1, 0, 0.3f)); // giallo semi-trasparente
    shapeRenderer.rect(cellX * CELL_SIZE, cellY * CELL_SIZE, CELL_SIZE, CELL_SIZE);
shapeRenderer.end();
```

**Attenzione:** `ShapeRenderer` e `SpriteBatch` non possono essere attivi contemporaneamente.
Prima `batch.end()`, poi `shapeRenderer.begin()`.

---

## 8. Input — tastiera, mouse, touch

```java
// Polling — controllo diretto ogni frame (in render()):
if (Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(delta);
if (Gdx.input.isKeyPressed(Input.Keys.A)) player.moveLeft(delta);

// isKeyJustPressed — si attiva SOLO al primo frame della pressione:
if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
    game.setScreen(new PauseScreen(game));
}

// Mouse:
float mouseX = Gdx.input.getX();   // coordinate schermo
float mouseY = Gdx.input.getY();
boolean leftClick = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

// Rotella del mouse per lo zoom:
// (si usa InputProcessor)
```

**InputProcessor** — per eventi (click, scroll):
```java
// In show():
Gdx.input.setInputProcessor(new InputAdapter() {
    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY * 0.1f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3f);
        return true;
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // converti coordinate e gestisci click sulla griglia
        return true;
    }
});
```

---

## 9. Scene2D — UI e HUD

Scene2D è il sistema UI di LibGDX (bottoni, label, tabelle, ecc.).
È il modo corretto per fare menu e HUD.

```java
// In show():
Stage stage = new Stage(new FitViewport(1280, 720));
Gdx.input.setInputProcessor(stage); // stage gestisce gli input

// Skin (tema visivo dei widget):
Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

// Label (testo):
Label yenLabel = new Label("¥ 0", skin);

// Bottone:
TextButton playButton = new TextButton("Inizia", skin);
playButton.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        game.setScreen(new GameScreen(game));
    }
});

// Table (layout):
Table table = new Table();
table.setFillParent(true); // occupa tutto lo schermo
table.center();
table.add(playButton).width(200).height(60);
stage.addActor(table);

// In render():
stage.act(delta);   // aggiorna animazioni e logica UI
stage.draw();       // disegna

// In resize():
stage.getViewport().update(width, height, true);

// In dispose():
stage.dispose();
skin.dispose();
```

**Skin gratuita per iniziare:** scarica `uiskin.json` dalla repository
[libgdx/libgdx-demo-tests](https://github.com/libgdx/libgdx-demo-tests) oppure
usa [Skin Composer](https://github.com/raeleus/skin-composer) per crearne una custom.

---

## 10. BitmapFont — testo a schermo

```java
// Font di default LibGDX (Arial 15px):
BitmapFont font = new BitmapFont();

// Font personalizzato (da file .fnt generato con Hiero o bmfont):
BitmapFont customFont = new BitmapFont(Gdx.files.internal("fonts/myfont.fnt"));

// Disegno (dentro batch.begin() / batch.end()):
font.setColor(Color.WHITE);
font.draw(batch, "¥ 1500", 20, Gdx.graphics.getHeight() - 20);

// Scala:
font.getData().setScale(1.5f);
```

---

## 11. AssetManager — caricare le risorse

`AssetManager` carica le risorse in modo asincrono e gestisce il caching.
È il modo corretto per caricare texture, font e audio in un progetto reale.

```java
// In MainGame.create():
AssetManager assets = new AssetManager();
assets.load("entities/robot.png", Texture.class);
assets.load("entities/conveyor.png", Texture.class);
assets.load("fonts/game.fnt", BitmapFont.class);
assets.finishLoading(); // blocca fino al completamento (ok per desktop)

// Recupero delle risorse (da qualsiasi screen tramite game.assets):
Texture robotTex = assets.get("entities/robot.png", Texture.class);

// In MainGame.dispose():
assets.dispose(); // libera tutto in un colpo
```

---

## 12. La griglia di gioco — implementazione pratica

La griglia è il cuore di MakeItWash. Ecco un'implementazione base:

```java
// world/Grid.java
package com.makeitwash.world;

import com.makeitwash.entities.PlaceableEntity;

public class Grid {
    public static final int CELL_SIZE = 64; // pixel per cella
    private final int width;
    private final int height;
    private final PlaceableEntity[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new PlaceableEntity[width][height];
    }

    public boolean isEmpty(int gridX, int gridY) {
        return isValid(gridX, gridY) && cells[gridX][gridY] == null;
    }

    public boolean place(PlaceableEntity entity, int gridX, int gridY) {
        if (!isEmpty(gridX, gridY)) return false;
        cells[gridX][gridY] = entity;
        entity.gridX = gridX;
        entity.gridY = gridY;
        return true;
    }

    public PlaceableEntity remove(int gridX, int gridY) {
        PlaceableEntity e = cells[gridX][gridY];
        cells[gridX][gridY] = null;
        return e;
    }

    // Converti pixel → griglia:
    public int toGridX(float pixelX) { return (int)(pixelX / CELL_SIZE); }
    public int toGridY(float pixelY) { return (int)(pixelY / CELL_SIZE); }

    // Converti griglia → pixel (angolo in basso a sinistra della cella):
    public float toPixelX(int gridX) { return gridX * CELL_SIZE; }
    public float toPixelY(int gridY) { return gridY * CELL_SIZE; }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
```

**Rilevare su quale cella sta il mouse** (in `GameScreen.render()`):
```java
Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(mouse);
int hoveredCellX = grid.toGridX(mouse.x);
int hoveredCellY = grid.toGridY(mouse.y);
```

---

## 13. Gestione della memoria — dispose()

LibGDX **non usa il garbage collector** per le risorse GPU. Devi liberarle tu.
Ogni classe che implementa `Disposable` va chiamata con `.dispose()`.

| Classe | Va liberata con |
|--------|----------------|
| `Texture` | `texture.dispose()` |
| `SpriteBatch` | `batch.dispose()` |
| `ShapeRenderer` | `shapeRenderer.dispose()` |
| `BitmapFont` | `font.dispose()` |
| `Stage` | `stage.dispose()` |
| `Skin` | `skin.dispose()` |
| `AssetManager` | `assets.dispose()` (libera tutto) |

**Regola:** se l'hai creato in `show()`, liberalo in `dispose()`.  
Se l'hai creato in `MainGame.create()`, liberalo in `MainGame.dispose()`.

---

## 14. Struttura consigliata per MakeItWash

```
core/src/com/makeitwash/
├── MainGame.java              ← Game, AssetManager globale
├── screens/
│   ├── MenuScreen.java        ← Scene2D: bottoni, titolo
│   ├── GameScreen.java        ← camera, griglia, entità, HUD
│   ├── BuildMenuScreen.java   ← pannello costruzione (Stage overlay)
│   ├── PauseScreen.java
│   ├── DayResultScreen.java
│   └── GameOverScreen.java
├── world/
│   ├── Grid.java              ← matrice di celle, piazzamento
│   ├── Day.java               ← timer giornata, ondate clienti
│   └── Economy.java           ← yen, costi, guadagni
├── entities/
│   ├── PlaceableEntity.java   ← classe base per tutto ciò che va sulla griglia
│   ├── WashingMachine.java
│   ├── ConveyorBelt.java
│   ├── Robot.java
│   ├── Player.java
│   └── Customer.java
├── items/
│   ├── Item.java              ← classe base per gli oggetti trasportabili
│   ├── Rice.java
│   └── SushiItem.java
└── ui/
    └── HUD.java               ← Stage con yen, reputazione, timer giornata
```
