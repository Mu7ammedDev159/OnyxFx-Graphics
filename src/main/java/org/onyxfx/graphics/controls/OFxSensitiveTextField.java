
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
import javafx.scene.control.TextField;

/**
 * {@code OFxSensitiveTextField} is a specialized {@link TextField} that supports
 * masking and unmasking of sensitive textual information (e.g. Passwords, Mobile Numbers,
 * credentials) at runtime.
 *
 * <p>
 * The control preserves the real (original) text internally while rendering
 * a masked representation to the UI when masking is enabled.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Toggleable masking via {@link #setMasked(boolean)}</li>
 *   <li>Customizable mask character</li>
 *   <li>Safe handling of runtime {@code setText(...)} calls</li>
 *   <li>SceneBuilder-friendly initialization</li>
 *   <li>Partial masking utilities (range / first / last)</li>
 * </ul>
 *
 * <p>
 * This control is designed to behave consistently in both
 * SceneBuilder and runtime usage.
 * </p>
 *
 * @author OnyxFX {Mohammed Joharji}
 * @since 1.2.0
 */

public class OFxSensitiveTextField extends TextField {

    /**
     * Indicates whether the text is currently masked.
     * <p>
     * When {@code true}, the UI shows a masked version of the text.
     * When {@code false}, the original text is shown.
     * </p>
     */
    private final BooleanProperty masked =
            new SimpleBooleanProperty(this, "isMasked", true);

    /**
     * Character(s) used to mask the visible text.
     * <p>
     * Defaults to {@code "*"} but can be customized.
     * </p>
     */
    private final StringProperty maskChar =
            new SimpleStringProperty(this, "Mask Character", "*");

    /**
     * Internal backup of the real, unmasked text.
     *
     * <p>
     * This value represents the source of truth and is never modified
     * by masking operations.
     * </p>
     *
     * @implNote This is required because {@link TextField#setText(String)}
     * is destructive and cannot be overridden.
     */
    private String originalText = "";

    /**
     * Guard flag to prevent infinite update loops when rendering
     * masked/unmasked content.
     */
    private boolean internalUpdate = false;

    /** Creates an empty sensitive text field. */
    public OFxSensitiveTextField() {
        init();
    }

    /**
     * Creates a sensitive text field with initial text.
     *
     * @param text initial text value
     */
    public OFxSensitiveTextField(String text) {
        this();
        setText(text);
    }

    /**
     * Initializes listeners and captures initial text state.
     *
     * <p>
     * This method ensures correct behavior when:
     * </p>
     * <ul>
     *   <li>Text is set via SceneBuilder</li>
     *   <li>Text is updated at runtime</li>
     *   <li>Masking state is toggled</li>
     * </ul>
     */
    private void init() {

        // Capture initial text from SceneBuilder / constructor (if present)
        originalText = getText();

        // Re-render whenever masking state changes
        masked.addListener((obs, oldVal, newVal) -> updateDisplayedText());

        // Listen for any external text changes (runtime, typing, FXML)
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (internalUpdate) return;

            // Always treat new values as the real source text
            originalText = newVal;

            // Maintain masked appearance if currently masked
            if (isMasked()) {
                updateDisplayedText();
            }
        });

        updateDisplayedText();
        getStyleClass().add("ofx-sensitive-textfield");
    }

    /**
     * Renders the appropriate text representation based on masking state.
     *
     * <p>
     * This method never modifies {@link #originalText}.
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
     * Masks an entire string using the configured mask character.
     *
     * @param text the text to mask
     * @return masked string
     */
    private String mask(String text) {
        if (text == null || text.isEmpty()) return "";
        return getMaskCharacter().repeat(text.length());
    }

    /**
     * Masks a specific range of characters within a string.
     *
     * @param text       source text
     * @param beginIndex start index (inclusive)
     * @param endIndex   end index (exclusive)
     * @return partially masked string
     */
    private String maskRange(String text, int beginIndex, int endIndex) {
        if (text == null || text.isEmpty()) return "";

        int len = text.length();
        beginIndex = Math.max(0, beginIndex);
        endIndex = Math.min(len, endIndex);

        if (beginIndex >= endIndex) return text;

        String mask = getMaskCharacter();

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
     * @param text  source text
     * @return masked string
     */
    public String maskFirst(int count, String text) {
        return maskRange(text, 0, count);
    }

    /**
     * Masks the last {@code count} characters of the given text.
     *
     * @param count number of characters to mask
     * @param text  source text
     * @return masked string
     */
    public String maskLast(int count, String text) {
        int len = (text == null) ? 0 : text.length();
        return maskRange(text, len - count, len);
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    /**
     * Sets the character used for masking.
     *
     * @param value mask character (defaults to "*")
     */
    public void setMaskCharacter(String value) {
        maskChar.set(value);
        if (isMasked()) updateDisplayedText();
    }

    /**
     * Returns the current mask character.
     *
     * @return mask character
     */
    public String getMaskCharacter() {
        String c = maskChar.get();
        return (c == null || c.isEmpty()) ? "*" : c;
    }

    /** @return mask character property */
    public StringProperty maskCharacterProperty() {
        return maskChar;
    }

    /** @return {@code true} if text is masked */
    public boolean isMasked() {
        return masked.get();
    }

    /**
     * Sets masking state.
     *
     * @param value {@code true} to mask, {@code false} to show real text
     */
    public void setMasked(boolean value) {
        masked.set(value);
    }

    /** @return masked property */
    public BooleanProperty maskedProperty() {
        return masked;
    }

    /** Shows the real text. */
    public void show() { setMasked(false); }

    /** Masks the text. */
    public void hide() { setMasked(true); }

    /** Toggles between masked and unmasked states. */
    public void toggle() { setMasked(!isMasked()); }

    /**
     * Masks a specific range of the original text in-place.
     *
     * @param beginIndex start index
     * @param endIndex   end index
     */
    public void maskRange(int beginIndex, int endIndex) {
        if (originalText == null) return;
        originalText = maskRange(originalText, beginIndex, endIndex);
        updateDisplayedText();
    }

    /**
     * Masks the first {@code count} characters of the original text.
     *
     * @param count number of characters
     */
    public void maskFirst(int count) {
        if (originalText == null) return;
        originalText = maskRange(originalText, 0, count);
        updateDisplayedText();
    }

    /**
     * Masks the last {@code count} characters of the original text.
     *
     * @param count number of characters
     */
    public void maskLast(int count) {
        if (originalText == null) return;
        int len = originalText.length();
        originalText = maskRange(originalText, len - count, len);
        updateDisplayedText();
    }
}