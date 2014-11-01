/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.ui.action.impl.*;
import com.mucommander.ui.main.MainFrame;

/**
 * ActionManager provides methods to retrieve {@link MuAction} instances and invoke them. It keeps track of all the
 * action instances it has created and allows them to be reused within a {@link MainFrame}.
 *
 * <p>MuAction subclasses should not be instantiated directly, <code>getActionInstance</code>
 * methods should be used instead. Using ActionManager to retrieve a MuAction ensures that only one instance
 * exists for a given {@link MainFrame}. This is particularly important because actions are stateful and can be used
 * in several components of a MainFrame at the same time; if an action's state changes, the change must be reflected
 * everywhere the action is used. It is also important for performance reasons: sharing one action throughout a
 * {@link com.mucommander.ui.main.MainFrame} saves some memory and also CPU cycles as some actions listen to particular events to change
 * their state accordingly.</p>
 *
 * @see MuAction
 * @see ActionParameters
 * @see ActionKeymap
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionManager.class);
	
    /** MuAction id -> factory map */
    private static Map<String, ActionFactory> actionFactories = new Hashtable<String, ActionFactory>();
    
    /** MainFrame -> MuAction map */
    private static WeakHashMap<MainFrame, Map<ActionParameters, ActionAndIdPair>> mainFrameActionsMap = new WeakHashMap<MainFrame, Map<ActionParameters, ActionAndIdPair>>();
    
    /** Pattern to resolve the action ID from action class path */
    private final static Pattern pattern = Pattern.compile(".*\\.(.*)?Action");

    public static void registerActions() {
    	registerAction(new AddBookmarkAction.Descriptor(),                  new AddBookmarkAction.Factory());
    	registerAction(new AddTabAction.Descriptor(),						new AddTabAction.Factory());
    	registerAction(new BatchRenameAction.Descriptor(),                  new BatchRenameAction.Factory());
    	registerAction(new BringAllToFrontAction.Descriptor(),              new BringAllToFrontAction.Factory());
    	registerAction(new CalculateChecksumAction.Descriptor(),            new CalculateChecksumAction.Factory());
    	registerAction(new ChangeDateAction.Descriptor(),                   new ChangeDateAction.Factory());
    	registerAction(new ChangeLocationAction.Descriptor(),               new ChangeLocationAction.Factory());
    	registerAction(new ChangePermissionsAction.Descriptor(),            new ChangePermissionsAction.Factory());
    	registerAction(new CheckForUpdatesAction.Descriptor(),              new CheckForUpdatesAction.Factory());
    	registerAction(new CloneTabToOtherPanelAction.Descriptor(), 		new CloneTabToOtherPanelAction.Factory());
    	registerAction(new CloseDuplicateTabsAction.Descriptor(),			new CloseDuplicateTabsAction.Factory());
    	registerAction(new CloseOtherTabsAction.Descriptor(),				new CloseOtherTabsAction.Factory());
    	registerAction(new CloseWindowAction.Descriptor(),                  new CloseWindowAction.Factory());
    	registerAction(new CloseTabAction.Descriptor(),						new CloseTabAction.Factory());
//    	registerAction(new CommandAction.Descriptor(),           		     new CommandAction.Factory());
    	registerAction(new CompareFoldersAction.Descriptor(),               new CompareFoldersAction.Factory());
    	registerAction(new ConnectToServerAction.Descriptor(),              new ConnectToServerAction.Factory());
    	registerAction(new CopyAction.Descriptor(),                         new CopyAction.Factory());
    	registerAction(new CopyFileBaseNamesAction.Descriptor(),            new CopyFileBaseNamesAction.Factory());
    	registerAction(new CopyFileNamesAction.Descriptor(),                new CopyFileNamesAction.Factory());
    	registerAction(new CopyFilePathsAction.Descriptor(),                new CopyFilePathsAction.Factory());
    	registerAction(new CopyFilesToClipboardAction.Descriptor(),         new CopyFilesToClipboardAction.Factory());
    	registerAction(new FocusPreviousAction.Descriptor(),                new FocusPreviousAction.Factory());
    	registerAction(new FocusNextAction.Descriptor(),                    new FocusNextAction.Factory());
    	registerAction(new DeleteAction.Descriptor(),         			    new DeleteAction.Factory());
    	registerAction(new DonateAction.Descriptor(),    			        new DonateAction.Factory());
    	registerAction(new DuplicateTabAction.Descriptor(),						new DuplicateTabAction.Factory());
    	registerAction(new EditAction.Descriptor(),     			        new EditAction.Factory());
    	registerAction(new EditBookmarksAction.Descriptor(),                new EditBookmarksAction.Factory());
    	registerAction(new EditCredentialsAction.Descriptor(),              new EditCredentialsAction.Factory());
    	registerAction(new EmailAction.Descriptor(),          			    new EmailAction.Factory());
    	registerAction(new EmptyTrashAction.Descriptor(),           	    new EmptyTrashAction.Factory());
    	registerAction(new ExploreBookmarksAction.Descriptor(),             new ExploreBookmarksAction.Factory());
//    	registerAction(new GarbageCollectAction.Descriptor(),               new GarbageCollectAction.Factory());
    	registerAction(new GoBackAction.Descriptor(),                       new GoBackAction.Factory());
    	registerAction(new GoForwardAction.Descriptor(),                    new GoForwardAction.Factory());
    	registerAction(new GoToDocumentationAction.Descriptor(),            new GoToDocumentationAction.Factory());
    	registerAction(new GoToForumsAction.Descriptor(),                   new GoToForumsAction.Factory());
    	registerAction(new GoToHomeAction.Descriptor(),                     new GoToHomeAction.Factory());
    	registerAction(new GoToParentAction.Descriptor(),                   new GoToParentAction.Factory());
    	registerAction(new GoToParentInBothPanelsAction.Descriptor(),       new GoToParentInBothPanelsAction.Factory());
    	registerAction(new GoToParentInOtherPanelAction.Descriptor(),       new GoToParentInOtherPanelAction.Factory());
    	registerAction(new GoToRootAction.Descriptor(),                     new GoToRootAction.Factory());
    	registerAction(new GoToWebsiteAction.Descriptor(),                  new GoToWebsiteAction.Factory());
    	registerAction(new InternalEditAction.Descriptor(),                 new InternalEditAction.Factory());
    	registerAction(new InternalViewAction.Descriptor(),                 new InternalViewAction.Factory());
    	registerAction(new InvertSelectionAction.Descriptor(),              new InvertSelectionAction.Factory());
    	registerAction(new LocalCopyAction.Descriptor(),                    new LocalCopyAction.Factory());
    	registerAction(new MarkAllAction.Descriptor(),           		    new MarkAllAction.Factory());
    	registerAction(new MarkExtensionAction.Descriptor(),            	new MarkExtensionAction.Factory());
    	registerAction(new MarkGroupAction.Descriptor(),            		new MarkGroupAction.Factory());
        registerAction(new MarkNextBlockAction.Descriptor(),                new MarkNextBlockAction.Factory());
    	registerAction(new MarkNextPageAction.Descriptor(),             	new MarkNextPageAction.Factory());
        registerAction(new MarkNextRowAction.Descriptor(),             	    new MarkNextRowAction.Factory());
        registerAction(new MarkPreviousBlockAction.Descriptor(),            new MarkPreviousBlockAction.Factory());
    	registerAction(new MarkPreviousPageAction.Descriptor(),             new MarkPreviousPageAction.Factory());
        registerAction(new MarkPreviousRowAction.Descriptor(),              new MarkPreviousRowAction.Factory());
    	registerAction(new MarkSelectedFileAction.Descriptor(),             new MarkSelectedFileAction.Factory());
    	registerAction(new MarkToFirstRowAction.Descriptor(),               new MarkToFirstRowAction.Factory());
    	registerAction(new MarkToLastRowAction.Descriptor(),                new MarkToLastRowAction.Factory());
    	registerAction(new MaximizeWindowAction.Descriptor(),               new MaximizeWindowAction.Factory());
    	registerAction(new CombineFilesAction.Descriptor(),            		new CombineFilesAction.Factory());
    	registerAction(new MinimizeWindowAction.Descriptor(),               new MinimizeWindowAction.Factory());
    	registerAction(new MkdirAction.Descriptor(),           			    new MkdirAction.Factory());
    	registerAction(new MkfileAction.Descriptor(),		                new MkfileAction.Factory());
    	registerAction(new MoveAction.Descriptor(),		                    new MoveAction.Factory());
    	registerAction(new MoveTabToOtherPanelAction.Descriptor(),			new MoveTabToOtherPanelAction.Factory());
    	registerAction(new NewWindowAction.Descriptor(),     		        new NewWindowAction.Factory());
    	registerAction(new NextTabAction.Descriptor(),						new NextTabAction.Factory());
    	registerAction(new OpenAction.Descriptor(),          				new OpenAction.Factory());
    	registerAction(new OpenInBothPanelsAction.Descriptor(),             new OpenInBothPanelsAction.Factory());
    	registerAction(new OpenInNewTabAction.Descriptor(),					new OpenInNewTabAction.Factory());
    	registerAction(new OpenInOtherPanelAction.Descriptor(),             new OpenInOtherPanelAction.Factory());
//    	registerAction(new OpenLocationAction.Descriptor(),          	    new OpenLocationAction.Factory());
    	registerAction(new OpenNativelyAction.Descriptor(),       		    new OpenNativelyAction.Factory());
    	registerAction(new OpenTrashAction.Descriptor(),           	        new OpenTrashAction.Factory());
    	registerAction(new OpenURLInBrowserAction.Descriptor(),             new OpenURLInBrowserAction.Factory());
    	registerAction(new PackAction.Descriptor(),       			        new PackAction.Factory());
    	registerAction(new PasteClipboardFilesAction.Descriptor(),          new PasteClipboardFilesAction.Factory());
    	registerAction(new PermanentDeleteAction.Descriptor(),              new PermanentDeleteAction.Factory());
    	registerAction(new PopupLeftDriveButtonAction.Descriptor(),         new PopupLeftDriveButtonAction.Factory());
    	registerAction(new PopupRightDriveButtonAction.Descriptor(),        new PopupRightDriveButtonAction.Factory());
    	registerAction(new PreviousTabAction.Descriptor(),					new PreviousTabAction.Factory());
    	registerAction(new QuitAction.Descriptor(),              			new QuitAction.Factory());
    	registerAction(new RecallNextWindowAction.Descriptor(),             new RecallNextWindowAction.Factory());
    	registerAction(new RecallPreviousWindowAction.Descriptor(),         new RecallPreviousWindowAction.Factory());
    	registerAction(new RecallWindow10Action.Descriptor(),               new RecallWindow10Action.Factory());
    	registerAction(new RecallWindow1Action.Descriptor(),                new RecallWindow1Action.Factory());
    	registerAction(new RecallWindow2Action.Descriptor(),                new RecallWindow2Action.Factory());
    	registerAction(new RecallWindow3Action.Descriptor(),                new RecallWindow3Action.Factory());
    	registerAction(new RecallWindow4Action.Descriptor(),                new RecallWindow4Action.Factory());
    	registerAction(new RecallWindow5Action.Descriptor(),                new RecallWindow5Action.Factory());
    	registerAction(new RecallWindow6Action.Descriptor(),                new RecallWindow6Action.Factory());
    	registerAction(new RecallWindow7Action.Descriptor(),                new RecallWindow7Action.Factory());
    	registerAction(new RecallWindow8Action.Descriptor(),                new RecallWindow8Action.Factory());
    	registerAction(new RecallWindow9Action.Descriptor(),                new RecallWindow9Action.Factory());
    	registerAction(new RecallWindowAction.Descriptor(),                 new RecallWindowAction.Factory());
    	registerAction(new RefreshAction.Descriptor(),        		        new RefreshAction.Factory());
    	registerAction(new RenameAction.Descriptor(),              		    new RenameAction.Factory());
    	registerAction(new ReportBugAction.Descriptor(),       	            new ReportBugAction.Factory());
    	registerAction(new RevealInDesktopAction.Descriptor(),              new RevealInDesktopAction.Factory());
    	registerAction(new ReverseSortOrderAction.Descriptor(),             new ReverseSortOrderAction.Factory());
    	registerAction(new RunCommandAction.Descriptor(),     		        new RunCommandAction.Factory());
        registerAction(new SelectPreviousBlockAction.Descriptor(),          new SelectPreviousBlockAction.Factory());
        registerAction(new SelectPreviousPageAction.Descriptor(),           new SelectPreviousPageAction.Factory());
        registerAction(new SelectPreviousRowAction.Descriptor(),            new SelectPreviousRowAction.Factory());
        registerAction(new SelectNextBlockAction.Descriptor(),              new SelectNextBlockAction.Factory());
        registerAction(new SelectNextPageAction.Descriptor(),               new SelectNextPageAction.Factory());
        registerAction(new SelectNextRowAction.Descriptor(),                new SelectNextRowAction.Factory());
    	registerAction(new SelectFirstRowAction.Descriptor(),               new SelectFirstRowAction.Factory());
    	registerAction(new SelectLastRowAction.Descriptor(),                new SelectLastRowAction.Factory());
    	registerAction(new SetSameFolderAction.Descriptor(),                new SetSameFolderAction.Factory());
    	registerAction(new SetTabTitleAction.Descriptor(),                  new SetTabTitleAction.Factory());
    	registerAction(new ShowAboutAction.Descriptor(),          		    new ShowAboutAction.Factory());
    	registerAction(new ShowBookmarksQLAction.Descriptor(),              new ShowBookmarksQLAction.Factory());
    	registerAction(new CustomizeCommandBarAction.Descriptor(),          new CustomizeCommandBarAction.Factory());
        registerAction(new ShowDebugConsoleAction.Descriptor(),             new ShowDebugConsoleAction.Factory());
        registerAction(new ShowFilePropertiesAction.Descriptor(),           new ShowFilePropertiesAction.Factory());
    	registerAction(new ShowKeyboardShortcutsAction.Descriptor(),        new ShowKeyboardShortcutsAction.Factory());
    	registerAction(new ShowParentFoldersQLAction.Descriptor(),          new ShowParentFoldersQLAction.Factory());
    	registerAction(new ShowPreferencesAction.Descriptor(),              new ShowPreferencesAction.Factory());
    	registerAction(new ShowRecentExecutedFilesQLAction.Descriptor(),    new ShowRecentExecutedFilesQLAction.Factory());
    	registerAction(new ShowRecentLocationsQLAction.Descriptor(),        new ShowRecentLocationsQLAction.Factory());
    	registerAction(new ShowRootFoldersQLAction.Descriptor(), 			new ShowRootFoldersQLAction.Factory());
    	registerAction(new ShowServerConnectionsAction.Descriptor(),        new ShowServerConnectionsAction.Factory());
    	registerAction(new ShowTabsQLAction.Descriptor(),					new ShowTabsQLAction.Factory());
    	registerAction(new SortByDateAction.Descriptor(),             		new SortByDateAction.Factory());
    	registerAction(new SortByExtensionAction.Descriptor(),              new SortByExtensionAction.Factory());
    	registerAction(new SortByGroupAction.Descriptor(),            		new SortByGroupAction.Factory());
    	registerAction(new SortByNameAction.Descriptor(),           		new SortByNameAction.Factory());
    	registerAction(new SortByOwnerAction.Descriptor(),               	new SortByOwnerAction.Factory());
    	registerAction(new SortByPermissionsAction.Descriptor(),            new SortByPermissionsAction.Factory());
    	registerAction(new SortBySizeAction.Descriptor(),                   new SortBySizeAction.Factory());
    	registerAction(new SplitEquallyAction.Descriptor(),             	new SplitEquallyAction.Factory());
    	registerAction(new SplitFileAction.Descriptor(),            		new SplitFileAction.Factory());
    	registerAction(new SplitHorizontallyAction.Descriptor(),            new SplitHorizontallyAction.Factory());
    	registerAction(new SplitVerticallyAction.Descriptor(),              new SplitVerticallyAction.Factory());
    	registerAction(new StopAction.Descriptor(),              			new StopAction.Factory());
    	registerAction(new SwapFoldersAction.Descriptor(),       	        new SwapFoldersAction.Factory());
    	registerAction(new SwitchActiveTableAction.Descriptor(),            new SwitchActiveTableAction.Factory());
    	registerAction(new ToggleAutoSizeAction.Descriptor(),               new ToggleAutoSizeAction.Factory());
//    	registerAction(new ToggleColumnAction.Descriptor(),           	    new ToggleColumnAction.Factory());
    	registerAction(new ToggleCommandBarAction.Descriptor(),             new ToggleCommandBarAction.Factory());
    	registerAction(new ToggleDateColumnAction.Descriptor(),             new ToggleDateColumnAction.Factory());
    	registerAction(new ToggleExtensionColumnAction.Descriptor(),        new ToggleExtensionColumnAction.Factory());
    	registerAction(new ToggleGroupColumnAction.Descriptor(),            new ToggleGroupColumnAction.Factory());
    	registerAction(new ToggleHiddenFilesAction.Descriptor(),            new ToggleHiddenFilesAction.Factory());
    	registerAction(new ToggleLockTabAction.Descriptor(),                new ToggleLockTabAction.Factory());
    	registerAction(new ToggleOwnerColumnAction.Descriptor(),            new ToggleOwnerColumnAction.Factory());
    	registerAction(new TogglePermissionsColumnAction.Descriptor(),      new TogglePermissionsColumnAction.Factory());
    	registerAction(new ToggleShowFoldersFirstAction.Descriptor(),       new ToggleShowFoldersFirstAction.Factory());
    	registerAction(new ToggleSizeColumnAction.Descriptor(),             new ToggleSizeColumnAction.Factory());
    	registerAction(new ToggleStatusBarAction.Descriptor(),              new ToggleStatusBarAction.Factory());
    	registerAction(new ToggleToolBarAction.Descriptor(),                new ToggleToolBarAction.Factory());
    	registerAction(new ToggleTreeAction.Descriptor(),             	    new ToggleTreeAction.Factory());
    	registerAction(new UnmarkAllAction.Descriptor(),            		new UnmarkAllAction.Factory());
    	registerAction(new UnmarkGroupAction.Descriptor(),            		new UnmarkGroupAction.Factory());
    	registerAction(new UnpackAction.Descriptor(),             			new UnpackAction.Factory());
    	registerAction(new ViewAction.Descriptor(),              			new ViewAction.Factory());

    	// register "open with" commands as actions, to allow for keyboard shortcuts for them
    	for (Command command : CommandManager.commands()) {
    		if (command.getType() == CommandType.NORMAL_COMMAND) {
    			ActionManager.registerAction(new CommandAction.Descriptor(command),
    					                     new CommandAction.Factory(command));
    		}
    	}
    }

    /**
     * Registration method for MuActions.
     * 
     * @param actionDescriptor - ActionDescriptor instance of the action.
     * @param actionFactory - ActionFactory instance of the action.
     */
    public static void registerAction(ActionDescriptor actionDescriptor, ActionFactory actionFactory) {
    	actionFactories.put(actionDescriptor.getId(), actionFactory);
    	ActionProperties.addActionDescriptor(actionDescriptor);
    }
    
    /**
     * Return all ids of the registered actions.
     * 
     * @return Enumeration of all registered actions' ids.
     */
    public static Iterator<String> getActionIds() {
    	return actionFactories.keySet().iterator();
    }
    
    /**
     * Return the id of MuAction in a given path.
     * 
     * @param actionClassPath - path to MuAction class.
     * @return String representing the id of the MuAction in the specified path. null is returned if the given path is invalid.
     */
    public static String extrapolateId(String actionClassPath) {
    	if (actionClassPath == null)
    		return null;
    	
    	Matcher matcher = pattern.matcher(actionClassPath);
    	return matcher.matches() ? 
    			matcher.group(1)
    			: actionClassPath;
    }
    
    /**
     * Checks whether an MuAction is registered.
     * 
     * @param actionId - id of MuAction.
     * @return true if an MuAction which is represented by the given id is registered, otherwise return false.
     */
    public static boolean isActionExist(String actionId) {    	
    	return actionId != null && actionFactories.containsKey(actionId);
    }

    /**
     * Convenience method that returns an instance of the action corresponding to the given <code>Command</code>,
     * and associated with the specified <code>MainFrame</code>. This method gets the ID of the relevant action,
     * passes it to {@link #getActionInstance(String, MainFrame)} and returns the {@link MuAction} instance.
     *
     * @param command the command that is invoked by the returned action
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given action ID and MainFrame, <code>null</code> if the
     * @see {@link #getActionInstance(String, MainFrame)}
     * action could not be found or could not be instantiated.
     */
    public static MuAction getActionInstance(Command command, MainFrame mainFrame) {
        return getActionInstance(new CommandAction.Descriptor(command).getId(), mainFrame);
    }

    /**
     * Convenience method that returns an instance of the action denoted by the given ID, and associated with the
     * specified <code>MainFrame</code>. This method creates an ActionParameters with no initial property, passes it to
     * {@link #getActionInstance(ActionParameters, MainFrame)} and returns the {@link MuAction} instance.
     *
     * @param actionId ID of the action to instantiate
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given action ID and MainFrame, <code>null</code> if the
     * @see {@link #getActionInstance(ActionParameters, MainFrame)}
     * action could not be found or could not be instantiated.
     */
    public static MuAction getActionInstance(String actionId, MainFrame mainFrame) {
        return getActionInstance(new ActionParameters(actionId), mainFrame);
    }

    /**
     * Returns an instance of the MuAction class denoted by the given ActionParameters and for the
     * specified MainFrame. If an existing instance corresponding to the same ActionParameters and MainFrame is found,
     * it is simply returned.
     * If no matching instance could be found, a new instance is created, added to the internal action instances map
     * (for further use) and returned.
     * If the action denoted by the specified ActionParameters cannot be found or cannot be instantiated,
     * <code>null</code> is returned.
     *
     * @param actionParameters a descriptor of the action to instantiate with initial properties
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given ActionParameters and MainFrame, <code>null</code> if the
     * MuAction action denoted by the ActionParameters could not be found or could not be instantiated.
     */
    public static MuAction getActionInstance(ActionParameters actionParameters, MainFrame mainFrame) {
        Map<ActionParameters, ActionAndIdPair> mainFrameActions = mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
            mainFrameActions = new Hashtable<ActionParameters, ActionAndIdPair>();
            mainFrameActionsMap.put(mainFrame, mainFrameActions);
        }

        // Looks for an existing MuAction instance used by the specified MainFrame
        if (mainFrameActions.containsKey(actionParameters)) {
        	return mainFrameActions.get(actionParameters).getAction();
        }
        else {
            String actionId = actionParameters.getActionId();

            // Looks for the action's factory
            ActionFactory actionFactory = actionFactories.get(actionId);
            if(actionFactory == null) {
            	LOGGER.debug("couldn't initiate action: " + actionId + ", its factory wasn't found");
            	return null;
            }

            Map<String,Object> properties = actionParameters.getInitProperties();
            // If no properties hashtable is specified in the action descriptor
            if(properties==null) {
            	properties = Collections.emptyMap();
            }
            // else clone the hashtable to ensure that it doesn't get modified by action instances.
            // Since cloning is an expensive operation, this is done only if the hashtable is not empty.
            else if(!properties.isEmpty()) {
                Map<String,Object> buffer = new Hashtable<String,Object>(properties);
                properties = buffer;
            }

            // Instantiate the MuAction class
            MuAction action = actionFactory.createAction(mainFrame, properties);
            mainFrameActions.put(actionParameters, new ActionAndIdPair(action, actionId));

            // If the action's label has not been set yet, use the action descriptor's
            if(action.getLabel()==null) {
                // Retrieve the standard label entry from the dictionary and use it as this action's label
                String label = ActionProperties.getActionLabel(actionId);
                
                // Append '...' to the label if this action invokes a dialog when performed
                if(action.getClass().isAnnotationPresent(InvokesDialog.class))
                    label += "...";

                action.setLabel(label);

                // Looks for a standard label entry in the dictionary and if it is defined, use it as this action's tooltip
                String tooltip = ActionProperties.getActionTooltip(actionId);
                if(tooltip!=null)
                    action.setToolTipText(tooltip);
            }
            
            // If the action's accelerators have not been set yet, use the ones from ActionKeymap
            if(action.getAccelerator()==null) {
                // Retrieve the standard accelerator (if any) and use it as this action's accelerator
                KeyStroke accelerator = ActionKeymap.getAccelerator(actionId);
                if(accelerator!=null)
                    action.setAccelerator(accelerator);

                // Retrieve the standard alternate accelerator (if any) and use it as this action's alternate accelerator
                accelerator = ActionKeymap.getAlternateAccelerator(actionId);
                if(accelerator!=null)
                    action.setAlternateAccelerator(accelerator);
            }
            
            // If the action's icon has not been set yet, use the action descriptor's
            if(action.getIcon()==null) {
                // Retrieve the standard icon image (if any) and use it as the action's icon
                ImageIcon icon = ActionProperties.getActionIcon(actionId);
                if(icon!=null)
                    action.setIcon(icon);
            }
            
            return action;
        }
    }


    /**
     * Returns a Vector of all MuAction instances matching the specified action id.
     *
     * @param muActionId the MuAction id to compare instances against
     * @return  a Vector of all MuAction instances matching the specified action id
     */
    public static List<MuAction> getActionInstances(String muActionId) {
        List<MuAction> actionInstances = new Vector<MuAction>();

        // Iterate on all MainFrame instances
        for (Map<ActionParameters, ActionAndIdPair> actionParametersActionAndIdPairHashtable : mainFrameActionsMap.values()) {
            // Iterate on all the MainFrame's actions and their ids pairs
            for (ActionAndIdPair actionAndIdPair : actionParametersActionAndIdPairHashtable.values()) {
                if (actionAndIdPair.getId().equals(muActionId)) {
                    // Found an action matching the specified class
                    actionInstances.add(actionAndIdPair.getAction());
                    // Jump to the next MainFrame
                    break;
                }
            }
        }

        return actionInstances;
    }

    /**
     * Convenience method that retrieves an instance of the action denoted by the given ID and associated
     * with the given {@link MainFrame} and calls {@link MuAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionId ID of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise 
     */
    public static boolean performAction(String actionId, MainFrame mainFrame) {
        return performAction(new ActionParameters(actionId), mainFrame);
    }

    /**
     * Convenience method that retrieves an instance of the MuAction denoted by the given {@link ActionParameters}
     * and associated with the given {@link com.mucommander.ui.main.MainFrame} and calls {@link MuAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionParameters the ActionParameters of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise
     */
    public static boolean performAction(ActionParameters actionParameters, MainFrame mainFrame) {
        MuAction action = getActionInstance(actionParameters, mainFrame);

        if(action==null)
            return false;

        action.performAction();

        return true;
    }
    
    /**
     *  Helper class to represent a pair of instance and id of MuAction.
     */
    private static class ActionAndIdPair {
    	private MuAction action;
    	private String id;
    	
    	public ActionAndIdPair(MuAction action, String id) {
    		this.action = action;
    		this.id = id;
    	}
    	
    	public MuAction getAction() { return action; }
    	
    	public String getId() { return id; }
    }
}
