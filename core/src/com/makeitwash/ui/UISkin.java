package com.makeitwash.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UISkin {

    public enum ColorScheme {
        BLUE("Blue"),
        GREEN("Green"),
        GREY("Grey"),
        RED("Red"),
        YELLOW("Yellow");

        public final String folder;

        ColorScheme(String folder) {
            this.folder = folder;
        }
    }

    private static UISkin instance;
    private final Skin skin;
    private final Texture buttonSquareFlat;
    private final Texture buttonSquareGloss;
    private final Texture buttonSquareDepthFlat;
    private final Texture buttonSquareDepthGloss;
    private final Texture buttonRoundFlat;
    private final Texture buttonRoundGloss;
    private final Texture buttonRectangleFlat;
    private final Texture buttonRectangleGloss;
    private final Texture buttonRectangleDepthFlat;
    private final Texture buttonRectangleDepthGloss;
    private final Texture slideHorizontalGrey;
    private final Texture slideHorizontalColor;
    private final Texture slideVerticalGrey;
    private final Texture slideVerticalColor;
    private final Texture checkSquareGrey;
    private final Texture checkSquareColor;
    private final Texture checkRoundGrey;
    private final Texture checkRoundColor;
    private final Texture iconCheckmark;
    private final Texture iconCross;
    private final Texture iconCircle;
    private final Texture iconSquare;
    private final Texture iconArrowUp;
    private final Texture iconArrowDown;
    private final Texture iconPlay;
    private final Texture iconRepeat;
    private final Texture star;
    private final Texture starOutline;
    private final Texture divider;
    private final Texture inputRectangle;
    private final Texture inputSquare;

    private UISkin() {
        skin = new Skin();

        String basePath = "assets/ui/PNG/";

        buttonSquareFlat = loadTexture(basePath + "Blue/Default/button_square_flat.png");
        buttonSquareGloss = loadTexture(basePath + "Blue/Default/button_square_gloss.png");
        buttonSquareDepthFlat = loadTexture(basePath + "Blue/Default/button_square_depth_flat.png");
        buttonSquareDepthGloss = loadTexture(basePath + "Blue/Default/button_square_depth_gloss.png");
        buttonRoundFlat = loadTexture(basePath + "Blue/Default/button_round_flat.png");
        buttonRoundGloss = loadTexture(basePath + "Blue/Default/button_round_gloss.png");
        buttonRectangleFlat = loadTexture(basePath + "Blue/Default/button_rectangle_flat.png");
        buttonRectangleGloss = loadTexture(basePath + "Blue/Default/button_rectangle_gloss.png");
        buttonRectangleDepthFlat = loadTexture(basePath + "Blue/Default/button_rectangle_depth_flat.png");
        buttonRectangleDepthGloss = loadTexture(basePath + "Blue/Default/button_rectangle_depth_gloss.png");

        slideHorizontalGrey = loadTexture(basePath + "Blue/Default/slide_horizontal_grey.png");
        slideHorizontalColor = loadTexture(basePath + "Blue/Default/slide_horizontal_color.png");
        slideVerticalGrey = loadTexture(basePath + "Blue/Default/slide_vertical_grey.png");
        slideVerticalColor = loadTexture(basePath + "Blue/Default/slide_vertical_color.png");

        checkSquareGrey = loadTexture(basePath + "Blue/Default/check_square_grey.png");
        checkSquareColor = loadTexture(basePath + "Blue/Default/check_square_color.png");
        checkRoundGrey = loadTexture(basePath + "Blue/Default/check_round_grey.png");
        checkRoundColor = loadTexture(basePath + "Blue/Default/check_round_color.png");

        iconCheckmark = loadTexture(basePath + "Blue/Default/icon_checkmark.png");
        iconCross = loadTexture(basePath + "Blue/Default/icon_cross.png");
        iconCircle = loadTexture(basePath + "Blue/Default/icon_circle.png");
        iconSquare = loadTexture(basePath + "Blue/Default/icon_square.png");
        iconArrowUp = loadTexture(basePath + "Extra/Default/icon_arrow_up_light.png");
        iconArrowDown = loadTexture(basePath + "Extra/Default/icon_arrow_down_light.png");
        iconPlay = loadTexture(basePath + "Extra/Default/icon_play_light.png");
        iconRepeat = loadTexture(basePath + "Extra/Default/icon_repeat_light.png");

        star = loadTexture(basePath + "Blue/Default/star.png");
        starOutline = loadTexture(basePath + "Blue/Default/star_outline.png");
        divider = loadTexture(basePath + "Extra/Default/divider.png");

        inputRectangle = loadTexture(basePath + "Extra/Default/input_rectangle.png");
        inputSquare = loadTexture(basePath + "Extra/Default/input_square.png");

        createTextButtonStyles();
        createLabelStyles();
        createCheckBoxStyles();
        createSliderStyles();
    }

    private Texture loadTexture(String path) {
        try {
            String cwd = new java.io.File(".").getCanonicalPath();
            Gdx.app.log("UISkin", "CWD: " + cwd);
            java.io.File testFile = new java.io.File("assets/ui/PNG/Blue/Default/button_square_flat.png");
            Gdx.app.log("UISkin", "Test file exists: " + testFile.exists() + " at " + testFile.getAbsolutePath());
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.log("UISkin", "Failed to load: " + path + " - " + e.getMessage());
            return null;
        }
    }

    private void createTextButtonStyles() {
        TextButtonStyle squareFlat = new TextButtonStyle();
        squareFlat.up = new TextureRegionDrawable(new TextureRegion(buttonSquareFlat));
        squareFlat.down = new TextureRegionDrawable(new TextureRegion(buttonSquareDepthFlat));
        squareFlat.over = new TextureRegionDrawable(new TextureRegion(buttonSquareGloss));
        skin.add("square_flat", squareFlat);

        TextButtonStyle squareGloss = new TextButtonStyle();
        squareGloss.up = new TextureRegionDrawable(new TextureRegion(buttonSquareGloss));
        squareGloss.down = new TextureRegionDrawable(new TextureRegion(buttonSquareDepthGloss));
        squareGloss.over = new TextureRegionDrawable(new TextureRegion(buttonSquareFlat));
        skin.add("square_gloss", squareGloss);

        TextButtonStyle squareDepthFlat = new TextButtonStyle();
        squareDepthFlat.up = new TextureRegionDrawable(new TextureRegion(buttonSquareDepthFlat));
        squareDepthFlat.down = new TextureRegionDrawable(new TextureRegion(buttonSquareFlat));
        squareDepthFlat.over = new TextureRegionDrawable(new TextureRegion(buttonSquareGloss));
        skin.add("square_depth_flat", squareDepthFlat);

        TextButtonStyle squareDepthGloss = new TextButtonStyle();
        squareDepthGloss.up = new TextureRegionDrawable(new TextureRegion(buttonSquareDepthGloss));
        squareDepthGloss.down = new TextureRegionDrawable(new TextureRegion(buttonSquareGloss));
        squareDepthGloss.over = new TextureRegionDrawable(new TextureRegion(buttonSquareDepthFlat));
        skin.add("square_depth_gloss", squareDepthGloss);

        TextButtonStyle roundFlat = new TextButtonStyle();
        roundFlat.up = new TextureRegionDrawable(new TextureRegion(buttonRoundFlat));
        roundFlat.down = new TextureRegionDrawable(new TextureRegion(buttonRoundGloss));
        roundFlat.over = new TextureRegionDrawable(new TextureRegion(buttonRoundGloss));
        skin.add("round_flat", roundFlat);

        TextButtonStyle roundGloss = new TextButtonStyle();
        roundGloss.up = new TextureRegionDrawable(new TextureRegion(buttonRoundGloss));
        roundGloss.down = new TextureRegionDrawable(new TextureRegion(buttonRoundFlat));
        roundGloss.over = new TextureRegionDrawable(new TextureRegion(buttonRoundGloss));
        skin.add("round_gloss", roundGloss);

        TextButtonStyle rectFlat = new TextButtonStyle();
        rectFlat.up = new TextureRegionDrawable(new TextureRegion(buttonRectangleFlat));
        rectFlat.down = new TextureRegionDrawable(new TextureRegion(buttonRectangleDepthFlat));
        rectFlat.over = new TextureRegionDrawable(new TextureRegion(buttonRectangleGloss));
        skin.add("rectangle_flat", rectFlat);

        TextButtonStyle rectGloss = new TextButtonStyle();
        rectGloss.up = new TextureRegionDrawable(new TextureRegion(buttonRectangleGloss));
        rectGloss.down = new TextureRegionDrawable(new TextureRegion(buttonRectangleDepthGloss));
        rectGloss.over = new TextureRegionDrawable(new TextureRegion(buttonRectangleFlat));
        skin.add("rectangle_gloss", rectGloss);

        TextButtonStyle rectDepthFlat = new TextButtonStyle();
        rectDepthFlat.up = new TextureRegionDrawable(new TextureRegion(buttonRectangleDepthFlat));
        rectDepthFlat.down = new TextureRegionDrawable(new TextureRegion(buttonRectangleFlat));
        rectDepthFlat.over = new TextureRegionDrawable(new TextureRegion(buttonRectangleGloss));
        skin.add("rectangle_depth_flat", rectDepthFlat);

        TextButtonStyle rectDepthGloss = new TextButtonStyle();
        rectDepthGloss.up = new TextureRegionDrawable(new TextureRegion(buttonRectangleDepthGloss));
        rectDepthGloss.down = new TextureRegionDrawable(new TextureRegion(buttonRectangleGloss));
        rectDepthGloss.over = new TextureRegionDrawable(new TextureRegion(buttonRectangleDepthFlat));
        skin.add("rectangle_depth_gloss", rectDepthGloss);
    }

    private void createLabelStyles() {
        LabelStyle defaultStyle = new LabelStyle();
        skin.add("default", defaultStyle);
    }

    private void createCheckBoxStyles() {
        CheckBoxStyle squareGrey = new CheckBoxStyle();
        squareGrey.checkboxOff = new TextureRegionDrawable(new TextureRegion(checkSquareGrey));
        squareGrey.checkboxOn = new TextureRegionDrawable(new TextureRegion(checkSquareColor));
        squareGrey.checkboxOn.setLeftWidth(0);
        skin.add("check_square_grey", squareGrey);

        CheckBoxStyle roundGrey = new CheckBoxStyle();
        roundGrey.checkboxOff = new TextureRegionDrawable(new TextureRegion(checkRoundGrey));
        roundGrey.checkboxOn = new TextureRegionDrawable(new TextureRegion(checkRoundColor));
        roundGrey.checkboxOn.setLeftWidth(0);
        skin.add("check_round_grey", roundGrey);
    }

    private void createSliderStyles() {
        SliderStyle hGrey = new SliderStyle();
        hGrey.background = new TextureRegionDrawable(new TextureRegion(slideHorizontalGrey));
        hGrey.knob = new TextureRegionDrawable(new TextureRegion(slideHorizontalColor));
        skin.add("slider_horizontal_grey", hGrey);

        SliderStyle hColor = new SliderStyle();
        hColor.background = new TextureRegionDrawable(new TextureRegion(slideHorizontalColor));
        hColor.knob = new TextureRegionDrawable(new TextureRegion(slideHorizontalGrey));
        skin.add("slider_horizontal_color", hColor);

        SliderStyle vGrey = new SliderStyle();
        vGrey.background = new TextureRegionDrawable(new TextureRegion(slideVerticalGrey));
        vGrey.knob = new TextureRegionDrawable(new TextureRegion(slideVerticalColor));
        skin.add("slider_vertical_grey", vGrey);

        SliderStyle vColor = new SliderStyle();
        vColor.background = new TextureRegionDrawable(new TextureRegion(slideVerticalColor));
        vColor.knob = new TextureRegionDrawable(new TextureRegion(slideVerticalGrey));
        skin.add("slider_vertical_color", vColor);
    }

    public static UISkin get() {
        if (instance == null) {
            instance = new UISkin();
        }
        return instance;
    }

    public Skin getSkin() {
        return skin;
    }

    public TextButtonStyle getButtonStyle(String styleName) {
        return skin.get(styleName, TextButtonStyle.class);
    }

    public TextButton createTextButton(String text, String styleName) {
        TextButton btn = new TextButton(text, skin, styleName);
        btn.getStyle().font = skin.get("default", LabelStyle.class).font;
        return btn;
    }

    public TextButton createTextButton(String text, String styleName, Color fontColor) {
        TextButton btn = new TextButton(text, skin, styleName);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.get("default", LabelStyle.class).font;
        labelStyle.fontColor = fontColor;
        btn.getStyle().font = labelStyle.font;
        btn.getLabel().setStyle(labelStyle);
        return btn;
    }

    public Drawable getDrawable(String texturePath) {
        Texture tex = loadTexture(texturePath);
        if (tex != null) {
            return new TextureRegionDrawable(new TextureRegion(tex));
        }
        return null;
    }

    public Texture getTexture(String texturePath) {
        return loadTexture(texturePath);
    }

    public TextButtonStyle createColoredButtonStyle(ColorScheme scheme, String baseStyle) {
        String basePath = "assets/ui/PNG/" + scheme.folder + "/Default/";
        TextButtonStyle style = new TextButtonStyle();
        
        String upPath = basePath + baseStyle + ".png";
        String downPath = basePath + baseStyle.replace("_flat", "_depth_flat").replace("_gloss", "_depth_gloss") + ".png";
        String overPath = basePath + baseStyle.replace("_flat", "_gloss").replace("_depth_", "_depth_") + ".png";

        style.up = getDrawable(upPath);
        style.down = getDrawable(downPath);
        style.over = getDrawable(overPath);

        return style;
    }

    public void dispose() {
        skin.dispose();
    }
}
