
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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 *
 * @ONYX-FX
 *
 * A class for managing switchable views,
 * acting as an index to switch between nodes and javafx controls or elements.
 *
 * @author Mohammed Joharji
 * @version 1.0
 * @since 2025
 *
 */

// The class extends StackPane, which allows stacking children on top of each other.
public class OFxSwitcher extends StackPane {

    // IntegerProperty to store the index of the currently visible child.
    // The default value is set to -1, meaning no child is visible initially.
    private final IntegerProperty index = new SimpleIntegerProperty(this, "index", -1);

    // Constructor for OFxSwitcher class.
    public OFxSwitcher(){
        // Adding a listener to the 'index' property to trigger an update of child visibility whenever 'index' changes.
        index.addListener((obs, oldVal, newVal) -> updateVisibilityChild());
    }

    // Getter method for 'index' property.
    public int getIndex() {
        return index.get();
    }

    // Setter method for 'index' property. It sets a new index and updates the visibility of child nodes.
    public void setIndex(int index){
        this.index.set(index);
    }

    // Method to update visibility and manage the child nodes based on the 'index' property.
    // Only the child at the 'index' will be visible and managed (i.e., it can be interacted with).
    private void updateVisibilityChild() {
        int index = this.index.get();

        // Iterating through the children of the StackPane (all the child nodes added to the StackPane).
        for (int j = 0; j < getChildren().size(); j++) {
            Node child = getChildren().get(j);

            // Setting the 'managed' property to true or false for each child.
            // A node that is managed is part of the layout and can be interacted with.
            child.setManaged(j == index);

            // Setting the 'visible' property to true or false for each child.
            // A node that is visible is shown on the screen.
            child.setVisible(j == index);
        }
    }

    // Overridden layoutChildren method, which is called when layout calculations are done.
    // It updates the visibility of children every time the layout is recalculated.
    @Override
    protected void layoutChildren(){
        // Calling the superclass method to ensure proper layout behavior.
        super.layoutChildren();
        // Updating the visibility of children based on the current 'index' value.
        updateVisibilityChild();
    }
}
