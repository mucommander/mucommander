/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.list;

import com.mucommander.text.Translator;
import com.mucommander.util.AlteredVector;
import com.mucommander.util.VectorChangeListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * DynamicList extends JList to work with an {@link AlteredVector} of items which values can be dynamically modified
 * and automatically reflected in the list, also keeping the current selection consistent.
 *
 * <p>It also provides actions to:
 * <ul>
 * <li>move the currently selected item up (mapped to 'Shift+UP' and 'Shift+LEFT')
 * <li>move the currently selected item down (mapped to 'Shift+DOWN' and 'Shift+RIGHT')
 * <li>remove the currently selected item and selects the previous one (if any) (mapped to 'DELETE' and 'BACKSPACE')
 * </ul>
 *
 * <p>This list only works in 'single selection mode', that means only one item can be selected at a time.
 *
 * @author Maxence Bernard
 */
public class DynamicList extends JList {

    /** Items displayed in the JList */
    private AlteredVector items;

    /** Custom ListModel that handles modifications made to the AlteredVector */
    private DynamicListModel model;

    /** Action instance which moves the currently selected item up when triggered */
    private MoveUpAction moveUpAction;

    /** Action instance which moves the currently selected item down when triggered */
    private MoveDownAction moveDownAction;

    /** Action instance which, when triggered, removes the currently selected item from the list
     * and selects the previous item (if any). */
    private RemoveAction removeAction;


    /**
     * Custom ListModel that handles modifications made to the AlteredVector and reflect them changes in the JList.
     */
    private class DynamicListModel extends AbstractListModel implements VectorChangeListener {

        public int getSize() {
            return items.size();
        }

        public Object getElementAt(int i) {
            if(i<0 || i>=items.size())
                return null;

            return items.elementAt(i);
        }

        private void notifyAdded(int fromIndex, int toIndex) {
            fireIntervalAdded(this, fromIndex, toIndex);
        }

        private void notifyRemoved(int fromIndex, int toIndex) {
            fireIntervalRemoved(this, fromIndex, toIndex);
        }

        private void notifyModified(int index) {
            fireContentsChanged(this, index, index);
        }

        //////////////////////////
        // VectorChangeListener //
        //////////////////////////

        public void elementsAdded(int startIndex, int nbAdded) {
            model.notifyAdded(startIndex, startIndex+nbAdded-1);
        }

        public void elementsRemoved(int startIndex, int nbRemoved) {
            model.notifyRemoved(startIndex, startIndex+nbRemoved-1);
        }

        public void elementChanged(int index) {
            model.notifyModified(index);
        }
    }


    /**
     * Action which moves the currently selected item up when triggered.
     */
    private class MoveUpAction extends AbstractAction {

        private MoveUpAction() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            moveItem(getSelectedIndex(), true);

            // Request focus back on the list
            requestFocus();
        }
    }


    /**
     * Action which moves the currently selected item down when triggered.
     */
    private class MoveDownAction extends AbstractAction {

        private MoveDownAction() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            moveItem(getSelectedIndex(), false);

            // Request focus back on the list
            requestFocus();
        }
    }


    /**
     * Action which, when triggered, removes the currently selected item from the list and selects the previous item (if any).
     */
    private class RemoveAction extends AbstractAction {

        private RemoveAction() {
            putValue(Action.NAME, Translator.get("delete"));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            int selectedIndex = getSelectedIndex();

            if(!isIndexValid(selectedIndex))
                return;

            items.removeElementAt(selectedIndex);

            // Select previous item (if there is one) and make sure it is visible.
            int nbItems = items.size();
            if(nbItems>0)
                selectAndScroll(Math.min(selectedIndex, nbItems-1));
            
            // Request focus back on the list
            requestFocus();
        }
    }


    /**
     * Creates a new DynamicList using the items stored in the given {@link AlteredVector}.
     * These items (if any) will be visible whenever this list is visible, and the first item (if any) will be selected.
     *
     * <p>Any change made to the AlteredVector will be automatically reflected in the list, except for changes
     * made to the item instances themselves for which {@link #itemModified(int, boolean)} will need to
     * be called explicitely.
     *
     * @param items items to add to the list
     */
    public DynamicList(AlteredVector items) {
        this.items = items;

        // Use a custom ListModel
        this.model = new DynamicListModel();

        setModel(model);
        
        // Listen to changes made to the Vector
        this.items.addVectorChangeListener(model);

        // Allow only one item to be selected at a time
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Select first item, if there is at least one
        if(items.size()>0)
           setSelectedIndex(0);

        // Create action instances
        this.moveUpAction = new MoveUpAction();
        this.moveDownAction = new MoveDownAction();
        this.removeAction = new RemoveAction();

        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();

        // Map 'Delete' and 'Backspace' to RemoveAction
        Class actionClass = removeAction.getClass();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), actionClass);
        actionMap.put(actionClass, removeAction);

        // Map 'Shift+Up'/'Meta+Up' and 'Shift+Left'/'Meta+Left' to MoveUpAction
        actionClass = moveUpAction.getClass();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_MASK), actionClass);
        actionMap.put(actionClass, moveUpAction);

        // Map 'Shift+Down'/'Meta+Down' and 'Shift+Right'/'Meta+Right' to MoveDownAction
        actionClass = moveDownAction.getClass();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK), actionClass);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_MASK), actionClass);
        actionMap.put(actionClass, moveDownAction);
    }


    /**
     * Returns the items displayed by this DynamicList.
     */
    public AlteredVector getItems() {
        return items;
    }


    /**
     * Selects the item located at the given index and if necessary scrolls the list to make sure
     * that the new selection is visible within the viewport.
     *
     * @param index index of the item to select
     */
    public void selectAndScroll(int index) {
        setSelectedIndex(index);
        ensureIndexIsVisible(index);
    }


    /**
     * Returns true if the given index is without the bounds of the items Vector.
     *
     * @param index index to test
     * @return true if the given index is without the bounds of the items.
     */
    public boolean isIndexValid(int index) {
        return index>=0 && index<items.size();
    }


    /**
     * This method should be called whenever an item in the items vector has been modified in order to properly
     * repaint the list and reflect the change.
     *
     * @param index index of the item in the Vector that has been modified
     * @param selectItem if true, the modified item will be selected
     */
    public void itemModified(int index, boolean selectItem) {
        // Make sure that the given index is not out of bounds
        if(!isIndexValid(index))
            return;

        // Notify ListModel in order to properly repaint list
        model.notifyModified(index);
    }


    /**
     * Moves the item located at the given index up or down, swapping its place with the previous or next item.
     *
     * @param index the item to move
     * @param moveUp if true the item at the given index will be moved up, if not moved down
     */
    public void moveItem(int index, boolean moveUp) {
        // Make sure that the given index is not out of bounds
        if(!isIndexValid(index))
            return;

        int newIndex;

        // Calculate the new index for the item to move
        if (moveUp)  {
            // Item is already at the top, do nothing
            if(index<1)
                return;

            newIndex = index-1;
        }
        else {
            // Item is already at the bottom, do nothing
            if(index>=items.size()-1)
                return;

            newIndex = index+1;
        }

        // Swap values in the Vector
        Object tmp = items.elementAt(index);
        items.setElementAt(items.elementAt(newIndex), index);
        items.setElementAt(tmp, newIndex);

        // Select moved item and make sure it is visible
        selectAndScroll(newIndex);
    }


    /**
     * Returns an Action that can be used for instance in a JButton to
     * move the item currently selected item up, swapping it with the previous item.
     *
     * @return an Action that moves the currently selected item up.
     */
    public Action getMoveUpAction() {
        return moveUpAction;
    }

    /**
     * Returns an Action that can be used for instance in a JButton to
     * move the item currently selected item down, swapping it with the following item.
     *
     * @return an Action that moves the currently selected item down.
     */
    public Action getMoveDownAction() {
        return moveDownAction;
    }


    /**
     * Returns an Action that can be used for instance in a JButton to
     * remove the currently selected item.
     *
     * @return an Action that removes the currently selected item.
     */
    public Action getRemoveAction() {
        return removeAction;
    }
}
