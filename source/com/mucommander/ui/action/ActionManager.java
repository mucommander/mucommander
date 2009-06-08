/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;

import com.mucommander.Debug;
import com.mucommander.ui.action.impl.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

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
 * @see ActionDescriptor
 * @see ActionKeymap
 * @author Maxence Bernard, Arik Hadas
 */
public class ActionManager {

    /** MuAction class -> factory map */
    private static Hashtable actionFactories = new Hashtable();
    
    /** MuAction class name -> class map */
    private static Hashtable actionClasses = new Hashtable();

    /** MainFrame -> MuAction map */
    private static WeakHashMap mainFrameActionsMap = new WeakHashMap();

    static {
    	registerAction(AddBookmarkAction.class,                  new AddBookmarkAction.Factory());
    	registerAction(BatchRenameAction.class,                  new BatchRenameAction.Factory());
    	registerAction(BringAllToFrontAction.class,              new BringAllToFrontAction.Factory());
    	registerAction(CalculateChecksumAction.class,            new CalculateChecksumAction.Factory());
    	registerAction(ChangeDateAction.class,                   new ChangeDateAction.Factory());
    	registerAction(ChangeLocationAction.class,               new ChangeLocationAction.Factory());
    	registerAction(ChangePermissionsAction.class,            new ChangePermissionsAction.Factory());
    	registerAction(CheckForUpdatesAction.class,              new CheckForUpdatesAction.Factory());
    	registerAction(CloseWindowAction.class,                  new CloseWindowAction.Factory());
//    	registerAction(CommandAction.class,           		     new CommandAction.Factory());
    	registerAction(CompareFoldersAction.class,               new CompareFoldersAction.Factory());
    	registerAction(ConnectToServerAction.class,              new ConnectToServerAction.Factory());
    	registerAction(CopyAction.class,                         new CopyAction.Factory());
    	registerAction(CopyFileNamesAction.class,                new CopyFileNamesAction.Factory());
    	registerAction(CopyFilePathsAction.class,                new CopyFilePathsAction.Factory());
    	registerAction(CopyFilesToClipboardAction.class,         new CopyFilesToClipboardAction.Factory());
    	registerAction(CycleBackwardThruFolderPanelAction.class, new CycleBackwardThruFolderPanelAction.Factory());
    	registerAction(CycleForwardThruFolderPanelAction.class,  new CycleForwardThruFolderPanelAction.Factory());
    	registerAction(DeleteAction.class,         			     new DeleteAction.Factory());
    	registerAction(DonateAction.class,    			         new DonateAction.Factory());
    	registerAction(EditAction.class,     			         new EditAction.Factory());
    	registerAction(EditBookmarksAction.class,                new EditBookmarksAction.Factory());
    	registerAction(EditCredentialsAction.class,              new EditCredentialsAction.Factory());
    	registerAction(EmailAction.class,          			     new EmailAction.Factory());
    	registerAction(EmptyTrashAction.class,           	     new EmptyTrashAction.Factory());
    	registerAction(ExploreBookmarksAction.class,             new ExploreBookmarksAction.Factory());
    	registerAction(GarbageCollectAction.class,               new GarbageCollectAction.Factory());
    	registerAction(GoBackAction.class,                       new GoBackAction.Factory());
    	registerAction(GoForwardAction.class,                    new GoForwardAction.Factory());
    	registerAction(GoToDocumentationAction.class,            new GoToDocumentationAction.Factory());
    	registerAction(GoToForumsAction.class,                   new GoToForumsAction.Factory());
    	registerAction(GoToHomeAction.class,                     new GoToHomeAction.Factory());
    	registerAction(GoToParentAction.class,                   new GoToParentAction.Factory());
    	registerAction(GoToParentInBothPanelsAction.class,       new GoToParentInBothPanelsAction.Factory());
    	registerAction(GoToParentInOtherPanelAction.class,       new GoToParentInOtherPanelAction.Factory());
    	registerAction(GoToRootAction.class,                     new GoToRootAction.Factory());
    	registerAction(GoToWebsiteAction.class,                  new GoToWebsiteAction.Factory());
    	registerAction(InternalEditAction.class,                 new InternalEditAction.Factory());
    	registerAction(InternalViewAction.class,                 new InternalViewAction.Factory());
    	registerAction(InvertSelectionAction.class,              new InvertSelectionAction.Factory());
    	registerAction(LocalCopyAction.class,                    new LocalCopyAction.Factory());
    	registerAction(MarkAllAction.class,           		     new MarkAllAction.Factory());
    	registerAction(MarkExtensionAction.class,            	 new MarkExtensionAction.Factory());
    	registerAction(MarkGroupAction.class,            		 new MarkGroupAction.Factory());
    	registerAction(MarkPageDownAction.class,             	 new MarkPageDownAction.Factory());
    	registerAction(MarkPageUpAction.class,                   new MarkPageUpAction.Factory());
    	registerAction(MarkSelectedFileAction.class,             new MarkSelectedFileAction.Factory());
    	registerAction(MarkUpToFirstRowAction.class,             new MarkUpToFirstRowAction.Factory());
    	registerAction(MarkUpToLastRowAction.class,              new MarkUpToLastRowAction.Factory());
    	registerAction(MaximizeWindowAction.class,               new MaximizeWindowAction.Factory());
    	registerAction(MergeFileAction.class,            		 new MergeFileAction.Factory());
    	registerAction(MinimizeWindowAction.class,               new MinimizeWindowAction.Factory());
    	registerAction(MkdirAction.class,           			 new MkdirAction.Factory());
    	registerAction(MkfileAction.class,		                 new MkfileAction.Factory());
    	registerAction(MoveAction.class,		                 new MoveAction.Factory());
    	registerAction(NewWindowAction.class,     		         new NewWindowAction.Factory());
    	registerAction(OpenAction.class,          				 new OpenAction.Factory());
    	registerAction(OpenInBothPanelsAction.class,             new OpenInBothPanelsAction.Factory());
    	registerAction(OpenInOtherPanelAction.class,             new OpenInOtherPanelAction.Factory());
//    	registerAction(OpenLocationAction.class,          	     new OpenLocationAction.Factory());
    	registerAction(OpenNativelyAction.class,       		     new OpenNativelyAction.Factory());
    	registerAction(OpenTrashAction.class,           	     new OpenTrashAction.Factory());
    	registerAction(OpenURLInBrowserAction.class,             new OpenURLInBrowserAction.Factory());
    	registerAction(PackAction.class,       			         new PackAction.Factory());
    	registerAction(PasteClipboardFilesAction.class,          new PasteClipboardFilesAction.Factory());
    	registerAction(PermanentDeleteAction.class,              new PermanentDeleteAction.Factory());
    	registerAction(PopupLeftDriveButtonAction.class,         new PopupLeftDriveButtonAction.Factory());
    	registerAction(PopupRightDriveButtonAction.class,        new PopupRightDriveButtonAction.Factory());
    	registerAction(QuitAction.class,              			 new QuitAction.Factory());
    	registerAction(RecallNextWindowAction.class,             new RecallNextWindowAction.Factory());
    	registerAction(RecallPreviousWindowAction.class,         new RecallPreviousWindowAction.Factory());
    	registerAction(RecallWindow10Action.class,               new RecallWindow10Action.Factory());
    	registerAction(RecallWindow1Action.class,                new RecallWindow1Action.Factory());
    	registerAction(RecallWindow2Action.class,                new RecallWindow2Action.Factory());
    	registerAction(RecallWindow3Action.class,                new RecallWindow3Action.Factory());
    	registerAction(RecallWindow4Action.class,                new RecallWindow4Action.Factory());
    	registerAction(RecallWindow5Action.class,                new RecallWindow5Action.Factory());
    	registerAction(RecallWindow6Action.class,                new RecallWindow6Action.Factory());
    	registerAction(RecallWindow7Action.class,                new RecallWindow7Action.Factory());
    	registerAction(RecallWindow8Action.class,                new RecallWindow8Action.Factory());
    	registerAction(RecallWindow9Action.class,                new RecallWindow9Action.Factory());
    	registerAction(RecallWindowAction.class,                 new RecallWindowAction.Factory());
    	registerAction(RefreshAction.class,        		         new RefreshAction.Factory());
    	registerAction(RenameAction.class,              		 new RenameAction.Factory());
    	registerAction(ReportBugAction.class,       	         new ReportBugAction.Factory());
    	registerAction(RevealInDesktopAction.class,              new RevealInDesktopAction.Factory());
    	registerAction(ReverseSortOrderAction.class,             new ReverseSortOrderAction.Factory());
    	registerAction(RunCommandAction.class,     		         new RunCommandAction.Factory());
    	registerAction(SelectFirstRowAction.class,               new SelectFirstRowAction.Factory());
    	registerAction(SelectLastRowAction.class,                new SelectLastRowAction.Factory());
    	registerAction(SetSameFolderAction.class,                new SetSameFolderAction.Factory());
    	registerAction(ShowAboutAction.class,          		     new ShowAboutAction.Factory());
    	registerAction(ShowBookmarksQLAction.class,              new ShowBookmarksQLAction.Factory());
    	registerAction(CustomizeCommandBarAction.class,  new CustomizeCommandBarAction.Factory());
    	registerAction(ShowFilePropertiesAction.class,           new ShowFilePropertiesAction.Factory());
    	registerAction(ShowKeyboardShortcutsAction.class,        new ShowKeyboardShortcutsAction.Factory());
    	registerAction(ShowParentFoldersQLAction.class,          new ShowParentFoldersQLAction.Factory());
    	registerAction(ShowPreferencesAction.class,              new ShowPreferencesAction.Factory());
    	registerAction(ShowRecentExecutedFilesQLAction.class,    new ShowRecentExecutedFilesQLAction.Factory());
    	registerAction(ShowRecentLocationsQLAction.class,        new ShowRecentLocationsQLAction.Factory());
    	registerAction(ShowServerConnectionsAction.class,        new ShowServerConnectionsAction.Factory());
    	registerAction(SortByDateAction.class,             		 new SortByDateAction.Factory());
    	registerAction(SortByExtensionAction.class,              new SortByExtensionAction.Factory());
    	registerAction(SortByGroupAction.class,            		 new SortByGroupAction.Factory());
    	registerAction(SortByNameAction.class,           		 new SortByNameAction.Factory());
    	registerAction(SortByOwnerAction.class,             	 new SortByOwnerAction.Factory());
    	registerAction(SortByPermissionsAction.class,            new SortByPermissionsAction.Factory());
    	registerAction(SortBySizeAction.class,              	 new SortBySizeAction.Factory());
    	registerAction(SplitEquallyAction.class,             	 new SplitEquallyAction.Factory());
    	registerAction(SplitFileAction.class,            		 new SplitFileAction.Factory());
    	registerAction(SplitHorizontallyAction.class,            new SplitHorizontallyAction.Factory());
    	registerAction(SplitVerticallyAction.class,              new SplitVerticallyAction.Factory());
    	registerAction(StopAction.class,              			 new StopAction.Factory());
    	registerAction(SwapFoldersAction.class,       	         new SwapFoldersAction.Factory());
    	registerAction(SwitchActiveTableAction.class,            new SwitchActiveTableAction.Factory());
    	registerAction(ToggleAutoSizeAction.class,               new ToggleAutoSizeAction.Factory());
//    	registerAction(ToggleColumnAction.class,           	     new ToggleColumnAction.Factory());
    	registerAction(ToggleCommandBarAction.class,             new ToggleCommandBarAction.Factory());
    	registerAction(ToggleDateColumnAction.class,             new ToggleDateColumnAction.Factory());
    	registerAction(ToggleExtensionColumnAction.class,        new ToggleExtensionColumnAction.Factory());
    	registerAction(ToggleGroupColumnAction.class,            new ToggleGroupColumnAction.Factory());
    	registerAction(ToggleHiddenFilesAction.class,            new ToggleHiddenFilesAction.Factory());
    	registerAction(ToggleOwnerColumnAction.class,            new ToggleOwnerColumnAction.Factory());
    	registerAction(TogglePermissionsColumnAction.class,      new TogglePermissionsColumnAction.Factory());
    	registerAction(ToggleShowFoldersFirstAction.class,       new ToggleShowFoldersFirstAction.Factory());
    	registerAction(ToggleSizeColumnAction.class,             new ToggleSizeColumnAction.Factory());
    	registerAction(ToggleStatusBarAction.class,              new ToggleStatusBarAction.Factory());
    	registerAction(ToggleToolBarAction.class,                new ToggleToolBarAction.Factory());
    	registerAction(ToggleTreeAction.class,             		 new ToggleTreeAction.Factory());
    	registerAction(UnmarkAllAction.class,            		 new UnmarkAllAction.Factory());
    	registerAction(UnmarkGroupAction.class,            		 new UnmarkGroupAction.Factory());
    	registerAction(UnpackAction.class,             			 new UnpackAction.Factory());
    	registerAction(ViewAction.class,              			 new ViewAction.Factory());
    }
    
    public static Enumeration getActionClasses() {
    	return actionClasses.elements();
    }
    
    /**
     * This method is used to fetch all the action classes sorted by their labels.
     * 
     * @return sorted list of the action classes.
     */
    public static List getSortedActionClasses() {
    	// Convert the action-classes to MuAction instances
		List list = Collections.list(actionClasses.elements());
		
		// Sort actions by their labels
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				// TODO: remove actions without a standard label?
				if (MuAction.getStandardLabel((Class) o1) == null)
					return 1;
				if (MuAction.getStandardLabel((Class) o2) == null)
					return -1;
				return MuAction.getStandardLabel((Class) o1).compareTo(MuAction.getStandardLabel((Class) o2));
			}
		});
		
		return list;
    }
    
    public static void registerAction(Class actionClass, MuActionFactory actionFactory) {
    	actionFactories.put(actionClass, actionFactory);
    	actionClasses.put(actionClass.getName(), actionClass);
    }
    
    public static Class getActionClass(String actionClassName) {
    	return (Class) actionClasses.get(actionClassName);
    }

    /**
     * Convenience method that returns an instance of the given MuAction class, and associated with the specified
     * MainFrame. This method creates an ActionDescriptor with no initial property, passes it to
     * {@link #getActionInstance(ActionDescriptor, MainFrame)} and returns the MuAction instance.
     *
     * @param actionClass the MuAction class to instantiate
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given MuAction Class and MainFrame, <code>null</code> if the
     * class could not be found or could not be instantiated.
     */
    public static MuAction getActionInstance(Class actionClass, MainFrame mainFrame) {
        return getActionInstance(new ActionDescriptor(actionClass), mainFrame);
    }

    /**
     * Helper method to get action instance of the given MuAction class in the current MainFrame.
     * 
     * @param actionClass - MuAction class.
     * @return the corresponding MuAction instance for the given MuAction class in the current MainFrame.
     */
    public static MuAction getActionInstance(Class actionClass) {
        return getActionInstance(new ActionDescriptor(actionClass), WindowManager.getCurrentMainFrame());
    }

    /**
     * Returns an instance of the MuAction class denoted by the given ActionDescriptor, for the specified MainFrame.
     * If an existing instance corresponding to the same ActionDescriptor and MainFrame is found, it is simply returned.
     * If no matching instance could be found, a new instance is created, added to the internal action instances map
     * (for further use) and returned.
     * If the MuAction denoted by the specified ActionDescriptor cannot be found or cannot be instantiated,
     * <code>null</code> is returned.
     *
     * @param actionDescriptor a descriptor of the action class to instantiate with initial properties
     * @param mainFrame the MainFrame instance the action belongs to
     * @return a MuAction instance matching the given ActionDescriptor and MainFrame, <code>null</code> if the
     * MuAction class denoted by the ActionDescriptor could not be found or could not be instantiated.
     */
    public static MuAction getActionInstance(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
//      if(Debug.ON) Debug.trace("called, actionDescriptor = "+actionDescriptor, 5);

        Hashtable mainFrameActions = (Hashtable)mainFrameActionsMap.get(mainFrame);
        if(mainFrameActions==null) {
//            if(Debug.ON) Debug.trace("creating MainFrame action map");

            mainFrameActions = new Hashtable();
            mainFrameActionsMap.put(mainFrame, mainFrameActions);
        }

        // Looks for an existing MuAction instance used by the specified MainFrame
        MuAction action = (MuAction)mainFrameActions.get(actionDescriptor);
        if(action==null) {
            Class actionClass = actionDescriptor.getActionClass();

            // Looks for the action's factory
            MuActionFactory actionFactory = (MuActionFactory) actionFactories.get(actionClass);
            if(actionFactory == null) {
            	if(Debug.ON) Debug.trace("couldn't initiate action: " + actionClass.getName() + ", its factory wasn't found");
            	return null;
            }

//          if(Debug.ON) Debug.trace("creating instance");

            Hashtable properties = actionDescriptor.getInitProperties();
            // If no properties hashtable is specified in the action descriptor
            if(properties==null) {
            	properties = new Hashtable();
            }
            // else clone the hashtable to ensure that it doesn't get modified by action instances.
            // Since cloning is an expensive operation, this is done only if the hashtable is not empty.
            else if(!properties.isEmpty()) {
            	properties = (Hashtable)properties.clone();
            }

            // Instanciate the MuAction class
            action = actionFactory.createAction(mainFrame, properties);
            mainFrameActions.put(actionDescriptor, action);

//          if(Debug.ON) Debug.trace("nb action instances = "+mainFrameActions.size());
        }
//      else {
//      	if(Debug.ON) Debug.trace("found existing action instance: "+action);
//      }

        return action;
    }


    /**
     * Returns a Vector of all MuAction instances matching the specified Class.
     *
     * @param muActionClass the MuAction class to compare instances against
     * @return  a Vector of all MuAction instances matching the specified Class
     */
    public static Vector getActionInstances(Class muActionClass) {
        Vector actionInstances = new Vector();

        // Iterate on all MainFrame instances
        Iterator mainFrameActions = mainFrameActionsMap.values().iterator();
        while(mainFrameActions.hasNext()) {
            Iterator actions = ((Hashtable)mainFrameActions.next()).values().iterator();
            // Iterate on all the MainFrame's actions
            while(actions.hasNext()) {
                MuAction action = (MuAction)actions.next();
                if(action.getClass().equals(muActionClass)) {
                    // Found an action matching the specified class
                    actionInstances.add(action);
                    // Jump to the next MainFrame
                    break;
                }
            }
        }

// if(Debug.ON) Debug.trace("returning "+actionInstances);
        return actionInstances;
    }


    /**
     * Convenience method that retrieves an instance of the MuAction denoted by the given Class and associated
     * with the given {@link MainFrame} and calls {@link MuAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionClass the class of the MuAction to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise 
     */
    public static boolean performAction(Class actionClass, MainFrame mainFrame) {
        return performAction(new ActionDescriptor(actionClass), mainFrame);
    }


    /**
     * Convenience method that retrieves an instance of the MuAction denoted by the given {@link ActionDescriptor}
     * and associated with the given {@link com.mucommander.ui.main.MainFrame} and calls {@link MuAction#performAction()} on it.
     * Returns <code>true</code> if an instance of the action could be retrieved and performed, <code>false</code>
     * if the MuAction could not be found or could not be instantiated.
     *
     * @param actionDescriptor the ActionDescriptor of the action to perform
     * @param mainFrame the MainFrame the action belongs to
     * @return true if the action instance could be retrieved and the action performed, false otherwise
     */
    public static boolean performAction(ActionDescriptor actionDescriptor, MainFrame mainFrame) {
        MuAction action = getActionInstance(actionDescriptor, mainFrame);

        if(action==null)
            return false;

        action.performAction();

        return true;
    }
}
