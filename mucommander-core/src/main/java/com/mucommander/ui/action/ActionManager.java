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

import java.util.ArrayList;
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
import com.mucommander.desktop.ActionType;
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
    	registerAction(new AddBookmarkAction.Descriptor(),                  AddBookmarkAction::new);
    	registerAction(new NewTabAction.Descriptor(),						NewTabAction::new);
    	registerAction(new BatchRenameAction.Descriptor(),                  BatchRenameAction::new);
    	registerAction(new BringAllToFrontAction.Descriptor(),              BringAllToFrontAction::new);
    	registerAction(new CalculateChecksumAction.Descriptor(),            CalculateChecksumAction::new);
    	registerAction(new ChangeDateAction.Descriptor(),                   ChangeDateAction::new);
    	registerAction(new ChangeLocationAction.Descriptor(),               ChangeLocationAction::new);
    	registerAction(new ChangePermissionsAction.Descriptor(),            ChangePermissionsAction::new);
    	registerAction(new CheckForUpdatesAction.Descriptor(),              CheckForUpdatesAction::new);
    	registerAction(new CloneTabToOtherPanelAction.Descriptor(), 		CloneTabToOtherPanelAction::new);
    	registerAction(new CloseDuplicateTabsAction.Descriptor(),			CloseDuplicateTabsAction::new);
    	registerAction(new CloseOtherTabsAction.Descriptor(),				CloseOtherTabsAction::new);
    	registerAction(new CloseWindowAction.Descriptor(),                  CloseWindowAction::new);
    	registerAction(new CloseTabAction.Descriptor(),						CloseTabAction::new);
//    	registerAction(new CommandAction.Descriptor(),           		     new CommandAction::new);
    	registerAction(new CompareFoldersAction.Descriptor(),               CompareFoldersAction::new);
    	registerAction(new ConnectToServerAction.Descriptor(),              ConnectToServerAction::new);
    	registerAction(new CopyAction.Descriptor(),                         CopyAction::new);
    	registerAction(new CopyFileBaseNamesAction.Descriptor(),            CopyFileBaseNamesAction::new);
    	registerAction(new CopyFileNamesAction.Descriptor(),                CopyFileNamesAction::new);
    	registerAction(new CopyFilePathsAction.Descriptor(),                CopyFilePathsAction::new);
    	registerAction(new CopyFilesToClipboardAction.Descriptor(),         CopyFilesToClipboardAction::new);
        registerAction(new FindAction.Descriptor(),                         FindAction::new);
    	registerAction(new FocusPreviousAction.Descriptor(),                FocusPreviousAction::new);
    	registerAction(new FocusNextAction.Descriptor(),                    FocusNextAction::new);
    	registerAction(new DeleteAction.Descriptor(),         			    DeleteAction::new);
    	registerAction(new DonateAction.Descriptor(),    			        DonateAction::new);
    	registerAction(new DuplicateTabAction.Descriptor(),					DuplicateTabAction::new);
    	registerAction(new EditAction.Descriptor(),     			        EditAction::new);
    	registerAction(new EditBookmarksAction.Descriptor(),                EditBookmarksAction::new);
    	registerAction(new EditCredentialsAction.Descriptor(),              EditCredentialsAction::new);
    	registerAction(new EmailAction.Descriptor(),          			    EmailAction::new);
    	registerAction(new EmptyTrashAction.Descriptor(),           	    EmptyTrashAction::new);
    	registerAction(new ExploreBookmarksAction.Descriptor(),             ExploreBookmarksAction::new);
//    	registerAction(new GarbageCollectAction.Descriptor(),               GarbageCollectAction::new);
    	registerAction(new GoBackAction.Descriptor(),                       GoBackAction::new);
    	registerAction(new GoForwardAction.Descriptor(),                    GoForwardAction::new);
    	registerAction(new GoToDocumentationAction.Descriptor(),            GoToDocumentationAction::new);
    	registerAction(new GoToForumsAction.Descriptor(),                   GoToForumsAction::new);
    	registerAction(new GoToHomeAction.Descriptor(),                     GoToHomeAction::new);
    	registerAction(new GoToParentAction.Descriptor(),                   GoToParentAction::new);
    	registerAction(new GoToParentInBothPanelsAction.Descriptor(),       GoToParentInBothPanelsAction::new);
    	registerAction(new GoToParentInOtherPanelAction.Descriptor(),       GoToParentInOtherPanelAction::new);
    	registerAction(new GoToRootAction.Descriptor(),                     GoToRootAction::new);
    	registerAction(new GoToWebsiteAction.Descriptor(),                  GoToWebsiteAction::new);
    	registerAction(new InternalEditAction.Descriptor(),                 InternalEditAction::new);
    	registerAction(new InternalViewAction.Descriptor(),                 InternalViewAction::new);
    	registerAction(new InvertSelectionAction.Descriptor(),              InvertSelectionAction::new);
    	registerAction(new LocalCopyAction.Descriptor(),                    LocalCopyAction::new);
    	registerAction(new MarkAllAction.Descriptor(),           		    MarkAllAction::new);
    	registerAction(new MarkExtensionAction.Descriptor(),            	MarkExtensionAction::new);
    	registerAction(new MarkGroupAction.Descriptor(),            		MarkGroupAction::new);
        registerAction(new MarkNextBlockAction.Descriptor(),                MarkNextBlockAction::new);
    	registerAction(new MarkNextPageAction.Descriptor(),             	MarkNextPageAction::new);
        registerAction(new MarkNextRowAction.Descriptor(),             	    MarkNextRowAction::new);
        registerAction(new MarkPreviousBlockAction.Descriptor(),            MarkPreviousBlockAction::new);
    	registerAction(new MarkPreviousPageAction.Descriptor(),             MarkPreviousPageAction::new);
        registerAction(new MarkPreviousRowAction.Descriptor(),              MarkPreviousRowAction::new);
    	registerAction(new MarkSelectedFileAction.Descriptor(),             MarkSelectedFileAction::new);
    	registerAction(new MarkToFirstRowAction.Descriptor(),               MarkToFirstRowAction::new);
    	registerAction(new MarkToLastRowAction.Descriptor(),                MarkToLastRowAction::new);
    	registerAction(new MaximizeWindowAction.Descriptor(),               MaximizeWindowAction::new);
    	registerAction(new CombineFilesAction.Descriptor(),            		CombineFilesAction::new);
    	registerAction(new MinimizeWindowAction.Descriptor(),               MinimizeWindowAction::new);
    	registerAction(new MkdirAction.Descriptor(),           			    MkdirAction::new);
    	registerAction(new MkfileAction.Descriptor(),		                MkfileAction::new);
    	registerAction(new MoveAction.Descriptor(),		                    MoveAction::new);
    	registerAction(new MoveTabToOtherPanelAction.Descriptor(),			MoveTabToOtherPanelAction::new);
    	registerAction(new NewWindowAction.Descriptor(),     		        NewWindowAction::new);
    	registerAction(new NextTabAction.Descriptor(),						NextTabAction::new);
    	registerAction(new OpenAction.Descriptor(),          				OpenAction::new);
        registerAction(new OpenAsAction.Descriptor(),                       OpenAsAction::new);
		registerAction(new OpenCommandPromptAction.Descriptor(),            OpenCommandPromptAction::new);
    	registerAction(new OpenInBothPanelsAction.Descriptor(),             OpenInBothPanelsAction::new);
    	registerAction(new OpenInNewTabAction.Descriptor(),					OpenInNewTabAction::new);
    	registerAction(new OpenInOtherPanelAction.Descriptor(),             OpenInOtherPanelAction::new);
//    	registerAction(new OpenLocationAction.Descriptor(),          	    OpenLocationAction::new);
    	registerAction(new OpenNativelyAction.Descriptor(),       		    OpenNativelyAction::new);
    	registerAction(new OpenTrashAction.Descriptor(),           	        OpenTrashAction::new);
    	registerAction(new OpenURLInBrowserAction.Descriptor(),             OpenURLInBrowserAction::new);
    	registerAction(new PackAction.Descriptor(),       			        PackAction::new);
    	registerAction(new PasteClipboardFilesAction.Descriptor(),          PasteClipboardFilesAction::new);
    	registerAction(new PermanentDeleteAction.Descriptor(),              PermanentDeleteAction::new);
    	registerAction(new PopupLeftDriveButtonAction.Descriptor(),         PopupLeftDriveButtonAction::new);
    	registerAction(new PopupRightDriveButtonAction.Descriptor(),        PopupRightDriveButtonAction::new);
    	registerAction(new PreviousTabAction.Descriptor(),					PreviousTabAction::new);
        registerAction(new QuickFindAction.Descriptor(),                    QuickFindAction::new);
    	registerAction(new QuitAction.Descriptor(),              			QuitAction::new);
    	registerAction(new RecallNextWindowAction.Descriptor(),             RecallNextWindowAction::new);
    	registerAction(new RecallPreviousWindowAction.Descriptor(),         RecallPreviousWindowAction::new);
    	registerAction(new RecallWindow10Action.Descriptor(),               RecallWindow10Action::new);
    	registerAction(new RecallWindow1Action.Descriptor(),                RecallWindow1Action::new);
    	registerAction(new RecallWindow2Action.Descriptor(),                RecallWindow2Action::new);
    	registerAction(new RecallWindow3Action.Descriptor(),                RecallWindow3Action::new);
    	registerAction(new RecallWindow4Action.Descriptor(),                RecallWindow4Action::new);
    	registerAction(new RecallWindow5Action.Descriptor(),                RecallWindow5Action::new);
    	registerAction(new RecallWindow6Action.Descriptor(),                RecallWindow6Action::new);
    	registerAction(new RecallWindow7Action.Descriptor(),                RecallWindow7Action::new);
    	registerAction(new RecallWindow8Action.Descriptor(),                RecallWindow8Action::new);
    	registerAction(new RecallWindow9Action.Descriptor(),                RecallWindow9Action::new);
    	registerAction(new RecallWindowAction.Descriptor(),                 RecallWindowAction::new);
    	registerAction(new RefreshAction.Descriptor(),        		        RefreshAction::new);
    	registerAction(new RenameAction.Descriptor(),              		    RenameAction::new);
    	registerAction(new ReportBugAction.Descriptor(),       	            ReportBugAction::new);
    	registerAction(new RevealInDesktopAction.Descriptor(),              RevealInDesktopAction::new);
    	registerAction(new ReverseSortOrderAction.Descriptor(),             ReverseSortOrderAction::new);
    	registerAction(new RunCommandAction.Descriptor(),     		        RunCommandAction::new);
    	registerAction(new ShowInEnclosingFolderAction.Descriptor(),        ShowInEnclosingFolderAction::new);
        registerAction(new SelectPreviousBlockAction.Descriptor(),          SelectPreviousBlockAction::new);
        registerAction(new SelectPreviousPageAction.Descriptor(),           SelectPreviousPageAction::new);
        registerAction(new SelectPreviousRowAction.Descriptor(),            SelectPreviousRowAction::new);
        registerAction(new SelectNextBlockAction.Descriptor(),              SelectNextBlockAction::new);
        registerAction(new SelectNextPageAction.Descriptor(),               SelectNextPageAction::new);
        registerAction(new SelectNextRowAction.Descriptor(),                SelectNextRowAction::new);
    	registerAction(new SelectFirstRowAction.Descriptor(),               SelectFirstRowAction::new);
    	registerAction(new SelectLastRowAction.Descriptor(),                SelectLastRowAction::new);
    	registerAction(new SetSameFolderAction.Descriptor(),                SetSameFolderAction::new);
    	registerAction(new SetTabTitleAction.Descriptor(),                  SetTabTitleAction::new);
    	registerAction(new ShowAboutAction.Descriptor(),          		    ShowAboutAction::new);
    	registerAction(new ShowBookmarksQLAction.Descriptor(),              ShowBookmarksQLAction::new);
    	registerAction(new CustomizeCommandBarAction.Descriptor(),          CustomizeCommandBarAction::new);
        registerAction(new ShowDebugConsoleAction.Descriptor(),             ShowDebugConsoleAction::new);
        registerAction(new ShowFilePropertiesAction.Descriptor(),           ShowFilePropertiesAction::new);
        registerAction(new ShowFilePopupMenuAction.Descriptor(),            ShowFilePopupMenuAction::new);
    	registerAction(new ShowKeyboardShortcutsAction.Descriptor(),        ShowKeyboardShortcutsAction::new);
    	registerAction(new ShowParentFoldersQLAction.Descriptor(),          ShowParentFoldersQLAction::new);
    	registerAction(new ShowPreferencesAction.Descriptor(),              ShowPreferencesAction::new);
    	registerAction(new ShowRecentExecutedFilesQLAction.Descriptor(),    ShowRecentExecutedFilesQLAction::new);
    	registerAction(new ShowRecentLocationsQLAction.Descriptor(),        ShowRecentLocationsQLAction::new);
    	registerAction(new ShowRootFoldersQLAction.Descriptor(), 			ShowRootFoldersQLAction::new);
    	registerAction(new ShowServerConnectionsAction.Descriptor(),        ShowServerConnectionsAction::new);
    	registerAction(new ShowTabsQLAction.Descriptor(),					ShowTabsQLAction::new);
    	registerAction(new ShowTerminalAction.Descriptor(),                 ShowTerminalAction::new);
    	registerAction(new SortByDateAction.Descriptor(),             		SortByDateAction::new);
    	registerAction(new SortByExtensionAction.Descriptor(),              SortByExtensionAction::new);
    	registerAction(new SortByGroupAction.Descriptor(),            		SortByGroupAction::new);
    	registerAction(new SortByNameAction.Descriptor(),           		SortByNameAction::new);
    	registerAction(new SortByOwnerAction.Descriptor(),               	SortByOwnerAction::new);
    	registerAction(new SortByPermissionsAction.Descriptor(),            SortByPermissionsAction::new);
    	registerAction(new SortBySizeAction.Descriptor(),                   SortBySizeAction::new);
    	registerAction(new SplitEquallyAction.Descriptor(),             	SplitEquallyAction::new);
    	registerAction(new SplitFileAction.Descriptor(),            		SplitFileAction::new);
    	registerAction(new SplitHorizontallyAction.Descriptor(),            SplitHorizontallyAction::new);
    	registerAction(new SplitVerticallyAction.Descriptor(),              SplitVerticallyAction::new);
    	registerAction(new ToggleUseSinglePanelAction.Descriptor(),         ToggleUseSinglePanelAction::new);
    	registerAction(new StopAction.Descriptor(),                         StopAction::new);
    	registerAction(new SwapFoldersAction.Descriptor(),       	        SwapFoldersAction::new);
    	registerAction(new SwitchActiveTableAction.Descriptor(),            SwitchActiveTableAction::new);
    	registerAction(new ToggleAutoSizeAction.Descriptor(),               ToggleAutoSizeAction::new);
//    	registerAction(new ToggleColumnAction.Descriptor(),           	    ToggleColumnAction::new);
    	registerAction(new ToggleCommandBarAction.Descriptor(),             ToggleCommandBarAction::new);
    	registerAction(new ToggleDateColumnAction.Descriptor(),             ToggleDateColumnAction::new);
    	registerAction(new ToggleExtensionColumnAction.Descriptor(),        ToggleExtensionColumnAction::new);
    	registerAction(new ToggleGroupColumnAction.Descriptor(),            ToggleGroupColumnAction::new);
    	registerAction(new ToggleHiddenFilesAction.Descriptor(),            ToggleHiddenFilesAction::new);
    	registerAction(new ToggleLockTabAction.Descriptor(),                ToggleLockTabAction::new);
    	registerAction(new ToggleOwnerColumnAction.Descriptor(),            ToggleOwnerColumnAction::new);
    	registerAction(new TogglePermissionsColumnAction.Descriptor(),      TogglePermissionsColumnAction::new);
    	registerAction(new ToggleShowFoldersFirstAction.Descriptor(),       ToggleShowFoldersFirstAction::new);
    	registerAction(new ToggleSizeColumnAction.Descriptor(),             ToggleSizeColumnAction::new);
    	registerAction(new ToggleStatusBarAction.Descriptor(),              ToggleStatusBarAction::new);
    	registerAction(new ToggleToolBarAction.Descriptor(),                ToggleToolBarAction::new);
    	registerAction(new ToggleTreeAction.Descriptor(),             	    ToggleTreeAction::new);
    	registerAction(new UnmarkAllAction.Descriptor(),            		UnmarkAllAction::new);
    	registerAction(new UnmarkGroupAction.Descriptor(),            		UnmarkGroupAction::new);
    	registerAction(new UnpackAction.Descriptor(),             			UnpackAction::new);
    	registerAction(new ViewAction.Descriptor(),              			ViewAction::new);

    	// register "open with" commands as actions, to allow for keyboard shortcuts for them
    	for (Command command : CommandManager.commands()) {
    		if (command.getType() == CommandType.NORMAL_COMMAND) {
    			ActionManager.registerAction(
    			        new CommandAction.Descriptor(command),
    			        (mainFrame, properties) -> new CommandAction(mainFrame, properties, command));
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
     * @return List of all registered actions' ids.
     */
    public static List<String> getActionIds() {
        return new ArrayList<>(actionFactories.keySet());
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
     * @see {@link #getActionInstance(String, MainFrame)}
     */
    public static MuAction getActionInstance(ActionType actionId, MainFrame mainFrame) {
        return getActionInstance(actionId.toString(), mainFrame);
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
     * @see #performAction(String, MainFrame)
     */
    public static boolean performAction(ActionType actionType, MainFrame mainFrame) {
        return performAction(actionType.toString(), mainFrame);
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
