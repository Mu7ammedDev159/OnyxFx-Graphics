
/*
 * Copyright (c) [2025] [MuhammedTJ]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onyxfx.graphics.controls;

import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @ONYX-FX
 *
 * A Button class that is customizable to support enhanced styling features.
 * It allows dynamic configuration of visual states such as normal, hover, pressed, and focused colors via properties.
 *
 * @author Mohammed Joharji
 * @version 1.0
 * @since 2025
 *
 */

/**
 * A custom JavaFX {@link Button} component that supports extended styling features such as
 * hover, pressed, focused, and normal background colors. This button also supports
 * configurable corner radius and an optional icon.
 *
 * <p>CSS properties:
 * <ul>
 *   <li>-ofx-normal-color</li>
 *   <li>-ofx-hover-color</li>
 *   <li>-ofx-pressed-color</li>
 *   <li>-ofx-focused-color</li>
 * </ul>
 *
 * <p>This control is designed to be used as part of the {@code onyxfx} library.
 */
public class OFxButton extends Button {

    // Factory for creating CSS-styleable properties.
    private static final StyleablePropertyFactory<OFxButton> FACTORY =
            new StyleablePropertyFactory<>(Button.getClassCssMetaData());

    // Styleable background color in normal state.
    private final StyleableObjectProperty<Paint> normalColor =
            new StyleableObjectProperty<>(Color.web("#2196f3")) {
                @Override
                public Object getBean() { return OFxButton.this; }
                @Override
                public String getName() { return "normalColor"; }
                @Override
                public CssMetaData<OFxButton, Paint> getCssMetaData() {
                    return StyleableProperties.NORMAL_COLOR;
                }
            };

    // Styleable background color in hover state.
    private final StyleableObjectProperty<Paint> hoverColor =
            new StyleableObjectProperty<>(Color.web("#4654c0")) {
                @Override
                public Object getBean() { return OFxButton.this; }
                @Override
                public String getName() { return "hoverColor"; }
                @Override
                public CssMetaData<OFxButton, Paint> getCssMetaData() {
                    return StyleableProperties.HOVER_COLOR;
                }
            };

    // Styleable background color in pressed state.
    private final StyleableObjectProperty<Paint> pressedColor =
            new StyleableObjectProperty<>(Color.web("#3a48a3")) {
                @Override
                public Object getBean() { return OFxButton.this; }
                @Override
                public String getName() { return "pressedColor"; }
                @Override
                public CssMetaData<OFxButton, Paint> getCssMetaData() {
                    return StyleableProperties.PRESSED_COLOR;
                }
            };

    // Styleable background color in focused state.
    private final StyleableObjectProperty<Paint> focusedColor =
            new StyleableObjectProperty<>(Color.web("#2196f3")) {
                @Override
                public Object getBean() { return OFxButton.this; }
                @Override
                public String getName() { return "focusedColor"; }
                @Override
                public CssMetaData<OFxButton, Paint> getCssMetaData() {
                    return StyleableProperties.FOCUSED_COLOR;
                }
            };

    // Property for holding an optional icon.
    private final ObjectProperty<Image> icon = new SimpleObjectProperty<>();

    // Property to define the round corner radius.
    private final DoubleProperty cornerRadius = new SimpleDoubleProperty(10.0);

    /**
     * Creates a new instance of {@code OFxButton} with default styling and behavior.
     * Initializes CSS class, listeners, and applies the default corner radius and background.
     */
    public OFxButton() {
        super();
        getStyleClass().add("ofx-button");

        // Listeners for state changes to update appearance
        hoverProperty().addListener((obs, oldVal, newVal) -> updateBackgroundColor());
        armedProperty().addListener((obs, oldVal, newVal) -> updateBackgroundColor());

        // Listeners for property changes
        normalColorProperty().addListener((obs, old, val) -> updateBackgroundColor());
        hoverColorProperty().addListener((obs, old, val) -> updateBackgroundColor());
        pressedColorProperty().addListener((obs, old, val) -> updateBackgroundColor());
        focusedColorProperty().addListener((obs, old, val) -> updateBackgroundColor());

        updateBackgroundColor();
        initListeners();
        updateCornerRadiusStyle(getRoundRadius());
    }

    /**
     * Updates the background color based on the current button state.
     */
    private void updateBackgroundColor() {
        Paint paint;

        if (isPressed() && getcPressedColor() != null) {
            paint = getcPressedColor();
        } else if (isHover() && getbHoverColor() != null) {
            paint = getbHoverColor();
        } else if (isFocused() && getdFocusedColor() != null) {
            paint = getdFocusedColor();
        } else {
            paint = getaNormalColor();
        }

        if (paint instanceof Color) {
            setBackground(new Background(new BackgroundFill(
                    (Color) paint,
                    getBackground() != null ? getBackground().getFills().get(0).getRadii() : null,
                    Insets.EMPTY
            )));
        }
    }

    /** Sets the icon image displayed inside the button. */
    public void setIcon(Image image) {
        this.icon.set(image);
    }

    /** @return the icon image currently used in the button. */
    public Image getIcon() {
        return icon.get();
    }

    /** @return the icon property for binding. */
    public ObjectProperty<Image> iconProperty() {
        return icon;
    }

    /** Sets the round corner radius of the button. */
    public void setRoundRadius(double radiusValue) {
        this.cornerRadius.set(radiusValue);
    }

    /** @return the current corner radius. */
    public double getRoundRadius() {
        return cornerRadius.get();
    }

    /** @return the corner radius property for binding. */
    public DoubleProperty cornerRadiusProperty() {
        return cornerRadius;
    }

    /** @return the styleable normal color. */
    public Paint getaNormalColor() {
        return normalColor.get();
    }

    /** Sets the styleable normal color. */
    public void setaNormalColor(Paint color) {
        this.normalColor.set(color);
    }

    /** @return the styleable normal color property. */
    public StyleableObjectProperty<Paint> normalColorProperty() {
        return normalColor;
    }

    public Paint getbHoverColor() {
        return hoverColor.get();
    }

    public void setbHoverColor(Paint color) {
        this.hoverColor.set(color);
    }

    public StyleableObjectProperty<Paint> hoverColorProperty() {
        return hoverColor;
    }

    public Paint getcPressedColor() {
        return pressedColor.get();
    }

    public void setcPressedColor(Paint color) {
        this.pressedColor.set(color);
    }

    public StyleableObjectProperty<Paint> pressedColorProperty() {
        return pressedColor;
    }

    public Paint getdFocusedColor() {
        return focusedColor.get();
    }

    public void setdFocusedColor(Paint color) {
        this.focusedColor.set(color);
    }

    public StyleableObjectProperty<Paint> focusedColorProperty() {
        return focusedColor;
    }

    // Initialize listeners for dynamic behavior (icon, corner radius)
    private void initListeners() {
        icon.addListener((obs, old, img) -> {
            if (img != null) {
                setGraphic(new ImageView(img));
            }
        });

        cornerRadius.addListener((obs, old, val) -> {
            updateCornerRadiusStyle(val.doubleValue());
        });
    }

    // Applies the CSS style for corner radius.
    private void updateCornerRadiusStyle(double roundCornerRadius) {
        setStyle("-fx-background-radius: " + roundCornerRadius + ";");
    }

    // Helper to convert a Color object to a CSS rgba() string.
    private String toRgbaString(Color color) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    // List of all CSS metadata for this control.
    private static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA_LIST;
    static {
        List<CssMetaData<? extends Styleable, ?>> list = new ArrayList<>(Button.getClassCssMetaData());
        list.addAll(List.of(
                ((StyleableProperty<?>) new OFxButton().normalColor).getCssMetaData(),
                ((StyleableProperty<?>) new OFxButton().hoverColor).getCssMetaData(),
                ((StyleableProperty<?>) new OFxButton().pressedColor).getCssMetaData(),
                ((StyleableProperty<?>) new OFxButton().focusedColor).getCssMetaData()
        ));
        CSS_META_DATA_LIST = Collections.unmodifiableList(list);
    }

    /**
     * Returns the list of CSS metadata associated with this control,
     * including the styleable color properties.
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return CSS_META_DATA_LIST;
    }

    /**
     * Overrides layoutChildren to ensure background color is updated
     * during layout passes.
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        updateBackgroundColor();
    }

    /**
     * Internal class holding the custom CSS metadata for {@code OFxButton}.
     */
    private static class StyleableProperties {
        private static final CssMetaData<OFxButton, Paint> NORMAL_COLOR =
                new CssMetaData<>("-ofx-focused-color", StyleConverter.getPaintConverter(), Color.web("#2196f3")) {
                    @Override public boolean isSettable(OFxButton node) {
                        return !node.normalColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(OFxButton node) {
                        return node.normalColorProperty();
                    }
                };

        private static final CssMetaData<OFxButton, Paint> HOVER_COLOR =
                new CssMetaData<>("-ofx-hover-color", StyleConverter.getPaintConverter(), Color.web("#4654c0")) {
                    @Override public boolean isSettable(OFxButton node) {
                        return !node.hoverColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(OFxButton node) {
                        return node.hoverColorProperty();
                    }
                };

        private static final CssMetaData<OFxButton, Paint> PRESSED_COLOR =
                new CssMetaData<>("-ofx-pressed-color", StyleConverter.getPaintConverter(), Color.web("#3a48a3")) {
                    @Override public boolean isSettable(OFxButton node) {
                        return !node.pressedColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(OFxButton node) {
                        return node.pressedColorProperty();
                    }
                };

        private static final CssMetaData<OFxButton, Paint> FOCUSED_COLOR =
                new CssMetaData<>("-ofx-focused-color", StyleConverter.getPaintConverter(), Color.web("#2196f3")) {
                    @Override public boolean isSettable(OFxButton node) {
                        return !node.focusedColor.isBound();
                    }
                    @Override public StyleableProperty<Paint> getStyleableProperty(OFxButton node) {
                        return node.focusedColorProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Button.getClassCssMetaData());
            styleables.add(NORMAL_COLOR);
            styleables.add(HOVER_COLOR);
            styleables.add(PRESSED_COLOR);
            styleables.add(FOCUSED_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
