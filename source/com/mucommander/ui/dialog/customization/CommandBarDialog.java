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

package com.mucommander.ui.dialog.customization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.Box.Filler;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicHorizontalWrapList;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.commandbar.CommandBar;
import com.mucommander.ui.main.commandbar.CommandBarAttributes;
import com.mucommander.ui.text.RecordingKeyStrokeTextField;
import com.mucommander.util.AlteredVector;

/**
 * Dialog used to customize the command-bar.
 * 
 * @author Arik Hadas
 */
public class CommandBarDialog extends CustomizeDialog {
	
	/** ID numbers of the lists*/
	private static final int COMMAND_BAR_BUTTONS_LIST_ID              = 0;
	private static final int COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID  = 1;
	private static final int AVAILABLE_BUTTONS_LIST_ID                = 2;
	
	/** Constants that represent left & right sides */
	private static final int LEFT  = 0;
	private static final int RIGHT = 1;
	
	private static final int MODIFIER_FIELD_MAX_LENGTH = 6;
	
	/** Used to ensure we only have the one preferences dialog open at any given time. */
    private static CommandBarDialog singleton;
    
    /** The preferred size of button in the lists */
	public static final Dimension BUTTON_PREFERRED_SIZE = new Dimension(130, 30);
	
	/** The color that is used to paint highlighted button's border */
	private final Color PAINTED_BORDER_COLOR = Color.gray;
	/** The default color of button's border */
	private final Color JBUTTON_BACKGROUND_COLOR = UIManager.getColor("jbutton.background");
	
	/** List that contains all available buttons, i.e buttons that are not used by the command bar */
	private DynamicHorizontalWrapList commandBarAvailableButtonsList;
	/** List that contains the command-bar regular buttons (i.e, not alternative buttons) */
	private DynamicList  commandBarButtonsList;
	/** List that contains the command-bar alternative buttons */
	private DynamicList  commandBarAlternativeButtonsList;
	
	/** Vector that contains the buttons in the available buttons list */
	private AlteredVector commandBarAvailableButtons;
	/** Vector that contains the buttons in the command-bar regular buttons list */
	private AlteredVector commandBarButtons;
	/** Vector that contains the buttons in the command-bar alternative buttons list */
	private AlteredVector commandBarAlternativeButtons;
	
	private RecordingKeyStrokeTextField modifierField;
	
	private DataIndexAndSource transferedButtonProperties;
	private JButton transferedButton;
	private int selectedCommandBarButtonIndex = -2;
	private int selectedCommandBarButtonSide;
	private int selectedCommandBarAlternateButtonIndex = -2;
	private boolean isImported = false;
	private boolean canImport = false;
	
	private CommandBarDialog() {
		super(WindowManager.getCurrentMainFrame(), Translator.get("command_bar_customize_dialog.title"));
	}
	
	// - Singleton management ---------------------------------------------------
    // --------------------------------------------------------------------------
    public static synchronized CommandBarDialog getDialog() {
        // If no instance already exists, create a new one.
        if(singleton == null)
            singleton = new CommandBarDialog();

        return singleton;
    }
    
    protected void componentChanged() {
    	setCommitButtonsEnabled(areActionsChanged() || areAlternativeActionsChanged() || isModifierChanged());    		
    }
    
    private boolean areActionsChanged() {
    	// Fetch command-bar actions
    	Class[] commandBarActions = CommandBarAttributes.getActions();
    	int nbActions = commandBarActions.length;
    	for (int i=0; i<nbActions; ++i)
    		if (!((JButton) commandBarButtons.get(i)).getAction().getClass().equals(commandBarActions[i]))
    			return true;
    	return false;
    }
    
    private boolean areAlternativeActionsChanged() {
    	// Fetch command-bar alternative actions
    	Class[] commandBarAlternativeActions = CommandBarAttributes.getAlternateActions();
    	int nbActions = commandBarAlternativeActions.length;
    	for (int i=0; i<nbActions; ++i) {
    		Object component = commandBarAlternativeButtons.get(i);
    		if (!(component instanceof JButton)) {
    			if (commandBarAlternativeActions[i] != null)
    				return true;
    		}
    		else if (!((JButton) component).getAction().getClass().equals(commandBarAlternativeActions[i]))
    			return true;
    	}
    	return false;
    }
    
    private boolean isModifierChanged() {
    	return !modifierField.getKeyStroke().equals(CommandBarAttributes.getModifier());
    }
	
	protected void commit() {
		int nbNewActions = commandBarButtons.size();
		Class[] newActions = new Class[nbNewActions];
		for (int i=0; i<nbNewActions; ++i)
			newActions[i] = ((JButton) commandBarButtons.get(i)).getAction().getClass();
		
		int nbNewAlternativeActions = commandBarAlternativeButtons.size();
		Class[] newAlternativeActions = new Class[nbNewAlternativeActions];
		for (int i=0; i<nbNewAlternativeActions; ++i) {
			Object component = commandBarAlternativeButtons.get(i);
			newAlternativeActions[i] = (component instanceof JButton) ? 
										((JButton) component).getAction().getClass() : null;
		}
		
		CommandBarAttributes.setActions(newActions, newAlternativeActions);
		CommandBarAttributes.setModifier(modifierField.getKeyStroke());
	}

	protected JPanel createCustomizationPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		commandBarAvailableButtons   = new AlteredVector();
		commandBarButtons            = new AlteredVector();
		commandBarAlternativeButtons = new AlteredVector();
		
		// a Set that contains all actions that are used by the command-bar (as regular or alternative buttons).
		Set usedActions = new HashSet();
		usedActions.addAll(initCommandBarActionsList());
		usedActions.addAll(initCommandBarAlternateActionsList());
		initActionsPoolList(usedActions);
		
		panel.add(createAvailableButtonsPanel(), BorderLayout.CENTER);
		panel.add(createCommandBarPanel(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	private Collection initCommandBarAlternateActionsList() {
		Class[] commandBarActionClasses = CommandBarAttributes.getAlternateActions();
		int nbCommandBarActionClasses = commandBarActionClasses.length;
		for (int i=0; i<nbCommandBarActionClasses; ++i) {
			Class actionClass = commandBarActionClasses[i];
			if (actionClass == null) {
				Component c = Box.createHorizontalGlue();
				c.setPreferredSize(BUTTON_PREFERRED_SIZE);
				commandBarAlternativeButtons.add(c);
			}
			else {
				JButton button = CommandBar.createCommandBarButton(ActionManager.getActionInstance(actionClass));
				prepareButton(button);
				commandBarAlternativeButtons.add(button);
			}
		}
		
		commandBarAlternativeButtonsList = new DynamicList(commandBarAlternativeButtons);
		
		commandBarAlternativeButtonsList.setCellRenderer(new CommandBarAlternativeButtonListRenderer());
		commandBarAlternativeButtonsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		commandBarAlternativeButtonsList.setVisibleRowCount(1);
		commandBarAlternativeButtonsList.setDragEnabled(true);
		commandBarAlternativeButtonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commandBarAlternativeButtonsList.setTransferHandler(new CommandBarAlternateActionsListTransferHandler());
		commandBarAlternativeButtonsList.setBackground(UIManager.getColor("jbutton.background"));
		
		try {
			commandBarAlternativeButtonsList.getDropTarget().addDropTargetListener((new DropTargetListener() {

				public void dragEnter(DropTargetDragEvent dtde) { }

				public void dragExit(DropTargetEvent dte) {
					selectedCommandBarAlternateButtonIndex = -2;
					commandBarAlternativeButtonsList.repaint();
				}

				public void dragOver(DropTargetDragEvent dtde) {
					int index = canImport ? commandBarAlternativeButtonsList.locationToIndex(dtde.getLocation()): -2;
					
					if (index != selectedCommandBarAlternateButtonIndex) {
						selectedCommandBarAlternateButtonIndex = index;
						commandBarAlternativeButtonsList.repaint();
					}
				}

				public void drop(DropTargetDropEvent dtde) {
					if (!isImported)
						return;

					Point dropLocation = dtde.getLocation();
					int index = commandBarButtonsList.locationToIndex(dropLocation);
					
					JComponent component = (JComponent) commandBarAlternativeButtons.remove(index);
					if (component instanceof JButton)
						insertInOrder(commandBarAvailableButtons, (JButton) component);
					
					commandBarAlternativeButtons.add(index, transferedButton);
					commandBarAlternativeButtonsList.ensureIndexIsVisible(index);
				}

				public void dropActionChanged(DropTargetDragEvent dtde) { }
				
			}));
		} catch (TooManyListenersException e) {
			// Should never happen
			e.printStackTrace();
		}
		
		return Arrays.asList(commandBarActionClasses);
	}
	
	private void initActionsPoolList(Set usedActions) {
		List sortedActionClasses = ActionManager.getSortedActionClasses();
		Iterator sortedActionClassesIterator = sortedActionClasses.iterator();
		while(sortedActionClassesIterator.hasNext()) {
			Class actionClass = (Class) sortedActionClassesIterator.next();
			if (!usedActions.contains(actionClass)) {
				JButton button = CommandBar.createCommandBarButton(ActionManager.getActionInstance(actionClass));
				prepareButton(button);
				insertInOrder(commandBarAvailableButtons, button);
			}
		}

		commandBarAvailableButtonsList = new DynamicHorizontalWrapList(commandBarAvailableButtons, BUTTON_PREFERRED_SIZE.width, 6);
		
		commandBarAvailableButtonsList.setCellRenderer(new AvailableButtonCellListRenderer());
		commandBarAvailableButtonsList.setDragEnabled(true);
		commandBarAvailableButtonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commandBarAvailableButtonsList.setTransferHandler(new ActionsPoolTransferHandler());
		commandBarAvailableButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);
	}
	
	protected JPanel createAvailableButtonsPanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		panel.setBorder(BorderFactory.createTitledBorder(Translator.get("command_bar_customize_dialog.available_actions")));
		
		JScrollPane scrollPane = new JScrollPane(commandBarAvailableButtonsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		panel.add(scrollPane);
		
		return panel;
	}
	
	protected JPanel createCommandBarPanel() {
		YBoxPanel panel = new YBoxPanel();
		panel.setBorder(BorderFactory.createTitledBorder(Translator.get("command_bar_customize_dialog.command_bar")));
		
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		YBoxPanel listsPanel = new YBoxPanel();
		listsPanel.add(commandBarButtonsList);
		listsPanel.add(commandBarAlternativeButtonsList);		
		
		JScrollPane scrollPane = new JScrollPane(listsPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		panel.add(scrollPane);

		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		// TODO: translator\remove
		panel.add(new JLabel("(Drag buttons to customize the command bar)"));
		
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		JPanel modifierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modifierField = new RecordingKeyStrokeTextField(MODIFIER_FIELD_MAX_LENGTH, CommandBarAttributes.getModifier()) {
			public void setText(String t) {
				super.setText(t);
				componentChanged();
			}
			
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() != KeyEvent.VK_ENTER)
					super.keyPressed(e);
			}
		};
		
		modifierPanel.add(new JLabel(Translator.get("command_bar_customize_dialog.modifier")));
		modifierPanel.add(modifierField);
		panel.add(modifierPanel);
		
		return panel;
	}
	
	private static void prepareButton(JButton button) {
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_PREFERRED_SIZE);
	}
	
	private Collection initCommandBarActionsList() {
		Class[] commandBarActionClasses = CommandBarAttributes.getActions();
		int nbCommandBarActionClasses = commandBarActionClasses.length;
		for (int i=0; i<nbCommandBarActionClasses; ++i) {
			JButton button = CommandBar.createCommandBarButton(ActionManager.getActionInstance(commandBarActionClasses[i]));
			prepareButton(button);
			commandBarButtons.add(button);
		}
		
		commandBarButtonsList = new DynamicList(commandBarButtons); 
		
		// Set lists cells renderer
		commandBarButtonsList.setCellRenderer(new CommandBarButtonListCellRenderer());
		// Horizontal layout
		commandBarButtonsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		// All buttons appear in one row
		commandBarButtonsList.setVisibleRowCount(1);
		// Drag operations are supported
		commandBarButtonsList.setDragEnabled(true);
		// Can select only button at a time
		commandBarButtonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Set transfer handler
		commandBarButtonsList.setTransferHandler(new CommandBarActionsListTransferHandler());

		// Add drop-target-listener to the list.
		try {
			commandBarButtonsList.getDropTarget().addDropTargetListener(new DropTargetListener() {

				public void dragEnter(DropTargetDragEvent dtde) { }

				public void dragExit(DropTargetEvent dte) {
					selectedCommandBarButtonIndex = -2;
					commandBarButtonsList.repaint();
				}

				public void dragOver(DropTargetDragEvent dtde) {
					int index;
					int newSide = 0;
					
					if (canImport) {
						Point dropLocation = dtde.getLocation();
						index = commandBarButtonsList.locationToIndex(dropLocation);
						Point cellLocation = commandBarButtonsList.indexToLocation(index);
						newSide = cellLocation.x + BUTTON_PREFERRED_SIZE.width / 2 > dropLocation.x ? LEFT : RIGHT;
					}
					else
						index = -2;
					
					if (index != selectedCommandBarButtonIndex || selectedCommandBarButtonSide != newSide) {
						selectedCommandBarButtonIndex = index;
						selectedCommandBarButtonSide = newSide;
						commandBarButtonsList.repaint();
					}
				}

				public void drop(DropTargetDropEvent dtde) {
					if (!isImported)
						return;
					
					// get the mouse position when the drop operation happened
					Point dropLocation = dtde.getLocation();
					// convert the above mouse position to the corresponding cell index in the command bar regular buttons list
					int index = commandBarButtonsList.locationToIndex(dropLocation);
					// get the cell position 
					Point cellLocation = commandBarButtonsList.indexToLocation(index);
					if (cellLocation.x + BUTTON_PREFERRED_SIZE.width / 2 < dropLocation.x)
						index++;

					commandBarButtons.add(index, transferedButton);
					commandBarAlternativeButtons.add(index, transferedButtonProperties.getSource() == COMMAND_BAR_BUTTONS_LIST_ID ?
																commandBarAlternativeButtons.remove(transferedButtonProperties.getIndex()) :
																Box.createHorizontalGlue());
					
					commandBarButtonsList.ensureIndexIsVisible(index);
				}

				public void dropActionChanged(DropTargetDragEvent dtde) { }
				
			});
		} catch (TooManyListenersException e) {
			// Should never happen
			e.printStackTrace();
		}
		
		return Arrays.asList(commandBarActionClasses);
	}
	
	private class CommandBarButtonListCellRenderer implements ListCellRenderer {
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JButton button = (JButton) value;

			JButton but = new JButton(button.getText(), button.getIcon());
			but.setToolTipText(button.getToolTipText());
			but.setPreferredSize(button.getPreferredSize());
			
			if (selectedCommandBarButtonSide == LEFT ) {
				if (index == selectedCommandBarButtonIndex)
					but.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(
									BorderFactory.createMatteBorder(0, 2, 0, 0, PAINTED_BORDER_COLOR),
									BorderFactory.createMatteBorder(0, 0, 0, 2, JBUTTON_BACKGROUND_COLOR)),
									button.getBorder()));
				else 
					but.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 2, 0, 2, JBUTTON_BACKGROUND_COLOR),
							button.getBorder()));
			}
			else { // side == RIGHT
				if (index == selectedCommandBarButtonIndex + 1)
					but.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(
									BorderFactory.createMatteBorder(0, 2, 0, 0, PAINTED_BORDER_COLOR),
									BorderFactory.createMatteBorder(0, 0, 0, 2, JBUTTON_BACKGROUND_COLOR)),
									button.getBorder()));
				else if (index == selectedCommandBarButtonIndex && selectedCommandBarButtonIndex == commandBarButtons.size() - 1)
					but.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(
									BorderFactory.createMatteBorder(0, 2, 0, 0, JBUTTON_BACKGROUND_COLOR),
									BorderFactory.createMatteBorder(0, 0, 0, 2, PAINTED_BORDER_COLOR)),
									button.getBorder()));
				else 
					but.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 2, 0, 2, JBUTTON_BACKGROUND_COLOR),
							button.getBorder()));
			}
			
			return but;
		}
	}
	
	private class CommandBarAlternativeButtonListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value instanceof JButton) {
				JButton button = ((JButton) value);
				
				JButton but = new JButton(button.getText(), button.getIcon());
				but.setPreferredSize(button.getPreferredSize());
				but.setToolTipText(button.getToolTipText());
				but.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(2, 2, 2, 2, index == selectedCommandBarAlternateButtonIndex ? PAINTED_BORDER_COLOR : JBUTTON_BACKGROUND_COLOR),
						button.getBorder()));
				
				return but;
			}
			else {
				Box.Filler filler = (Filler) value;
				filler.setPreferredSize(BUTTON_PREFERRED_SIZE);
				filler.setBorder(BorderFactory.createLineBorder(JBUTTON_BACKGROUND_COLOR , 0));
				filler.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(2, 2, 2, 2, index == selectedCommandBarAlternateButtonIndex ? PAINTED_BORDER_COLOR : JBUTTON_BACKGROUND_COLOR ),
						filler.getBorder()));
				return filler;
			}
		}
	}
	
	private static class AvailableButtonCellListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JComponent component = (JComponent) value;
			panel.add(component);
			panel.setToolTipText(component.getToolTipText());
			
			return panel;
		}
	}	

	public static class DataIndexAndSource {
		private int index;
		private int source;
		
		public DataIndexAndSource(int index, int source) {
			this.index = index;
			this.source = source;
		}
		
		public int getIndex() { return index; }
		public int getSource() { return source; }
	}
	
	protected static class TransferableListData implements Transferable {
		
		private DataIndexAndSource data;
		private DataFlavor flavor;
		
		public TransferableListData(int index, JComponent selectedValue, int source) {
			try {
				flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + selectedValue.getClass().getName() + "\"");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			data = new DataIndexAndSource(index, source);
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException { return data; }

		public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] {flavor}; }

		public boolean isDataFlavorSupported(DataFlavor flavor) { return this.flavor.equals(flavor); }
	}

	
	private abstract class ActionListTransferHandler extends TransferHandler {

		private DataFlavor flavor;
		
		public ActionListTransferHandler() {
			try {
				flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
				           ";class=\"" + com.mucommander.ui.button.NonFocusableButton.class.getName() + "\"");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		protected abstract int getListId();
				
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			canImport = false;
			int nbFlavors = transferFlavors.length;
			for (int i=0; i<nbFlavors; ++i)
				if (flavor.equals(transferFlavors[i])) {
					canImport = true;
					break;
				}
			return canImport;
		}
		
		protected Transferable createTransferable(JComponent component) {
			JList list = (JList) component;
			return new TransferableListData(list.getSelectedIndex(), (JComponent) list.getSelectedValue(), getListId());
		}
		
		public abstract boolean importData(JComponent comp, Transferable t);
		
		protected void exportDone(JComponent component, Transferable data, int action) {
			if (isImported)
				componentChanged();	
			
			isImported = false;
			selectedCommandBarButtonIndex = -2;
			selectedCommandBarAlternateButtonIndex = -2;
		}

		public int getSourceActions(JComponent component) { return TransferHandler.MOVE; }
	}
	
	private class CommandBarAlternateActionsListTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
				e.printStackTrace();
				return isImported = false;
			}
			
			int source = transferedButtonProperties.getSource();
			if (source == AVAILABLE_BUTTONS_LIST_ID) {
				transferedButton = (JButton) commandBarAvailableButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = (JButton) commandBarButtons.remove(transferedIndex);
				
				// alternative button:
				JComponent component = (JComponent) commandBarAlternativeButtons.remove(transferedIndex);
				if (component instanceof JButton)
					insertInOrder(commandBarAvailableButtons, (JButton) component);
			}
			else if (source == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = (JButton) commandBarAlternativeButtons.remove(transferedIndex);
				
				Component c = Box.createHorizontalGlue();
				c.setPreferredSize(BUTTON_PREFERRED_SIZE);
				commandBarAlternativeButtons.add(transferedIndex, c);
			}
			
			return isImported = true;
		}

		protected int getListId() {
			return COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID;
		}
	}
	
	private class CommandBarActionsListTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
				e.printStackTrace();
				return isImported = false;
			}

			int source = transferedButtonProperties.getSource();
			if (source == AVAILABLE_BUTTONS_LIST_ID) {
				transferedButton = (JButton) commandBarAvailableButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_BUTTONS_LIST_ID) {
				transferedButton = (JButton) commandBarButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = (JButton) commandBarAlternativeButtons.remove(transferedIndex);
				
				Component c = Box.createHorizontalGlue();
				c.setPreferredSize(BUTTON_PREFERRED_SIZE);
				commandBarAlternativeButtons.add(transferedIndex, c);
			}
			
			return isImported = true;
		}

		protected int getListId() { return COMMAND_BAR_BUTTONS_LIST_ID; }
	}
	
	private class ActionsPoolTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
				e.printStackTrace();
				return isImported = false;
			}

			// Do not import my own buttons..
			if (transferedButtonProperties.getSource() == AVAILABLE_BUTTONS_LIST_ID)
				isImported = false;
			else {
				int transferedIndex = transferedButtonProperties.getIndex();
				int insertionPlace = 0;
				
				if (transferedButtonProperties.getSource() == COMMAND_BAR_BUTTONS_LIST_ID) {
					// regular button:
					JButton button = (JButton) commandBarButtons.remove(transferedIndex);
					insertionPlace = insertInOrder(commandBarAvailableButtons, button);
					
					// alternative button:
					JComponent component = (JComponent) commandBarAlternativeButtons.remove(transferedIndex);
					if (component instanceof JButton)
						insertInOrder(commandBarAvailableButtons, (JButton) component);
				}
				else if (transferedButtonProperties.getSource() == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
					JComponent component = (JComponent) commandBarAlternativeButtons.remove(transferedIndex);
					if (component instanceof JButton) {
						insertionPlace = insertInOrder(commandBarAvailableButtons, (JButton) component);
						
						Component c = Box.createHorizontalGlue();
						c.setPreferredSize(BUTTON_PREFERRED_SIZE);
						commandBarAlternativeButtons.add(transferedIndex, c);
					}
				}
				
				commandBarAvailableButtonsList.ensureIndexIsVisible(insertionPlace);
				isImported = true;
			}
			
			
			return isImported;
		}

		protected int getListId() { return AVAILABLE_BUTTONS_LIST_ID; }
	}
	
	//////////////////////////
	///// Helper methods /////
	//////////////////////////
	
	public static int insertInOrder(Vector vector, JButton element) {
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				// TODO: remove actions without a standard label?
				if (((JButton) o1).getText() == null)
					return 1;
				if (((JButton) o2).getText() == null)
					return -1;
				return ((JButton) o1).getText().compareTo(((JButton) o2).getText());
			}
		};
		
		if (vector.size() != 0) {
			int index = findPlace(vector, element, comparator, 0, vector.size() - 1);
			vector.add(index, element);
			return index;
		}
		else {
			vector.add(element);
			return vector.size();
		}
	}
	
	public static int findPlace(Vector vector, JButton element, Comparator comparator, int first, int last) {
		if (comparator.compare(vector.elementAt(last), element) < 0)
			return last + 1;
		if (comparator.compare(vector.elementAt(first), element) > 0)
			return first;
		if (last - first == 1)
			return last;
		
		int middle = (first + last) / 2;
		int result = comparator.compare(vector.elementAt(middle), element);
		return result > 0 ?
				findPlace(vector, element, comparator, first, middle) :
				findPlace(vector, element, comparator, middle, last);
	}
}
