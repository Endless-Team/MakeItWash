# 📚 Guida LibGDX per MakeItWash

> Guida pratica all'uso di LibGDX per un gioco gestionale 2D cross-platform.
> Target: Desktop (Windows/macOS/Linux) + Android.
> Calibrata su MakeItWash (Java esperti, LibGDX zero).

---

## Indice

1. [Come funziona LibGDX](#1-come-funziona-libgdx)
2. [Il ciclo di vita del gioco](#2-il-ciclo-di-vita-del-gioco)
3. [Screen — gestione delle schermate](#3-screen--gestione-delle-schermate)
4. [SpriteBatch — disegnare texture](#4-spritebatch--disegnare-texture)
5. [Texture e TextureRegion](#5-texture-e-textureregion)
6. [OrthographicCamera — la telecamera](#6-orthographiccamera--la-telecamera)
7. [Viewport — adattarsi a ogni schermo](#7-viewport--adattarsi-a-ogni-schermo)
8. [ShapeRenderer — debug e forme geometriche](#8-shaperenderer--debug-e-forme-geometriche)
9. [Input — tastiera, mouse e touch](#9-input--tastiera-mouse-e-touch)
10. [Gesture — pinch zoom e swipe su Android](#10-gesture--pinch-zoom-e-swipe-su-android)
11. [Scene2D — UI e HUD](#11-scene2d--ui-e-hud)
12. [BitmapFont — testo a schermo](#12-bitmapfont--testo-a-schermo)
13. [AssetManager — caricare le risorse](#13-assetmanager--caricare-le-risorse)
14. [Preferences — salvataggio cross-platform](#14-preferences--salvataggio-cross-platform)
15. [La griglia di gioco — implementazione pratica](#15-la-griglia-di-gioco--implementazione-pratica)
16. [Gestione della memoria — dispose()](#16-gestione-della-memoria--dispose)
17. [Modulo Android — setup e differenze](#17-modulo-android--setup-e-differenze)
18. [Struttura consigliata per MakeItWash](#18-struttura-consigliata-per-makeitwash)

---

## 1. Come funziona LibGDX

LibGDX astrae le differenze tra piattaforme (Desktop, Android, iOS).
Il progetto è diviso in tre moduli:

- **`core/`** → tutta la logica di gioco, condivisa ← scrivi tutto qui
- **`desktop/`** → crea la finestra LWJGL3 e avvia il core
- **`android/`** → Activity Android che avvia il core

**Regola fondamentale:** tutto il codice di gioco va in `core/`.
I moduli `desktop/` e `android/` contengono solo il launcher (2-3 file ciascuno).

---

## 2. Il ciclo di vita del gioco

```java
public class MainGame extends Game {

    @Override
    public void create() {
        // Chiamato UNA VOLTA all'avvio (desktop e android)
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // obbligatorio: delega alla Screen corrente
    }

    @Override
    public void pause() {
        // Android: app va in background → salva stato
        // Desktop: finestra minimizzata
    }

    @Override
    public void resume() {
        // Android: app torna in primo piano
        // Le texture vengono ricaricate automaticamente se usi AssetManager
    }

    @Override
    public void dispose() {
        // Chiusura del gioco → libera risorse globali
    }
}
```

> **Nota Android critica:** quando l'app va in background, il contesto OpenGL
> viene distrutto e tutte le texture vengono perse. `AssetManager` le ricarica
> automaticamente al resume. Se carichi texture con `new Texture(...)` manualmente,
> devi ricaricarle tu in `resume()`.

---

## 3. Screen — gestione delle schermate

```java
public class GameScreen extends ScreenAdapter {
    private final MainGame game;

    public GameScreen(MainGame game) { this.game = game; }

    @Override
    public void show() {
        // Inizializza SpriteBatch, camera, Stage QUI — non nel costruttore
    }

    @Override
    public void render(float delta) {
        // delta = secondi dall'ultimo frame (0.016f a 60fps)
        // Usalo per movimenti frame-rate indipendenti: x += velocità * delta
    }

    @Override
    public void resize(int width, int height) {
        // Desktop: ridimensionamento finestra
        // Android: rotazione dispositivo
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() { /* libera risorse create in show() */ }
}
```

**Cambiare schermata:**
```java
game.setScreen(new DayResultScreen(game));
// La vecchia screen riceve hide() poi dispose() automaticamente
```

---

## 4. SpriteBatch — disegnare texture

```java
// In show():
SpriteBatch batch = new SpriteBatch();

// In render():
Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

batch.setProjectionMatrix(camera.combined);
batch.begin();
    batch.draw(texture, x, y, width, height);
batch.end();
```

- Non creare un nuovo `SpriteBatch` ogni frame — crealo una volta in `show()`
- Non annidare `begin()`/`end()` — non puoi chiamare `begin()` mentre è già aperto

---

## 5. Texture e TextureRegion

```java
// Caricamento diretto (solo per testing — usa AssetManager in produzione):
Texture robotTexture = new Texture(Gdx.files.internal("entities/robot.png"));
batch.draw(robotTexture, x, y, 64, 64);

// TextureRegion — ritaglio di una spritesheet:
TextureRegion region = new TextureRegion(sheet, frameX, frameY, 32, 32);

// TextureAtlas — un file con tutte le texture (CONSIGLIATO):
TextureAtlas atlas = new TextureAtlas("game.atlas");
TextureRegion robotIdle = atlas.findRegion("robot_idle");
TextureRegion conveyor  = atlas.findRegion("conveyor");
```

**Consiglio MakeItWash:** usa un unico `TextureAtlas` per tutte le texture.
Si genera con **LibGDX TexturePacker**. Riduce i draw call → performance migliori su Android.

---

## 6. OrthographicCamera — la telecamera

```java
// In show():
OrthographicCamera camera = new OrthographicCamera();
camera.setToOrtho(false, 1280, 720); // false = y verso l'alto

// Seguire il giocatore (in render()):
camera.position.set(player.x, player.y, 0);
camera.update();

// Zoom:
camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3f);
camera.update();

// Convertire coordinate touch/mouse → coordinate mondo:
Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(pos);
// pos.x e pos.y ora sono coordinate nel mondo di gioco
// Funziona identicamente su Desktop (mouse) e Android (touch)
```

---

## 7. Viewport — adattarsi a ogni schermo

Il `Viewport` è **fondamentale** per il cross-platform. Gestisce come il mondo
di gioco si adatta a schermi diversi (monitor 1080p, tablet, telefono).

```java
// FitViewport: mantiene le proporzioni, aggiunge bande nere se necessario
// Consigliato per MakeItWash
Viewport viewport = new FitViewport(1280, 720, camera);

// ExtendViewport: estende il mondo invece di aggiungere bande nere
// Buono se vuoi mostrare più griglia su schermi più grandi
Viewport viewport = new ExtendViewport(1280, 720, camera);

// In resize() — OBBLIGATORIO:
@Override
public void resize(int width, int height) {
    viewport.update(width, height, true);          // aggiorna world viewport
    stage.getViewport().update(width, height, true); // aggiorna UI stage
}

// In render():
batch.setProjectionMatrix(camera.combined);
```

> Su Android `resize()` viene chiamato anche ad ogni rotazione dispositivo.
> Con orientamento landscape forzato in AndroidManifest questo non accade.

---

## 8. ShapeRenderer — debug e forme geometriche

```java
ShapeRenderer shapeRenderer = new ShapeRenderer();

// Griglia (in render(), dopo batch.end()):
shapeRenderer.setProjectionMatrix(camera.combined);
shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
    for (int x = 0; x <= GRID_WIDTH; x++)
        shapeRenderer.line(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
    for (int y = 0; y <= GRID_HEIGHT; y++)
        shapeRenderer.line(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
shapeRenderer.end();

// Cella evidenziata (anteprima piazzamento):
Gdx.gl.glEnable(GL20.GL_BLEND);
shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(1f, 1f, 0f, 0.3f);
    shapeRenderer.rect(cellX * CELL_SIZE, cellY * CELL_SIZE, CELL_SIZE, CELL_SIZE);
shapeRenderer.end();
```

**Regola:** `ShapeRenderer` e `SpriteBatch` non possono essere aperti contemporaneamente.
Sempre `batch.end()` prima di `shapeRenderer.begin()`.

---

## 9. Input — tastiera, mouse e touch

```java
// --- TASTIERA (solo desktop) ---
if (Gdx.input.isKeyPressed(Input.Keys.W))        player.moveUp(delta);
if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) pauseGame();

// --- MOUSE (desktop) / TOUCH (android) — stessa API ---
boolean touched   = Gdx.input.isTouched();     // tenuto premuto
boolean justTouch = Gdx.input.justTouched();   // solo primo frame

// Converti in coordinate mondo:
Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(pos);

// --- MULTI-TOUCH (android) ---
if (Gdx.input.isTouched(0)) { /* primo dito  */ }
if (Gdx.input.isTouched(1)) { /* secondo dito (pinch zoom) */ }
```

**InputProcessor** per eventi:
```java
Gdx.input.setInputProcessor(new InputAdapter() {
    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY * 0.1f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3f);
        return true;
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 pos = new Vector3(screenX, screenY, 0);
        camera.unproject(pos);
        handleCellTap(grid.toGridX(pos.x), grid.toGridY(pos.y));
        return true;
    }
});
```

**Combinare Stage UI e InputProcessor:**
```java
InputMultiplexer multiplexer = new InputMultiplexer();
multiplexer.addProcessor(stage);      // la UI intercetta prima
multiplexer.addProcessor(gameInput);  // poi la logica di gioco
Gdx.input.setInputProcessor(multiplexer);
```

---

## 10. Gesture — pinch zoom e swipe su Android

```java
GestureDetector gestureDetector = new GestureDetector(new GestureAdapter() {
    private float initialZoom;

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance == distance) initialZoom = camera.zoom;
        camera.zoom = initialZoom * (initialDistance / distance);
        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 3f);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        return true;
    }
});

// Aggiungilo al multiplexer dopo lo Stage:
multiplexer.addProcessor(gestureDetector);
```

---

## 11. Scene2D — UI e HUD

```java
// In show():
Stage stage = new Stage(new FitViewport(1280, 720));
Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

// Label:
Label yenLabel = new Label("¥ 0", skin);

// TextButton:
TextButton playButton = new TextButton("Inizia", skin);
playButton.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        game.setScreen(new GameScreen(game));
    }
});

// Table — layout responsive:
Table table = new Table();
table.setFillParent(true);
table.center();
table.add(playButton).width(200).height(80); // 80px = tappabile su mobile
stage.addActor(table);

// In render():
stage.act(delta);
stage.draw();

// In dispose():
stage.dispose();
skin.dispose();
```

---

## 12. BitmapFont — testo a schermo

```java
BitmapFont font = new BitmapFont();
font.getData().setScale(2f); // scala per leggibilità su mobile

font.setColor(Color.WHITE);
font.draw(batch, "¥ 1500", x, y); // dentro batch.begin()/end()
```

**Consiglio Android:** usa font almeno 20-24px o scala con `setScale()`.

---

## 13. AssetManager — caricare le risorse

`AssetManager` è **obbligatorio per Android**: ricarica automaticamente le texture
dopo che il contesto OpenGL viene perso (app in background).

```java
// In MainGame:
public AssetManager assets;

@Override
public void create() {
    assets = new AssetManager();
    assets.load("entities/robot.png", Texture.class);
    assets.load("ui/uiskin.json", Skin.class);
    assets.load("fonts/game.fnt", BitmapFont.class);
    assets.finishLoading(); // blocca fino al termine

    setScreen(new MenuScreen(this));
}

// In qualsiasi Screen:
Texture robotTex = game.assets.get("entities/robot.png", Texture.class);

// In MainGame.dispose():
assets.dispose(); // libera tutto in un colpo
```

---

## 14. Preferences — salvataggio cross-platform

Stessa API su Desktop (file locale) e Android (SharedPreferences):

```java
Preferences prefs = Gdx.app.getPreferences("MakeItWash");

// Salva:
prefs.putInteger("currentDay", 5);
prefs.putFloat("yen", 1500f);
prefs.putFloat("reputation", 80f);
prefs.flush(); // OBBLIGATORIO per persistere su disco

// Leggi (con valore di default):
int   day        = prefs.getInteger("currentDay", 1);
float yen        = prefs.getFloat("yen", 500f);
float reputation = prefs.getFloat("reputation", 100f);
```

---

## 15. La griglia di gioco — implementazione pratica

```java
// world/Grid.java
public class Grid {
    public static final int CELL_SIZE = 64; // px per cella (tappabile su mobile)
    private final int width, height;
    private final PlaceableEntity[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new PlaceableEntity[width][height];
    }

    public boolean isEmpty(int x, int y) {
        return isValid(x, y) && cells[x][y] == null;
    }

    public boolean place(PlaceableEntity entity, int x, int y) {
        if (!isEmpty(x, y)) return false;
        cells[x][y] = entity;
        entity.gridX = x;
        entity.gridY = y;
        return true;
    }

    public PlaceableEntity remove(int x, int y) {
        PlaceableEntity e = cells[x][y];
        cells[x][y] = null;
        return e;
    }

    public int toGridX(float worldX) { return (int)(worldX / CELL_SIZE); }
    public int toGridY(float worldY) { return (int)(worldY / CELL_SIZE); }
    public float toPixelX(int gridX) { return gridX * CELL_SIZE; }
    public float toPixelY(int gridY) { return gridY * CELL_SIZE; }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
```

**Rilevare la cella toccata/cliccata (desktop e android identici):**
```java
Vector3 pos = new Vector3(screenX, screenY, 0);
camera.unproject(pos);
int cellX = grid.toGridX(pos.x);
int cellY = grid.toGridY(pos.y);
```

---

## 16. Gestione della memoria — dispose()

| Classe | Va liberata con |
|--------|----------------|
| `Texture` / `TextureAtlas` | `.dispose()` |
| `SpriteBatch` | `.dispose()` |
| `ShapeRenderer` | `.dispose()` |
| `BitmapFont` | `.dispose()` |
| `Stage` | `.dispose()` |
| `Skin` | `.dispose()` |
| `AssetManager` | `.dispose()` — libera tutto |
| `Music` / `Sound` | `.dispose()` |

**Regola:** creato in `show()` → liberato in `dispose()`.
Creato in `MainGame.create()` → liberato in `MainGame.dispose()`.
Se usi `AssetManager`, non fare `dispose()` sulle singole risorse — le gestisce lui.

---

## 17. Modulo Android — setup e differenze

### `android/build.gradle`

```groovy
apply plugin: 'com.android.application'

android {
    namespace 'com.makeitwash'
    compileSdk 34
    defaultConfig {
        applicationId 'com.makeitwash'
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName '1.0'
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}

dependencies {
    implementation project(':core')
    implementation "com.badlogicgames.gdx:gdx-backend-android:$gdxVersion"
    natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a"
    natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a"
    natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86"
    natives "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64"
}
```

### `android/src/.../AndroidLauncher.java`

```java
package com.makeitwash;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true; // fullscreen su Android
        initialize(new MainGame(), config);
    }
}
```

### `android/AndroidManifest.xml` (parti importanti)

```xml
<activity
    android:name=".AndroidLauncher"
    android:screenOrientation="landscape"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize">
</activity>
```

### Differenze Desktop vs Android

| Aspetto | Desktop | Android |
|---------|---------|---------|
| Contesto OpenGL | Persiste sempre | Perso quando l'app va in background |
| Input principale | Mouse + tastiera | Touch multi-dito |
| Percorsi file (`assets/`) | Gestito da LibGDX — identico | Identico |
| Back button | Non esiste | Catturalo: `Gdx.input.setCatchKey(Input.Keys.BACK, true)` |
| Thread UI | Unico thread LibGDX | Unico thread LibGDX — non usare `runOnUiThread` |
| Permessi necessari | Nessuno | Nessuno (gioco offline) |

---

## 18. Struttura consigliata per MakeItWash

```
MakeItWash/
├── build.gradle                        ← root: gdxVersion, plugin android
├── settings.gradle                     ← include 'core', 'desktop', 'android'
├── core/
│   ├── build.gradle
│   ├── assets/                         ← texture, font, audio, skin UI
│   │   ├── entities/
│   │   ├── ui/
│   │   └── fonts/
│   └── src/com/makeitwash/
│       ├── MainGame.java               ← Game, AssetManager globale
│       ├── screens/
│       │   ├── MenuScreen.java
│       │   ├── GameScreen.java
│       │   ├── BuildMenuScreen.java
│       │   ├── PauseScreen.java
│       │   ├── DayResultScreen.java
│       │   └── GameOverScreen.java
│       ├── world/
│       │   ├── Grid.java
│       │   ├── Day.java
│       │   └── Economy.java
│       ├── entities/
│       │   ├── PlaceableEntity.java    ← classe base
│       │   ├── WashingMachine.java
│       │   ├── ConveyorBelt.java
│       │   ├── Robot.java
│       │   ├── Player.java
│       │   └── Customer.java
│       ├── items/
│       │   ├── Item.java
│       │   ├── Rice.java
│       │   └── SushiItem.java
│       └── ui/
│           └── HUD.java               ← Stage con yen, reputazione, timer
├── desktop/
│   ├── build.gradle
│   └── src/com/makeitwash/
│       └── DesktopLauncher.java
└── android/
    ├── build.gradle
    ├── AndroidManifest.xml
    └── src/com/makeitwash/
        └── AndroidLauncher.java
```
