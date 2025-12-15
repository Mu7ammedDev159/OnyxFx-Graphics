package org.onyxfx.graphics.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

public class OFxSensitiveLabel extends Label {

    private final BooleanProperty masked =
            new SimpleBooleanProperty(this, "isMasked", true);

    private final StringProperty maskChar =
            new SimpleStringProperty(this, "Mask Character", "*");

    // runtime backup of real text
    private String originalText = "";

    // prevents infinite update loops
    private boolean internalUpdate = false;

    public OFxSensitiveLabel() {
        init();
    }

    public OFxSensitiveLabel(String text) {
        this();
        setText(text);
    }

    private void init() {

        // Capture initial text IF already available (runtime constructor case)
        originalText = getText();

        // Re-render when masked changes
        masked.addListener((obs, oldVal, newVal) -> updateDisplayedText());

        // Listen to text changes
        textProperty().addListener((obs, oldVal, newVal) -> {
            // Ignore only internal rendering updates
            if (internalUpdate) return;

            // ALWAYS accept runtime updates
            originalText = newVal;

            // Re-render if currently masked
            if (isMasked()) {
                updateDisplayedText();
            }
        });

        updateDisplayedText();
        getStyleClass().add("ofx-sensitive-label");
    }

    private void updateDisplayedText() {
        internalUpdate = true;

        if (isMasked()) {
            super.setText(mask(originalText));
        } else {
            super.setText(originalText);
        }

        internalUpdate = false;
    }

    private String mask(String text) {
        if (text == null || text.isEmpty()) return "";
        return maskChar.get().repeat(text.length());
    }

    private String maskRange(String text, int beginIndex, int endIndex) {
        if (text == null || text.isEmpty()) return "";

        int len = text.length();

        // Safety correction
        beginIndex = Math.max(0, beginIndex);
        endIndex = Math.min(len, endIndex);

        if (beginIndex >= endIndex) return text; // nothing to mask

        String mask = getMaskCharacter();
        if (mask == null || mask.isEmpty()) mask = "*";

        StringBuilder sb = new StringBuilder(len);

        // Part 1: before mask
        sb.append(text, 0, beginIndex);

        // Part 2: masked area
        sb.append(mask.repeat(endIndex - beginIndex));

        // Part 3: after mask
        sb.append(text.substring(endIndex));

        return sb.toString();
    }

    public String maskFirst(int count, String text) {
        return maskRange(text, 0, count);
    }

    public String maskLast(int count, String text) {
        int len = text.length();
        return maskRange(text, len - count, len);
    }

    // --- Public API ---

    public void setMaskCharacter(String value) {
        maskChar.set(value);
        if (isMasked()) updateDisplayedText();
    }

    public String getMaskCharacter() {
        return maskChar.get();
    }

    public StringProperty maskCharacterProperty() {
        return maskChar;
    }

    public boolean isMasked() {
        return masked.get();
    }

    public void setMasked(boolean value) {
        masked.set(value);
    }

    public BooleanProperty maskedProperty() {
        return masked;
    }

    // Convenience actions
    public void show()   { setMasked(false); }
    public void hide()   { setMasked(true); }
    public void toggle() { setMasked(!isMasked()); }

    /** Mask a range IN-PLACE on the Label */
    public void maskRange(int beginIndex, int endIndex) {
        if (getText() == null) return;
        String updated = maskRange(getText(), beginIndex, endIndex);
        super.setText(updated);
    }

    /** Mask the first N characters IN-PLACE */
    public void maskFirst(int count) {
        if (getText() == null) return;
        String updated = maskRange(getText(), 0, count);
        super.setText(updated);
    }

    /** Mask the last N characters IN-PLACE */
    public void maskLast(int count) {
        if (getText() == null) return;
        int len = getText().length();
        String updated = maskRange(getText(), len - count, len);
        super.setText(updated);
    }
}
