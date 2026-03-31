package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.makeitwash.MainGame;
import com.makeitwash.world.Grid;
import com.makeitwash.world.Economy;

public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Economy economy;
    private SpriteBatch batch;
    private Stage stage;
    private BitmapFont font;

    public MenuScreen(MainGame game) {
        this.game = game;
        this.grid = new Grid();
        this.economy = new Economy();
    }

    // Crea un drawable colorato da usare come sfondo per i bottoni
    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        font = generator.generateFont(parameter);
        stage = new Stage();
        
        // Skin: contenitore di stili per la UI (font, bottoni, label)
        Skin skin = new Skin();
        skin.add("default", font);
        
        // Stile per le label (testo del titolo)
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
        skin.add("default", labelStyle);
        
        // Stile per i bottoni con colori per i vari stati (normale, premuto, hover)
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = createColorDrawable(Color.DARK_GRAY);
        buttonStyle.down = createColorDrawable(Color.GRAY);
        buttonStyle.over = createColorDrawable(Color.LIGHT_GRAY);
        skin.add("default", buttonStyle);

        // Table: layout tabellare per centrare gli elementi
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Titolo del gioco
        Label titleLabel = new Label("MakeItWash", skin);
        titleLabel.setStyle(labelStyle);

        // Bottoni: "Inizia Partita" e "Esci"
        TextButton playButton = new TextButton("Inizia Partita", skin);
        TextButton quitButton = new TextButton("Esci", skin);

        // Listener per il bottone "Inizia Partita": passa al GameScreen
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new GameScreen(game, grid, economy));
            }
        });

        // Listener per il bottone "Esci": chiude l'applicazione
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        // Disposizione degli elementi nella tabella (verticale, centrati)
        table.add(titleLabel).padBottom(50);
        table.row();
        table.add(playButton).width(250).height(60).padBottom(15);
        table.row();
        table.add(quitButton).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Pulisce lo schermo con un colore di sfondo scuro
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aggiorna e disegna la UI (stage)
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        // Libera le risorse (batch, stage, font)
        batch.dispose();
        stage.dispose();
        font.dispose();
    }
}
