/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.ui.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mucommander.ui.main.MainFrame;

/**
 * This class manages keyboard associations with {@link MuAction} ids.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionKeymap {

    /** Maps action id instances onto Keystroke instances */
    private static Map<ActionId, KeyStroke> customPrimaryActionKeymap = new HashMap<>();
    /** Maps action id instances onto alt Keystroke instances */
    private static Map<ActionId, KeyStroke> customAlternateActionKeymap = new HashMap<>();
    /** Maps Keystroke instances onto action id */
    private static AcceleratorMap acceleratorMap = new AcceleratorMap();

    /******************
     * Public Methods *
     ******************/

    /**
     * Register all action shortcuts to the given MainFrame's file tables.
     * 
     * @param mainFrame
     *            - MainFrame instance to which all action shortcuts would be registered.
     */
    public static void registerActions(MainFrame mainFrame) {
        for (ActionId actionId : ActionManager.getActionIds()) {
            ActionDescriptor actionDescriptor = ActionProperties.getActionDescriptor(actionId);

            // Instantiate the action only if it is not parameterized: parameterized actions should only be instantiated
            // when they are needed and with the required parameters.
            if (!actionDescriptor.isParameterized()) {
                registerAction(mainFrame, ActionManager.getActionInstance(actionId, mainFrame));
            }
        }
    }

    /**
     * Register MuAction to JComponent with the given condition.
     * 
     * @param action
     *            - MuAction instance.
     * @param comp
     *            - JComponent to which the action would be registered
     * @param condition
     *            - condition in which the action could be invoked.
     */
    public static void registerActionAccelerators(MuAction action, JComponent comp, int condition) {
        registerActionAccelerator(action, action.getAccelerator(), comp, condition);
        registerActionAccelerator(action, action.getAlternateAccelerator(), comp, condition);
    }

    /**
     * Register new accelerators to the given action.
     * 
     * Note: if accelerator is null, it would be replaced by altAccelerator.
     * 
     * @param actionId
     *            - id of the MuAction.
     * @param accelerator
     *            - KeyStroke that would be primary accelerator of the given action.
     * @param altAccelerator
     *            - KeyStroke that would be alternative accelerator of the given action.
     */
    public static void changeActionAccelerators(ActionId actionId, KeyStroke accelerator, KeyStroke altAccelerator) {
        var curAccelerator = ActionKeymap.getAccelerator(actionId);
        var curAltAccelerator = ActionKeymap.getAlternateAccelerator(actionId);
        // Check whether the given actions are already assigned to the given action
        if (Objects.equals(accelerator, curAccelerator) && Objects.equals(altAccelerator, curAltAccelerator))
            return;

        // If primary accelerator is already registered to other MuAction, unregister it.
        ActionId previousActionForAccelerator = getRegisteredActionIdForKeystroke(accelerator);
        if (previousActionForAccelerator != null &&
                isSameType(previousActionForAccelerator, actionId) &&
                !previousActionForAccelerator.equals(actionId)) {
            unregisterAcceleratorFromAction(previousActionForAccelerator, accelerator);
        }

        // If alternative accelerator is already registered to other MuAction, unregister it.
        ActionId previousActionForAltAccelerator = getRegisteredActionIdForKeystroke(altAccelerator);
        if (previousActionForAltAccelerator != null &&
                isSameType(previousActionForAltAccelerator, actionId) &&
                !previousActionForAltAccelerator.equals(actionId)) {
            unregisterAcceleratorFromAction(previousActionForAltAccelerator, altAccelerator);
        }

        // Remove action's previous accelerators (primary and alternate)
        acceleratorMap.remove(customPrimaryActionKeymap.remove(actionId));
        acceleratorMap.remove(customAlternateActionKeymap.remove(actionId));

        // Register new accelerators
        registerActionAccelerators(actionId, accelerator, altAccelerator);
    }

    private static boolean isSameType(ActionId id1, ActionId id2) {
        return !(id1.getType() == ActionId.ActionType.TERMINAL ^ id2.getType() == ActionId.ActionType.TERMINAL);
    }

    private static void unregisterAcceleratorFromAction(ActionId actionId, KeyStroke accelerator) {
        switch (getAcceleratorType(accelerator)) {
        case PRIMARY:
            registerActionAccelerators(actionId, null, getAlternateAccelerator(actionId));
            break;
        case ALTERNATIVE:
            registerActionAccelerators(actionId, getAccelerator(actionId), null);
            break;
        }
    }

    /**
     * Register all primary and alternative accelerator given.
     * 
     * @param primary
     *            - HashMap that maps action id to primary accelerator.
     * @param alternate
     *            - HashMap that maps action id to alternative accelerator.
     */
    public static void registerActions(Map<ActionId, KeyStroke> primary, Map<ActionId, KeyStroke> alternate) {
        for (ActionId actionId : primary.keySet()) {
            // Add the action/keystroke mapping
            ActionKeymap.registerActionAccelerators(
                    actionId,
                    primary.get(actionId),
                    alternate.get(actionId));
        }
    }

    /**
     * Check whether an accelerator is registered to MuAction.
     * 
     * @param ks
     *            - accelerator.
     * @return true if the given accelerator is registered to whatever MuAction, false otherwise.
     */
    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
        return getRegisteredActionIdForKeystroke(ks) != null;
    }

    /**
     * Check whether the action has accelerator assigned to it.
     * 
     * @param actionId
     *            - id of MuAction.
     * @return true if there is a shortcut which is assigned to the action, false otherwise.
     */
    public static boolean doesActionHaveShortcut(ActionId actionId) {
        return getAccelerator(actionId) != null;
    }

    ///////////////////
    ///// getters /////
    ///////////////////

    /**
     * Return primary accelerator of MuAction.
     * 
     * @param actionId
     *            - id of MuAction.
     * @return primary accelerator of the given MuAction.
     */
    public static KeyStroke getAccelerator(ActionId actionId) {
        if (customPrimaryActionKeymap.containsKey(actionId))
            return customPrimaryActionKeymap.get(actionId);
        return ActionProperties.getDefaultAccelerator(actionId);
    }

    /**
     * Return alternative accelerator of MuAction.
     * 
     * @param actionId
     *            - id of MuAction.
     * @return alternative accelerator of the given MuAction.
     */
    public static KeyStroke getAlternateAccelerator(ActionId actionId) {
        if (customAlternateActionKeymap.containsKey(actionId))
            return customAlternateActionKeymap.get(actionId);
        return ActionProperties.getDefaultAlternativeAccelerator(actionId);
    }

    /**
     * Return the id of MuAction to which accelerator is registered.
     * 
     * @param ks
     *            - accelerator.
     * @return id of MuAction to which the given accelerator is registered, null if the accelerator is not registered.
     */
    public static ActionId getRegisteredActionIdForKeystroke(KeyStroke ks) {
        ActionId actionId = acceleratorMap.getActionId(ks);
        return actionId != null ? actionId : ActionProperties.getDefaultActionForKeyStroke(ks);
    }

    /**
     * Return accelerator type: primary\alternative\not registered.
     * 
     * @param ks
     *            - accelerator.
     * @return accelerator type.
     */
    private static AcceleratorMap.AcceleratorType getAcceleratorType(KeyStroke ks) {
        AcceleratorMap.AcceleratorType type = acceleratorMap.getAcceleratorType(ks);
        return type != null ? type : ActionProperties.getDefaultAcceleratorType(ks);
    }

    /**
     * Return ids of actions that their registered accelerators are different from their default accelerators.
     * 
     * @return Iterator of actions that their accelerators were customized.
     */
    public static Iterator<ActionId> getCustomizedActions() {
        HashSet<ActionId> modifiedActions = new HashSet<>();
        modifiedActions.addAll(customPrimaryActionKeymap.keySet());
        modifiedActions.addAll(customAlternateActionKeymap.keySet());
        return modifiedActions.iterator();
    }

    /*******************
     * Private Methods *
     *******************/

    /**
     * Register MuAction instance to MainFrame instance.
     */
    private static void registerAction(MainFrame mainFrame, MuAction action) {
        registerActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    /**
     * Register accelerator of MuAction to JComponent with a condition that states when the action can be invoked.
     */
    private static void registerActionAccelerator(MuAction action,
            KeyStroke accelerator,
            JComponent comp,
            int condition) {
        if (accelerator != null) {
            InputMap inputMap = comp.getInputMap(condition);
            ActionMap actionMap = comp.getActionMap();
            ActionId actionId = ActionId.asGenericAction(action.getDescriptor().getId());
            inputMap.put(accelerator, actionId);
            actionMap.put(actionId, action);
        }
    }

    /**
     * Unregister MuAction instance from MainFrame instance.
     */
    private static void unregisterAction(MainFrame mainFrame, MuAction action) {
        unregisterActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    /**
     * Unregister MuAction from JComponent.
     */
    private static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
        unregisterActionAccelerator(action, action.getAccelerator(), comp, condition);
        unregisterActionAccelerator(action, action.getAlternateAccelerator(), comp, condition);
    }

    /**
     * Unregister accelerator of MuAction from JComponent.
     */
    private static void unregisterActionAccelerator(MuAction action,
            KeyStroke accelerator,
            JComponent comp,
            int condition) {
        if (accelerator != null) {
            InputMap inputMap = comp.getInputMap(condition);
            ActionMap actionMap = comp.getActionMap();
            inputMap.remove(accelerator);
            ActionId actionId = ActionId.asGenericAction(action.getDescriptor().getId());
            actionMap.remove(actionId);
        }
    }

    /**
     * Register primary and alternative accelerators to MuAction.
     */
    private static void registerActionAccelerators(ActionId actionId,
            KeyStroke accelerator,
            KeyStroke alternateAccelerator) {
        // If accelerator is null, replace it with alternateAccelerator
        if (accelerator == null) {
            accelerator = alternateAccelerator;
            alternateAccelerator = null;
        }

        var defaultAccelerator = ActionProperties.getDefaultAccelerator(actionId);
        var defaultAltAccelerator = ActionProperties.getDefaultAlternativeAccelerator(actionId);
        // New accelerators are identical to the default action's accelerators
        if (Objects.equals(accelerator, defaultAccelerator) &&
                Objects.equals(alternateAccelerator, defaultAltAccelerator)) {
            // Remove all action's accelerators customization
            customPrimaryActionKeymap.remove(actionId);
            customAlternateActionKeymap.remove(actionId);
            acceleratorMap.remove(accelerator);
            acceleratorMap.remove(alternateAccelerator);
        }
        // New accelerators are different from the default accelerators
        else {
            customPrimaryActionKeymap.put(actionId, accelerator);
            acceleratorMap.putAccelerator(accelerator, actionId);

            customAlternateActionKeymap.put(actionId, alternateAccelerator);
            acceleratorMap.putAlternativeAccelerator(alternateAccelerator, actionId);
        }

        if (actionId.getType() != ActionId.ActionType.TERMINAL) {
            // Update each MainFrame's action instance and input map
            for (var action : ActionManager.getActionInstances(actionId)) {
                updateActionInstance(action, accelerator, alternateAccelerator);
            }
        }
    }

    private static void updateActionInstance(MuAction action, KeyStroke accelerator, KeyStroke alternateAccelerator) {
        MainFrame mainFrame = action.getMainFrame();

        // Remove action from MainFrame's action and input maps
        unregisterAction(mainFrame, action);

        // Change action's accelerators
        action.setAccelerator(accelerator);
        action.setAlternateAccelerator(alternateAccelerator);

        // Add updated action to MainFrame's action and input maps
        registerAction(mainFrame, action);
    }
}
