
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

/**
 * <p>
 * {@code OFxSensitiveLabel} is a JavaFX {@link Label} that supports
 * masking and unmasking of sensitive textual information (e.g. IPs,
 * ports, tokens, secrets).
 * </p>
 *
 * <p>
 * The component preserves the original text internally while rendering
 * a masked representation in the UI. This allows safe toggling between
 * visible and hidden states without losing the original value.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Toggle masking at runtime</li>
 *   <li>Customizable mask character</li>
 *   <li>Safe handling of SceneBuilder + runtime updates</li>
 *   <li>Partial masking support (ranges, first N, last N)</li>
 * </ul>
 *
 * @author OnyxFX {Mohammed Joharji}
 * @since 1.2.0
 */

public class OFxSensitiveLabel extends Label {

    /**
     * Indicates whether the label is currently masked.
     *
     * <p>
     * When {@code true}, the displayed text is replaced with mask characters.
     * When {@code false}, the original text is shown.
     * </p>
     */
    private final BooleanProperty masked =
            new SimpleBooleanProperty(this, "isMasked", true);

    /**
     * Character used to mask the sensitive text.
     *
     * <p>
     * Defaults to {@code "*"} but can be changed to any character
     * (e.g. {@code •}, {@code X}, {@code #}).
     * </p>
     */
    private final StringProperty maskChar =
            new SimpleStringProperty(this, "Mask Character", "*");

    /**
     * Runtime backup of the real (unmasked) text.
     *
     * <p>
     * This field is mandatory because {@link Label#setText(String)} is
     * destructive and cannot be overridden.
     * </p>
     */
    private String originalText = "";

    /**
     * Internal guard flag to prevent infinite update loops
     * when the control updates its own text.
     */
    private boolean internalUpdate = false;

    /**
     * Creates an empty {@code OFxSensitiveLabel}.
     */
    public OFxSensitiveLabel() {
        init();
    }

    /**
     * Creates an {@code OFxSensitiveLabel} with initial text.
     *
     * @param text initial label text
     */
    public OFxSensitiveLabel(String text) {
        this();
        setText(text);
    }

    /**
     * Initializes listeners and synchronizes SceneBuilder/runtime behavior.
     *
     * @implNote
     * This method captures the initial text, handles runtime updates,
     * and ensures masking behaves consistently both in SceneBuilder
     * previews and at application runtime.
     */
    private void init() {

        // Capture initial text if set via SceneBuilder or constructor
        originalText = getText();

        // Re-render when masked state changes
        masked.addListener((obs, oldVal, newVal) -> updateDisplayedText());

        // Listen to text changes (runtime-safe)
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (internalUpdate) return;

            // Always accept runtime updates as the new source of truth
            originalText = newVal;

            // Re-render immediately if currently masked
            if (isMasked()) {
                updateDisplayedText();
            }
        });

        updateDisplayedText();
        getStyleClass().add("ofx-sensitive-label");
    }

    /**
     * Updates the visible text based on the current masked state.
     *
     * <p>
     * This method never mutates {@code originalText}.
     * It only controls rendering.
     * </p>
     */
    private void updateDisplayedText() {
        internalUpdate = true;

        if (isMasked()) {
            super.setText(mask(originalText));
        } else {
            super.setText(originalText);
        }

        internalUpdate = false;
    }

    /**
     * Masks an entire string using the current mask character.
     *
     * @param text the original text
     * @return masked text with the same length
     */
    private String mask(String text) {
        if (text == null || text.isEmpty()) return "";
        return maskChar.get().repeat(text.length());
    }

    /**
     * Masks a specific range of characters in a string.
     *
     * @param text the original text
     * @param beginIndex inclusive start index
     * @param endIndex exclusive end index
     * @return partially masked string
     */
    private String maskRange(String text, int beginIndex, int endIndex) {
        if (text == null || text.isEmpty()) return "";

        int len = text.length();

        beginIndex = Math.max(0, beginIndex);
        endIndex = Math.min(len, endIndex);

        if (beginIndex >= endIndex) return text;

        String mask = getMaskCharacter();
        if (mask == null || mask.isEmpty()) mask = "*";

        StringBuilder sb = new StringBuilder(len);
        sb.append(text, 0, beginIndex);
        sb.append(mask.repeat(endIndex - beginIndex));
        sb.append(text.substring(endIndex));

        return sb.toString();
    }

    /**
     * Masks the first {@code count} characters of the given text.
     *
     * @param count number of characters to mask
     * @param text source text
     * @return masked text
     */
    public String maskFirst(int count, String text) {
        return maskRange(text, 0, count);
    }

    /**
     * Masks the last {@code count} characters of the given text.
     *
     * @param count number of characters to mask
     * @param text source text
     * @return masked text
     */
    public String maskLast(int count, String text) {
        int len = text.length();
        return maskRange(text, len - count, len);
    }

    /* =========================
       Public API
       ========================= */

    /**
     * Sets the mask character.
     *
     * @param value new mask character
     */
    public void setMaskCharacter(String value) {
        maskChar.set(value);
        if (isMasked()) updateDisplayedText();
    }

    /**
     * @return current mask character
     */
    public String getMaskCharacter() {
        return maskChar.get();
    }

    /**
     * @return mask character property
     */
    public StringProperty maskCharacterProperty() {
        return maskChar;
    }

    /**
     * @return {@code true} if the label is masked
     */
    public boolean isMasked() {
        return masked.get();
    }

    /**
     * Sets whether the label is masked.
     *
     * @param value {@code true} to mask, {@code false} to show
     */
    public void setMasked(boolean value) {
        masked.set(value);
    }

    /**
     * @return masked state property
     */
    public BooleanProperty maskedProperty() {
        return masked;
    }

    /** Shows the original text. */
    public void show()   { setMasked(false); }

    /** Hides the text using masking. */
    public void hide()   { setMasked(true); }

    /** Toggles between masked and unmasked states. */
    public void toggle() { setMasked(!isMasked()); }

    /**
     * Masks a specific range of characters directly on the label.
     *
     * @param beginIndex start index
     * @param endIndex end index
     */
    public void maskRange(int beginIndex, int endIndex) {
        if (getText() == null) return;
        String updated = maskRange(getText(), beginIndex, endIndex);
        super.setText(updated);
    }

    /**
     * Masks the first {@code count} characters directly on the label.
     *
     * @param count number of characters to mask
     */
    public void maskFirst(int count) {
        if (getText() == null) return;
        String updated = maskRange(getText(), 0, count);
        super.setText(updated);
    }

    /**
     * Masks the last {@code count} characters directly on the label.
     *
     * @param count number of characters to mask
     */
    public void maskLast(int count) {
        if (getText() == null) return;
        int len = getText().length();
        String updated = maskRange(getText(), len - count, len);
        super.setText(updated);
    }
}
