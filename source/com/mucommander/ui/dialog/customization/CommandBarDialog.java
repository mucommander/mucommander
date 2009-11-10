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

import com.mucommander.AppLogger;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.CustomizeCommandBarAction;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicHorizontalWrapList;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.commandbar.CommandBarAttributes;
import com.mucommander.ui.main.commandbar.CommandBarButtonForDisplay;
import com.mucommander.ui.main.commandbar.CommandBarIO;
import com.mucommander.ui.text.RecordingKeyStrokeTextField;
import com.mucommander.util.AlteredVector;

import javax.swing.*;
import javax.swing.Box.Filler;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

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
	
	/** The color that is used to paint highlighted button's border */
	private static final Color PAINTED_BORDER_COLOR = Color.gray;
	/** The default color of button's border */
	private static final Color JBUTTON_BACKGROUND_COLOR = UIManager.getColor("button.background");
	
	private static final Border JBUTTON_BORDER = UIManager.getBorder("button.border");

	/** List that contains all available buttons, i.e buttons that are not used by the command bar */
	private DynamicHorizontalWrapList<JButton> commandBarAvailableButtonsList;
	/** List that contains the command-bar regular buttons (i.e, not alternative buttons) */
	private DynamicList<JButton>  commandBarButtonsList;
	/** List that contains the command-bar alternative buttons */
	private DynamicList<JButton> commandBarAlternateButtonsList;
	
	/** Vector that contains the buttons in the available buttons list */
	private AlteredVector<JButton> commandBarAvailableButtons;
	/** Vector that contains the buttons in the command-bar regular buttons list */
	private AlteredVector<JButton> commandBarButtons;
	/** Vector that contains the buttons in the command-bar alternate buttons list */
	private AlteredVector<JButton> commandBarAlternateButtons;
	/** Field which allows the user to enter new KeyStroke modifier for command-bar */
	private RecordingKeyStrokeTextField modifierField;
	
	/** DnD helper fields */
	private DataIndexAndSource transferedButtonProperties;
	private JButton transferedButton;
	private int selectedCommandBarButtonIndex = -2;
	private int selectedCommandBarButtonSide;
	private int selectedCommandBarAlternateButtonIndex = -2;
	private boolean isImported = false;
	private boolean canImport = false;

    public CommandBarDialog(MainFrame mainFrame) {
		super(mainFrame, ActionProperties.getActionLabel(CustomizeCommandBarAction.Descriptor.ACTION_ID));
	}
	
    protected void componentChanged() {
    	setCommitButtonsEnabled(areActionsChanged() || areAlternativeActionsChanged() || isModifierChanged());    		
    }
    
    private boolean areActionsChanged() {
    	// Fetch command-bar action ids
    	String[] commandBarActionIds = CommandBarAttributes.getActions();
    	int nbActions = commandBarActionIds.length;
    	
    	if (nbActions != commandBarButtons.size())
    		return true;
    	
    	for (int i=0; i<nbActions; ++i) {
    		CommandBarButtonForDisplay buttonI = (CommandBarButtonForDisplay) commandBarButtons.get(i);
    		if (buttonI == null) {
    			if (commandBarActionIds[i] != null)
    				return true;
    		}
    		else if (!buttonI.getActionId().equals(commandBarActionIds[i]))
    			return true;
    	}
    	return false;
    }
    
    private boolean areAlternativeActionsChanged() {
    	// Fetch command-bar alternative actions
    	String[] commandBarAlternativeActionIds = CommandBarAttributes.getAlternateActions();
    	int nbActions = commandBarAlternativeActionIds.length;
    	
    	if (nbActions != commandBarAlternateButtons.size())
    		return true;
    	
    	for (int i=0; i<nbActions; ++i) {
    		CommandBarButtonForDisplay buttonI = (CommandBarButtonForDisplay) commandBarAlternateButtons.get(i);
    		if (buttonI == null) {
    			if (commandBarAlternativeActionIds[i] != null)
    				return true;
    		}
    		else if (!buttonI.getActionId().equals(commandBarAlternativeActionIds[i]))
    			return true;
    	}
    	return false;
    }
    
    private boolean isModifierChanged() {
    	return !modifierField.getKeyStroke().equals(CommandBarAttributes.getModifier());
    }
	
	protected void commit() {
		int nbNewActions = commandBarButtons.size();
		String[] newActionIds = new String[nbNewActions];
		for (int i=0; i<nbNewActions; ++i) {
			newActionIds[i] = ((CommandBarButtonForDisplay) commandBarButtons.get(i)).getActionId();
		}
		
		int nbNewAlternativeActions = commandBarAlternateButtons.size();
		String[] newAlternativeActionIds = new String[nbNewAlternativeActions];
		for (int i=0; i<nbNewAlternativeActions; ++i) {
			Object button = commandBarAlternateButtons.get(i);
			newAlternativeActionIds[i] = (button != null) ? 
										((CommandBarButtonForDisplay) button).getActionId() : null;
		}
		
		CommandBarAttributes.setAttributes(newActionIds, newAlternativeActionIds, modifierField.getKeyStroke());
		CommandBarIO.setModified();
	}

	protected JPanel createCustomizationPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		commandBarAvailableButtons   = new AlteredVector<JButton>();
		commandBarButtons            = new AlteredVector<JButton>();
		commandBarAlternateButtons   = new AlteredVector<JButton>();
		
		// a Set that contains all actions that are used by the command-bar (as regular or alternate buttons).
		Set<String> usedActions = new HashSet<String>();
		usedActions.addAll(initCommandBarActionsList());
		usedActions.addAll(initCommandBarAlternateActionsList());
		initActionsPoolList(usedActions);
		
		panel.add(createAvailableButtonsPanel(), BorderLayout.CENTER);
		panel.add(createCommandBarPanel(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	private Collection<String> initCommandBarActionsList() {
		String[] commandBarActionIds = CommandBarAttributes.getActions();
		int nbCommandBarActionIds = commandBarActionIds.length;
		for (int i=0; i<nbCommandBarActionIds; ++i)
			commandBarButtons.add(CommandBarButtonForDisplay.create(commandBarActionIds[i]));
		
		commandBarButtonsList = new DynamicList<JButton>(commandBarButtons);
		
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
		// Set list's background color
		commandBarButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);

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
						newSide = cellLocation.x + CommandBarButtonForDisplay.PREFERRED_SIZE.width / 2 > dropLocation.x ? LEFT : RIGHT;
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
					if (cellLocation.x + CommandBarButtonForDisplay.PREFERRED_SIZE.width / 2 < dropLocation.x)
						index++;

					index += transferedButtonProperties.getSource() == COMMAND_BAR_BUTTONS_LIST_ID && index > transferedButtonProperties.getIndex() ? -1 : 0;
					commandBarButtons.add(index, transferedButton);
					commandBarAlternateButtons.add(index, transferedButtonProperties.getSource() == COMMAND_BAR_BUTTONS_LIST_ID ?
																commandBarAlternateButtons.remove(transferedButtonProperties.getIndex()) :
																null);
					
					commandBarButtonsList.ensureIndexIsVisible(index);
				}

				public void dropActionChanged(DropTargetDragEvent dtde) { }
				
			});
		} catch (TooManyListenersException e) {
			// Should never happen
            AppLogger.fine("Caught exception", e);
		}
		
		return Arrays.asList(commandBarActionIds);
	}
	
	private Collection<String> initCommandBarAlternateActionsList() {
		String[] commandBarActionIds = CommandBarAttributes.getAlternateActions();
		int nbCommandBarActionIds = commandBarActionIds.length;
		for (int i=0; i<nbCommandBarActionIds; ++i)
			commandBarAlternateButtons.add(CommandBarButtonForDisplay.create(commandBarActionIds[i]));
		
		commandBarAlternateButtonsList = new DynamicList<JButton>(commandBarAlternateButtons);
		
		// Set lists cells renderer
		commandBarAlternateButtonsList.setCellRenderer(new CommandBarAlternativeButtonListRenderer());
		// Horizontal layout
		commandBarAlternateButtonsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		// All buttons appear in one row
		commandBarAlternateButtonsList.setVisibleRowCount(1);
		// Drag operations are supported
		commandBarAlternateButtonsList.setDragEnabled(true);
		// Can select only button at a time
		commandBarAlternateButtonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Set transfer handler
		commandBarAlternateButtonsList.setTransferHandler(new CommandBarAlternateActionsListTransferHandler());
		// Set list's background color
		commandBarAlternateButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);
		
		try {
			commandBarAlternateButtonsList.getDropTarget().addDropTargetListener((new DropTargetListener() {

				public void dragEnter(DropTargetDragEvent dtde) { }

				public void dragExit(DropTargetEvent dte) {
					selectedCommandBarAlternateButtonIndex = -2;
					commandBarAlternateButtonsList.repaint();
				}

				public void dragOver(DropTargetDragEvent dtde) {
					int index = canImport ? commandBarAlternateButtonsList.locationToIndex(dtde.getLocation()): -2;
					
					if (index != selectedCommandBarAlternateButtonIndex) {
						selectedCommandBarAlternateButtonIndex = index;
						commandBarAlternateButtonsList.repaint();
					}
				}

				public void drop(DropTargetDropEvent dtde) {
					if (!isImported)
						return;

					Point dropLocation = dtde.getLocation();
					int index = commandBarButtonsList.locationToIndex(dropLocation);
					
					Object button = commandBarAlternateButtons.remove(index);
					if (button != null)
						insertInOrder(commandBarAvailableButtons, (JButton) button);
					
					commandBarAlternateButtons.add(index, transferedButton);
					commandBarAlternateButtonsList.ensureIndexIsVisible(index);
				}

				public void dropActionChanged(DropTargetDragEvent dtde) { }
				
			}));
		} catch (TooManyListenersException e) {
			// Should never happen
            AppLogger.fine("Caught exception", e);
		}
		
		return Arrays.asList(commandBarActionIds);
	}
	
	private void initActionsPoolList(Set<String> usedActions) {
		Enumeration<String> actionIds = ActionManager.getActionIds();
		while(actionIds.hasMoreElements()) {
			String actionId = actionIds.nextElement();
            // Filter out actions that are currently used in the command bar, and those that are parameterized
			if (!usedActions.contains(actionId) && !ActionProperties.getActionDescriptor(actionId).isParameterized())
				insertInOrder(commandBarAvailableButtons, CommandBarButtonForDisplay.create(actionId));			
		}

		commandBarAvailableButtonsList = new DynamicHorizontalWrapList<JButton>(commandBarAvailableButtons, CommandBarButtonForDisplay.PREFERRED_SIZE.width, 6);
		
		commandBarAvailableButtonsList.setCellRenderer(new AvailableButtonCellListRenderer());
		commandBarAvailableButtonsList.setDragEnabled(true);
		commandBarAvailableButtonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commandBarAvailableButtonsList.setTransferHandler(new AvailableActionsListTransferHandler());
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
		panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));
		
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		YBoxPanel listsPanel = new YBoxPanel();
		listsPanel.add(commandBarButtonsList);
		listsPanel.add(commandBarAlternateButtonsList);

		JScrollPane scrollPane = new JScrollPane(listsPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		panel.add(scrollPane);

		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		panel.add(new JLabel("(" + Translator.get("command_bar_dialog.help") + ")"));
		
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		JPanel modifierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modifierField = new RecordingKeyStrokeTextField(MODIFIER_FIELD_MAX_LENGTH, CommandBarAttributes.getModifier()) {
			
			public void setText(String t) {
				super.setText(t);
				componentChanged();
			}
			
			public void keyPressed(KeyEvent e) {
				int pressedKeyCode = e.getKeyCode();
				// Accept modifier keys only
				if (pressedKeyCode == KeyEvent.VK_CONTROL || pressedKeyCode == KeyEvent.VK_ALT
						|| pressedKeyCode == KeyEvent.VK_META || pressedKeyCode == KeyEvent.VK_SHIFT)
					super.keyPressed(e);
			}
		};
		
		modifierPanel.add(new JLabel(Translator.get("command_bar_customize_dialog.modifier")));
		modifierPanel.add(modifierField);
		panel.add(modifierPanel);
		
		return panel;
	}
	
	private class CommandBarButtonListCellRenderer implements ListCellRenderer {
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			Border insertionIndicatingBorder;
			if (selectedCommandBarButtonSide == LEFT ) {
				if (index == selectedCommandBarButtonIndex)
					insertionIndicatingBorder = (BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 2, 0, 0, PAINTED_BORDER_COLOR),
							BorderFactory.createMatteBorder(0, 0, 0, 2, JBUTTON_BACKGROUND_COLOR)));
				else 
					insertionIndicatingBorder = (BorderFactory.createMatteBorder(0, 2, 0, 2, JBUTTON_BACKGROUND_COLOR));
			}
			else { // side == RIGHT
				if (index == selectedCommandBarButtonIndex + 1)
					insertionIndicatingBorder = (BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 2, 0, 0, PAINTED_BORDER_COLOR),
							BorderFactory.createMatteBorder(0, 0, 0, 2, JBUTTON_BACKGROUND_COLOR)));
				else if (index == selectedCommandBarButtonIndex && selectedCommandBarButtonIndex == commandBarButtons.size() - 1)
					insertionIndicatingBorder = (BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 2, 0, 0, JBUTTON_BACKGROUND_COLOR),
							BorderFactory.createMatteBorder(0, 0, 0, 2, PAINTED_BORDER_COLOR)));
				else 
					insertionIndicatingBorder = (BorderFactory.createMatteBorder(0, 2, 0, 2, JBUTTON_BACKGROUND_COLOR));
			}

			if (value == null) {
				Box.Filler filler = createBoxFiller();
				filler.setBorder(insertionIndicatingBorder);
				return filler;
			}
			else {
				CommandBarButtonForDisplay button = (CommandBarButtonForDisplay) value;

                // Note: we wrap the button inside a panel and decorate the panel's border. The reason for decorating
                // the panel and not directly the button is because JButton stops rendering correctly under Mac OS X if
                // the button's border is changed.
                JPanel panel = new JPanel(new BorderLayout());

                panel.add(button, BorderLayout.CENTER);
				panel.setBorder(BorderFactory.createCompoundBorder(insertionIndicatingBorder, JBUTTON_BORDER));

                return panel;
            }
        }
	}

	private class CommandBarAlternativeButtonListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			if (value == null) {
				Box.Filler filler = createBoxFiller();
				filler.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, index == selectedCommandBarAlternateButtonIndex ? PAINTED_BORDER_COLOR : JBUTTON_BACKGROUND_COLOR ));
				return filler;
			}
			else {
                CommandBarButtonForDisplay button = (CommandBarButtonForDisplay) value;

                // Note: we wrap the button inside a panel and decorate the panel's border. The reason for decorating
                // the panel and not directly the button is because JButton stops rendering correctly under Mac OS X if
                // the button's border is changed.
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(button, BorderLayout.CENTER);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, index == selectedCommandBarAlternateButtonIndex ? PAINTED_BORDER_COLOR : JBUTTON_BACKGROUND_COLOR),
                        JBUTTON_BORDER));

                return panel;
            }
        }
	}

	private static class AvailableButtonCellListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			JPanel panel = new JPanel(new BorderLayout());
			CommandBarButtonForDisplay button = (CommandBarButtonForDisplay) value;
			panel.add(button, BorderLayout.CENTER);
			panel.setToolTipText(button.getToolTipText());
			return panel;
        }
	}	

	private static class DataIndexAndSource {
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
		private DataFlavor[] transferDataFlavors;
		
		public TransferableListData(int index, JComponent selectedValue, int source) {
			try {
				transferDataFlavors = selectedValue == null ?
						new DataFlavor[0] :
						new DataFlavor[]{new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + selectedValue.getClass().getName() + "\"")};
			} catch (ClassNotFoundException e) {
                AppLogger.fine("Caught exception", e);
			}
			
			data = new DataIndexAndSource(index, source);
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException { return data; }

		public DataFlavor[] getTransferDataFlavors() { return transferDataFlavors; }

		public boolean isDataFlavorSupported(DataFlavor flavor) { return transferDataFlavors.length > 0 && transferDataFlavors[0].equals(flavor); }
	}

	
	private abstract class ActionListTransferHandler extends TransferHandler {

		private DataFlavor flavor;
		
		public ActionListTransferHandler() {
			try {
				flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
				           ";class=\"" + CommandBarButtonForDisplay.class.getName() + "\"");
			} catch (ClassNotFoundException e) {
                AppLogger.fine("Caught exception", e);
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
			if (component == null)
				return null;
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
	
	private class CommandBarActionsListTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
                AppLogger.fine("Caught exception", e);
				return isImported = false;
			}

			int source = transferedButtonProperties.getSource();
			if (source == AVAILABLE_BUTTONS_LIST_ID) {
				transferedButton = commandBarAvailableButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_BUTTONS_LIST_ID) {
				transferedButton = commandBarButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = commandBarAlternateButtons.remove(transferedIndex);
				
				commandBarAlternateButtons.add(transferedIndex, null);
			}
			
			return isImported = true;
		}

		protected int getListId() { return COMMAND_BAR_BUTTONS_LIST_ID; }
	}
	
	private class CommandBarAlternateActionsListTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
                AppLogger.fine("Caught exception", e);
				return isImported = false;
			}
			
			int source = transferedButtonProperties.getSource();
			if (source == AVAILABLE_BUTTONS_LIST_ID) {
				transferedButton = commandBarAvailableButtons.remove(transferedButtonProperties.getIndex());
			}
			else if (source == COMMAND_BAR_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = commandBarButtons.remove(transferedIndex);
				
				// alternative button:
				Object button = commandBarAlternateButtons.remove(transferedIndex);
				if (button != null)
					insertInOrder(commandBarAvailableButtons, (JButton) button);
			}
			else if (source == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
				int transferedIndex = transferedButtonProperties.getIndex();
				transferedButton = commandBarAlternateButtons.remove(transferedIndex);
				
				commandBarAlternateButtons.add(transferedIndex, null);
			}
			
			return isImported = true;
		}

		protected int getListId() {
			return COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID;
		}
	}
	
	private class AvailableActionsListTransferHandler extends ActionListTransferHandler {
		
		public boolean importData(JComponent comp, Transferable t) {
			
			try {
				transferedButtonProperties = (DataIndexAndSource) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" + DataIndexAndSource.class.getName() + "\""));
			}
			catch (Exception e) {
                AppLogger.fine("Caught exception", e);
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
					JButton button = commandBarButtons.remove(transferedIndex);
					insertionPlace = insertInOrder(commandBarAvailableButtons, button);
					
					// alternative button:
					Object alternativeButton = commandBarAlternateButtons.remove(transferedIndex);
					if (alternativeButton != null)
						insertInOrder(commandBarAvailableButtons, (JButton) alternativeButton);
				}
				else if (transferedButtonProperties.getSource() == COMMAND_BAR_ALTERNATIVE_BUTTONS_LIST_ID) {
					Object alternativeButton = commandBarAlternateButtons.remove(transferedIndex);
					if (alternativeButton instanceof JButton) {
						insertionPlace = insertInOrder(commandBarAvailableButtons, (JButton) alternativeButton);
						
						commandBarAlternateButtons.add(transferedIndex, null);
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
	
	private static Filler createBoxFiller() {
		Box.Filler filler = (Filler) Box.createHorizontalGlue();
		filler.setPreferredSize(CommandBarButtonForDisplay.PREFERRED_SIZE);		
		return filler;
	}
	
	private static int insertInOrder(Vector<JButton> vector, JButton element) {
		Comparator<JButton> comparator = new Comparator<JButton>() {
			public int compare(JButton b1, JButton b2) {
				// TODO: remove actions without a standard label?
				if (b1.getText() == null)
					return 1;
				if (b2.getText() == null)
					return -1;
				return b1.getText().compareTo(b2.getText());
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
	
	private static int findPlace(Vector<JButton> vector, JButton element, Comparator<JButton> comparator, int first, int last) {
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
