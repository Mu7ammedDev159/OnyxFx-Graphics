
/*
 * Copyright (c) [2025] [Mohammed Joharji]
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

package org.onyxfx.graphics.layout;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.File;

/**
 *
 * @ONYX-FX
 *
 * A class for managing show/hide children in a Dropdown List manner,
 * It has enhanced modern design than original Expandable lists with more controls.
 *
 * @author Mohammed Joharji
 * @version 1.0
 * @since 2025
 *
 */

public class OFxExpandablePane extends VBox {

    private static final PseudoClass UNDERLINE_VISIBLE = PseudoClass.getPseudoClass("underline-visible");

    private final BooleanProperty underlineVisible = new SimpleBooleanProperty(this, "underlineVisible", true);
    private final ObjectProperty<Image> arrowImage = new SimpleObjectProperty<>();
    private final DoubleProperty arrowIconWidth = new SimpleDoubleProperty(this, "arrowIconWidth", 10);
    private final DoubleProperty arrowIconHeight = new SimpleDoubleProperty(this, "arrowIconHeight", 10);
    private final BooleanProperty expanded = new SimpleBooleanProperty(false);
    private final StringProperty text = new SimpleStringProperty(this, "text", "Title");
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());
    private final ObjectProperty<Paint> textFill = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Paint> arrowTint = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Paint> hoverColor = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);

    private final Label titleLabel = new Label();
    private final ImageView arrowIcon = new ImageView();
    private final HBox headerBox = new HBox();
    private final Rectangle underline = new Rectangle();

    public OFxExpandablePane() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add("expandable-list-section");

        titleLabel.textProperty().bind(text);
        titleLabel.fontProperty().bind(font);
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        arrowIcon.fitWidthProperty().bind(arrowIconWidth);
        arrowIcon.fitHeightProperty().bind(arrowIconHeight);
        arrowIcon.setPreserveRatio(true);
        arrowIcon.setSmooth(true);

        arrowImage.addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                arrowIcon.setImage(newImg);
                if (newImg.getException() != null) newImg.getException().printStackTrace();
            }
        });

        arrowImage.addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                String url = newImg.getUrl();

                try {
                    // If SceneBuilder used @-notation, fix it
                    if (url.startsWith("@")) {
                        File fxmlDir = new File(System.getProperty("user.dir")); // assume root project dir
                        File actualFile = new File(fxmlDir, url.substring(1).replace("/", File.separator));
                        if (actualFile.exists()) {
                            Image fixedImage = new Image(actualFile.toURI().toString());
                            arrowIcon.setImage(fixedImage);
                        }
                    } else {
                        // Try default way (e.g., full file:/)
                        arrowIcon.setImage(newImg);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fix image path: " + url);
                }
            }
        });

        Platform.runLater(() -> {
            if (arrowImage.get() != null) {
                arrowIcon.setImage(arrowImage.get());
            }
        });

        arrowIcon.setVisible(true);
        arrowIcon.setManaged(true);
        arrowIcon.setOpacity(0.8);
        arrowIcon.setRotate(0);

        titleLabel.textFillProperty().bind(Bindings.createObjectBinding(() ->
                        headerBox.isHover() ? hoverColor.get() : textFill.get(),
                headerBox.hoverProperty(), hoverColor, textFill));

        underline.fillProperty().bind(Bindings.createObjectBinding(() ->
                        headerBox.isHover() ? hoverColor.get() : textFill.get(),
                headerBox.hoverProperty(), hoverColor, textFill));

        arrowIcon.effectProperty().bind(Bindings.createObjectBinding(() -> {
                    double width = arrowIcon.getFitWidth();
                    double height = arrowIcon.getFitHeight();
                    Color tint = (Color) (headerBox.isHover() ? hoverColor.get() : arrowTint.get());

                    return new Blend(
                            BlendMode.SRC_ATOP,
                            null,
                            new ColorInput(0, 0, width, height, tint)
                    );
                }, headerBox.hoverProperty(), hoverColor, arrowTint,
                arrowIcon.fitWidthProperty(), arrowIcon.fitHeightProperty()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(titleLabel, spacer, arrowIcon);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(6, 8, 6, 8));
        headerBox.setOnMouseClicked(e -> setExpanded(!isExpanded()));

        underline.setHeight(1);
        underline.visibleProperty().bind(underlineVisible);
        underline.managedProperty().bind(underlineVisible);
        underline.widthProperty().bind(headerBox.widthProperty());

        VBox underlineBox = new VBox(headerBox, underline);
        getChildren().add(underlineBox);

        getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node node : change.getAddedSubList()) {
                        if (node != underlineBox) {
                            node.managedProperty().bind(expanded);
                            node.visibleProperty().bind(expanded);
                        }
                    }
                }
            }
        });

        expanded.addListener((obs, oldVal, newVal) -> {
            if (isAnimated()) animateArrow(newVal);
            else arrowIcon.setRotate(newVal ? 90 : 0);
        });

        underlineVisible.addListener((obs, oldVal, newVal) -> pseudoClassStateChanged(UNDERLINE_VISIBLE, newVal));
        pseudoClassStateChanged(UNDERLINE_VISIBLE, underlineVisible.get());
    }

    private void loadImageFromUrl(String url) {
        try {
            Image img;
            if (url.startsWith("@")) {
                img = new Image(getClass().getResource(url.substring(1)).toExternalForm());
            } else if (url.startsWith("file:/") || new File(url).exists()) {
                img = new Image(new File(url).toURI().toURL().toExternalForm());
            } else {
                return;
            }
            arrowIcon.setImage(img);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image from url: " + url);
        }
    }

    private void animateArrow(boolean expand) {
        RotateTransition rotate = new RotateTransition(Duration.millis(100), arrowIcon);
        rotate.setFromAngle(expand ? 0 : 90);
        rotate.setToAngle(expand ? 90 : 0);
        rotate.play();
    }

    // Properties
    public void expand() { setExpanded(true); }
    public void collapse() { setExpanded(false); }
    public boolean isExpanded() { return expanded.get(); }
    public void setExpanded(boolean value) { expanded.set(value); }
    public BooleanProperty expandedProperty() { return expanded; }

    public boolean isUnderlineVisible() { return underlineVisible.get(); }
    public void setUnderlineVisible(boolean value) { underlineVisible.set(value); }
    public BooleanProperty underlineVisibleProperty() { return underlineVisible; }

    public Image getArrowImage() { return arrowImage.get(); }
    public void setArrowImage(Image image) { this.arrowImage.set(image); }
    public ObjectProperty<Image> arrowImageProperty() { return arrowImage; }

    public double getArrowIconWidth() { return arrowIconWidth.get(); }
    public void setArrowIconWidth(double width) { this.arrowIconWidth.set(width); }
    public DoubleProperty arrowIconWidthProperty() { return arrowIconWidth; }

    public double getArrowIconHeight() { return arrowIconHeight.get(); }
    public void setArrowIconHeight(double height) { this.arrowIconHeight.set(height); }
    public DoubleProperty arrowIconHeightProperty() { return arrowIconHeight; }

    public String getText() { return text.get(); }
    public void setText(String value) { text.set(value); }
    public StringProperty textProperty() { return text; }

    public Font getFont() { return font.get(); }
    public void setFont(Font value) { font.set(value); }
    public ObjectProperty<Font> fontProperty() { return font; }

    public Paint getTextFill() { return textFill.get(); }
    public void setTextFill(Paint value) { textFill.set(value); }
    public ObjectProperty<Paint> textFillProperty() { return textFill; }

    public Paint getArrowTint() { return arrowTint.get(); }
    public void setArrowTint(Paint value) { arrowTint.set(value); }
    public ObjectProperty<Paint> arrowTintProperty() { return arrowTint; }

    public Paint getHoverColor() { return hoverColor.get(); }
    public void setHoverColor(Paint value) { hoverColor.set(value); }
    public ObjectProperty<Paint> hoverColorProperty() { return hoverColor; }

    public boolean isAnimated() { return animated.get(); }
    public void setAnimated(boolean value) { animated.set(value); }
    public BooleanProperty animatedProperty() { return animated; }
}