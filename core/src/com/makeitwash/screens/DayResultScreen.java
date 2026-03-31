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
import com.makeitwash.world.Economy;

public class DayResultScreen extends ScreenAdapter {
    private final MainGame game;
    private final Economy economy;
    private final int dayNumber;
    private final float earnedYen;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    public DayResultScreen(MainGame game, Economy economy, int dayNumber, float earnedYen) {
        this.game = game;
        this.economy = economy;
        this.dayNumber = dayNumber;
        this.earnedYen = earnedYen;
    }

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
        parameter.size = 32;
        font = generator.generateFont(parameter);
        generator.dispose();

        stage = new Stage();
        
        Skin skin = new Skin();
        skin.add("default", font);
        
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
        skin.add("default", labelStyle);
        
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = createColorDrawable(Color.DARK_GRAY);
        buttonStyle.down = createColorDrawable(Color.GRAY);
        buttonStyle.over = createColorDrawable(Color.LIGHT_GRAY);
        skin.add("default", buttonStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("FINE GIORNATA " + dayNumber, skin);
        titleLabel.setStyle(labelStyle);

        Label earningsLabel = new Label(String.format("Guadagno: %.0f Yen", earnedYen), skin);
        Label totalLabel = new Label(String.format("Totale: %.0f Yen", economy.getYen()), skin);
        Label reputationLabel = new Label(String.format("Reputazione: %.0f%%", economy.getReputation()), skin);

        TextButton nextDayBtn = new TextButton("Giorno Successivo", skin);
        TextButton mainMenuBtn = new TextButton("Menu Principale", skin);

        nextDayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        table.add(titleLabel).padBottom(30);
        table.row();
        table.add(earningsLabel).padBottom(10);
        table.row();
        table.add(totalLabel).padBottom(10);
        table.row();
        table.add(reputationLabel).padBottom(30);
        table.row();
        table.add(nextDayBtn).width(250).height(60).padBottom(15);
        table.row();
        table.add(mainMenuBtn).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.15f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
    }
}
