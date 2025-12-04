package org.onyxfx.graphics.controls;

import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Random;
import java.util.prefs.Preferences;

/**
 * @ONYX-FX
 *
 * A customizable, interactive avatar view that extends Button.
 * - Circular clipped image for default avatar mode
 * - Full component image takeover for uploaded avatars (still rounded)
 * - Background color stored in Preferences
 * - Customizable background colors for hover and pressed states
 * - Clickable, hoverable, focusable
 * - Optional image tinting (default avatar only)
 * - Optional status dot with outer ring effect
 *
 * @author Mohammed Joharji
 *
 */
public class OFxAvatarView extends StackPane {

    private static final Random RANDOM = new Random();
    private static final Preferences PREFS = Preferences.userNodeForPackage(OFxAvatarView.class);
    private static final String COLOR_PREF_KEY = "ofx-avatar-color";

    private final ObjectProperty<Image> defaultImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> uploadedImage = new SimpleObjectProperty<>();
    private final DoubleProperty size = new SimpleDoubleProperty(40);
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(loadOrGenerateColor());
    private final ObjectProperty<Color> hoverColor = new SimpleObjectProperty<>(Color.LIGHTGRAY);
    private final ObjectProperty<Color> pressedColor = new SimpleObjectProperty<>(Color.GRAY);
    private final DoubleProperty fitWidth = new SimpleDoubleProperty(0);
    private final DoubleProperty fitHeight = new SimpleDoubleProperty(0);
    private final ObjectProperty<Color> tintColor = new SimpleObjectProperty<>(Color.WHITE);

    private final BooleanProperty statusVisible = new SimpleBooleanProperty(false);
    private final ObjectProperty<Color> statusColor = new SimpleObjectProperty<>(Color.LIMEGREEN);
    private final ObjectProperty<Color> statusRingColor = new SimpleObjectProperty<>(Color.DARKBLUE);

    private final ImageView imageView = new ImageView();
    private final Circle clip = new Circle();
    private final Blend tintEffect = new Blend();
    private final ColorInput hoverOverlay = new ColorInput();

    private final Circle statusRing = new Circle();
    private final Circle statusDot = new Circle();

    private final Button button = new Button();

    public OFxAvatarView() {
        initialize();
    }

    public OFxAvatarView(Image defaultAvatarImage) {
        setDefaultImage(defaultAvatarImage);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("ofx-avatar-view");
        setPrefSize(size.get(), size.get());

        button.setPrefSize(size.get(), size.get());
        button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        button.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        imageView.setSmooth(true);
        button.setGraphic(imageView);

        uploadedImage.addListener((obs, old, newVal) -> updateVisualMode());
        defaultImage.addListener((obs, old, newVal) -> updateVisualMode());
        updateVisualMode();

        backgroundColor.addListener((obs, old, val) -> {
            if (uploadedImage.get() == null) {
                updateButtonBackground(val);
            }
        });

        size.addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();
            setPrefSize(val, val);
            button.setPrefSize(val, val);
            button.setMinSize(val, val);
            button.setMaxSize(val, val);
            updateStatusLayout();
        });

        hoverOverlay.setX(0);
        hoverOverlay.setY(0);
        hoverOverlay.widthProperty().bind(widthProperty());
        hoverOverlay.heightProperty().bind(heightProperty());

        button.setOnMouseEntered(e -> {
            if (uploadedImage.get() != null) {
                hoverOverlay.setPaint(applyAlpha(hoverColor.get(), 0.25));
                tintEffect.setTopInput(hoverOverlay);
                tintEffect.setMode(BlendMode.SRC_ATOP);
                imageView.setEffect(tintEffect);
            } else {
                updateButtonBackground(hoverColor.get());
            }
        });

        button.setOnMouseExited(e -> {
            if (uploadedImage.get() != null) {
                imageView.setEffect(null);
                updateButtonBackground(Color.TRANSPARENT);
            } else {
                updateButtonBackground(backgroundColor.get());
            }
        });

        button.setOnMousePressed(e -> updateButtonBackground(pressedColor.get()));
        button.setOnMouseReleased(e -> {
            if (uploadedImage.get() != null) {
                hoverOverlay.setPaint(applyAlpha(hoverColor.get(), 0.25));
                tintEffect.setTopInput(hoverOverlay);
                tintEffect.setMode(BlendMode.SRC_ATOP);
                imageView.setEffect(tintEffect);
            } else {
                updateButtonBackground(hoverColor.get());
            }
        });

        statusRing.visibleProperty().bind(statusVisible);
        statusDot.visibleProperty().bind(statusVisible);
        statusRing.setManaged(false);
        statusDot.setManaged(false);
        statusRing.fillProperty().bind(statusRingColor);
        statusDot.fillProperty().bind(statusColor);

        getChildren().addAll(button, statusRing, statusDot);
    }

    private void updateVisualMode() {
        if (uploadedImage.get() != null) {
            imageView.setImage(uploadedImage.get());
            imageView.setPreserveRatio(false);
            Circle fullClip = new Circle();
            fullClip.radiusProperty().bind(size.divide(2));
            fullClip.centerXProperty().bind(size.divide(2));
            fullClip.centerYProperty().bind(size.divide(2));
            imageView.setClip(fullClip);
            imageView.setEffect(null);
            imageView.setFitWidth(getWidth());
            imageView.setFitHeight(getHeight());
            widthProperty().addListener((obs, old, val) -> imageView.setFitWidth(val.doubleValue()));
            heightProperty().addListener((obs, old, val) -> imageView.setFitHeight(val.doubleValue()));
            updateButtonBackground(Color.TRANSPARENT);
        } else {
            imageView.setImage(defaultImage.get());
            imageView.setPreserveRatio(false);
            imageView.setClip(clip);
            imageView.setEffect(tintEffect);

            imageView.layoutBoundsProperty().addListener((obs, old, bounds) -> {
                double radius = Math.min(bounds.getWidth(), bounds.getHeight()) / 2;
                clip.radiusProperty().bind(size.multiply(0.48));
                clip.setCenterX(bounds.getWidth() / 2);
                clip.setCenterY(bounds.getHeight() / 2);
            });

            fitWidth.addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) imageView.setFitWidth(newVal.doubleValue());
            });
            fitHeight.addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) imageView.setFitHeight(newVal.doubleValue());
            });

            tintColor.addListener((obs, old, newColor) -> applyTint(newColor));
            applyTint(tintColor.get());

            updateButtonBackground(backgroundColor.get());
        }
        updateStatusLayout();
    }

    private void updateStatusLayout() {
        double outerRadius = size.get() * 0.20;
        double innerRadius = size.get() * 0.12;
        double offset = size.get() * 0.14;
        double center = size.get();

        statusRing.setRadius(outerRadius);
        statusRing.setCenterX(center - offset);
        statusRing.setCenterY(center - offset);

        statusDot.setRadius(innerRadius);
        statusDot.setCenterX(center - offset);
        statusDot.setCenterY(center - offset);
    }

    private void applyTint(Color color) {
        if (color == null) {
            tintEffect.setTopInput(null);
            return;
        }

        ColorInput overlay = new ColorInput(0, 0, size.get(), size.get(), color);
        tintEffect.setMode(BlendMode.SRC_ATOP);
        tintEffect.setTopInput(overlay);

        size.addListener((obs, oldVal, newVal) -> {
            overlay.setWidth(newVal.doubleValue());
            overlay.setHeight(newVal.doubleValue());
        });
    }

    private void updateButtonBackground(Color color) {
        button.setBackground(new Background(new BackgroundFill(color, new CornerRadii(100), Insets.EMPTY)));
    }

    private Color applyAlpha(Color baseColor, double alpha) {
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }

    public static Color loadOrGenerateColor() {
        String saved = PREFS.get(COLOR_PREF_KEY, null);
        if (saved != null) {
            try {
                return Color.web(saved);
            } catch (IllegalArgumentException ignored) {}
        }
        Color generated = Color.hsb(RANDOM.nextDouble() * 360, 0.6, 0.85);
        PREFS.put(COLOR_PREF_KEY, toHex(generated));
        return generated;
    }

    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public Image getDefaultImage() {
        return defaultImage.get();
    }

    public void setDefaultImage(Image value) {
        defaultImage.set(value);
    }

    public ObjectProperty<Image> defaultImageProperty() {
        return defaultImage;
    }

    public Image getUploadedImage() {
        return uploadedImage.get();
    }

    public void setUploadedImage(Image value) {
        uploadedImage.set(value);
    }

    public ObjectProperty<Image> uploadedImageProperty() {
        return uploadedImage;
    }

    public double getSize() {
        return size.get();
    }

    public void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(Color color) {
        backgroundColor.set(color);
        PREFS.put(COLOR_PREF_KEY, toHex(color));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public Color getHoverColor() {
        return hoverColor.get();
    }

    public void setHoverColor(Color color) {
        hoverColor.set(color);
    }

    public ObjectProperty<Color> hoverColorProperty() {
        return hoverColor;
    }

    public Color getPressedColor() {
        return pressedColor.get();
    }

    public void setPressedColor(Color color) {
        pressedColor.set(color);
    }

    public ObjectProperty<Color> pressedColorProperty() {
        return pressedColor;
    }

    public double getFitWidth() {
        return fitWidth.get();
    }

    public void setFitWidth(double value) {
        fitWidth.set(value);
    }

    public DoubleProperty fitWidthProperty() {
        return fitWidth;
    }

    public double getFitHeight() {
        return fitHeight.get();
    }

    public void setFitHeight(double value) {
        fitHeight.set(value);
    }

    public DoubleProperty fitHeightProperty() {
        return fitHeight;
    }

    public Color getTintColor() {
        return tintColor.get();
    }

    public void setTintColor(Color color) {
        tintColor.set(color);
    }

    public ObjectProperty<Color> tintColorProperty() {
        return tintColor;
    }

    public boolean isStatusVisible() {
        return statusVisible.get();
    }

    public void setStatusVisible(boolean value) {
        statusVisible.set(value);
    }

    public BooleanProperty statusVisibleProperty() {
        return statusVisible;
    }

    public Color getStatusColor() {
        return statusColor.get();
    }

    public void setStatusColor(Color color) {
        statusColor.set(color);
    }

    public ObjectProperty<Color> statusColorProperty() {
        return statusColor;
    }

    public Color getStatusRingColor() {
        return statusRingColor.get();
    }

    public void setStatusRingColor(Color color) {
        statusRingColor.set(color);
    }

    public ObjectProperty<Color> statusRingColorProperty() {
        return statusRingColor;
    }
}