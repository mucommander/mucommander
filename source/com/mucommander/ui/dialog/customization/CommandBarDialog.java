/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

/**
 * Dialog used to customize the command-bar.
 * 
 * @author Arik Hadas
 */
public class CommandBarDialog extends CustomizeDialog {
	
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
	
	/** Modifier text field length  */
	private static final int MODIFIER_FIELD_MAX_LENGTH = 6;
	
	/** The default color of button's border */
	private static final Color JBUTTON_BACKGROUND_COLOR = UIManager.getColor("button.background");
	
	/** Comparator for buttons according to their text */
	private static final Comparator<JButton> BUTTONS_COMPARATOR = new Comparator<JButton>() {
		public int compare(JButton b1, JButton b2) {
			if (b1.getText() == null)
				return 1;
			if (b2.getText() == null)
				return -1;
			return b1.getText().compareTo(b2.getText());
		}
	};
	
	/**
	 * Constructor
	 */
    public CommandBarDialog(MainFrame mainFrame) {
		super(mainFrame, ActionProperties.getActionLabel(CustomizeCommandBarAction.Descriptor.ACTION_ID));
	}
	
    @Override
    protected void componentChanged() {
    	setCommitButtonsEnabled(getNumberOfButtons() > 0 && (areActionsChanged() || areAlternativeActionsChanged() || isModifierChanged()));    		
    }
    
    @Override
    protected void commit() {
		int nbNewActions = getNumberOfButtons();
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

	@Override
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
    
    private boolean areActionsChanged() {
    	// Fetch command-bar action ids
    	String[] commandBarActionIds = CommandBarAttributes.getActions();
    	int nbActions = commandBarActionIds.length;
    	
    	if (nbActions != getNumberOfButtons())
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
		// Set transfer handler
		commandBarButtonsList.setTransferHandler(new TransferHandler(){
			
			@Override
			public int getSourceActions(JComponent c) {
				return MOVE;
			}
			
			@Override
			public Transferable createTransferable(JComponent c) {
				if (c instanceof JList) {
					JList list = (JList) c;
					return new TransferableButton((JButton) list.getSelectedValue());
				}
				return null;
			}
			
			@Override
			public void exportDone(JComponent c, Transferable t, int action) {
				if (action == TransferHandler.MOVE) {
					JList list = (JList) c;
					removeCommandBarButtonAtIndex(list.getSelectedIndex());
					componentChanged();
				}
			}
			
			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				return support.isDataFlavorSupported(TransferableButton.buttonFlavor);
			}
			
			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support))
					return false;
				try {
					Point dropLocation = support.getDropLocation().getDropPoint();
					JButton button = (JButton) support.getTransferable().getTransferData(TransferableButton.buttonFlavor);
					int index = addCommandBarButtonAtLocation(dropLocation, button);
					commandBarButtonsList.ensureIndexIsVisible(index);
					commandBarButtonsList.repaint();
					return true;
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		
		// Set list's background color
		commandBarButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);

		commandBarButtonsList.setDropMode(DropMode.INSERT);
		
		return Arrays.asList(commandBarActionIds);
	}
	
	private void removeCommandBarButtonAtIndex(int index) {
		commandBarButtons.remove(index);
		if (commandBarButtons.size() == 0) {
			commandBarButtons.add(null);
			commandBarButtonsList.setDropMode(DropMode.ON);
		}
		JButton alternateButtonAtIndex = commandBarAlternateButtons.remove(index);
		if (alternateButtonAtIndex != null)
			insertInOrder(commandBarAvailableButtons, alternateButtonAtIndex);
	}
	
	private int getNumberOfButtons() {
		int commandBarButtonsSize = commandBarButtons.size();
		return commandBarButtonsSize == 1 && commandBarButtons.get(0) == null ? 0 : commandBarButtonsSize;
	}
	
	private int addCommandBarButtonAtLocation(Point dropLocation, JButton button) {
		int index;
		if (getNumberOfButtons() == 0) {
			index = 0;
			commandBarButtons.set(index, button);
			commandBarAlternateButtons.add(index, null);
			commandBarButtonsList.setDropMode(DropMode.INSERT);
		}
		else {
			index = commandBarButtonsList.locationToIndex(dropLocation);
			index += dropLocation.x > commandBarButtonsList.indexToLocation(index).x + CommandBarButtonForDisplay.PREFERRED_SIZE.width/2 ? 1 : 0;
			commandBarButtons.add(index, button);
			commandBarAlternateButtons.add(index, null);
		}
		return index;
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
		// Set transfer handler
		commandBarAlternateButtonsList.setTransferHandler(new TransferHandler() {
		
			@Override
			public int getSourceActions(JComponent c) {
				return MOVE;
			}
			
			@Override
			public Transferable createTransferable(JComponent c) {
				if (c instanceof JList) {
					JList list = (JList) c;
					return new TransferableButton((JButton) list.getSelectedValue());
				}
				return null;
			}
			
			@Override
			public void exportDone(JComponent c, Transferable t, int action) {
				if (action == TransferHandler.MOVE) {
					JList list = (JList) c;
					commandBarAlternateButtons.set(list.getSelectedIndex(), null);
					componentChanged();
				}
			}
			
			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				return support.isDataFlavorSupported(TransferableButton.buttonFlavor);
			}
			
			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support))
					return false;
				try {
					Point dropLocation = support.getDropLocation().getDropPoint();
					int index = commandBarButtonsList.locationToIndex(dropLocation);
					JButton prevButton = commandBarAlternateButtons.get(index);
					if (prevButton != null)
						insertInOrder(commandBarAvailableButtons, prevButton);

					commandBarAlternateButtons.set(index, (JButton) support.getTransferable().getTransferData(TransferableButton.buttonFlavor));
					commandBarAlternateButtonsList.ensureIndexIsVisible(index);
					commandBarAlternateButtonsList.repaint();
					return true;
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		
		// Set list's background color
		commandBarAlternateButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);
		
		commandBarAlternateButtonsList.setDropMode(DropMode.ON);
		
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
		commandBarAvailableButtonsList.setTransferHandler(new TransferHandler() {
		
			@Override
			public int getSourceActions(JComponent c) {
				return MOVE;
			}
			
			@Override
			public Transferable createTransferable(JComponent c) {
				if (c instanceof JList)
					return new TransferableButton((JButton) ((JList) c).getSelectedValue());
				return null;
			}
			
			@Override
			public void exportDone(JComponent c, Transferable t, int action) {
				if (action == TransferHandler.MOVE) {
					if (c instanceof JList)
						commandBarAvailableButtons.remove(((JList) c).getSelectedValue());
					componentChanged();
				}
			}
			
			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				if (!support.isDataFlavorSupported(TransferableButton.buttonFlavor))
					return false;
				return true;
			}
			
			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support))
					return false;
				try {
					int insertedIndex = insertInOrder(commandBarAvailableButtons, (JButton) support.getTransferable().getTransferData(TransferableButton.buttonFlavor));
					commandBarAvailableButtonsList.ensureIndexIsVisible(insertedIndex);
					return true;
				}
				catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		
		commandBarAvailableButtonsList.setBackground(JBUTTON_BACKGROUND_COLOR);
		
		commandBarAvailableButtonsList.setDropMode(DropMode.ON);
	}
	
	protected JPanel createAvailableButtonsPanel() {
		JPanel panel = new JPanel(new GridLayout(1,0));
		panel.setBorder(BorderFactory.createTitledBorder(Translator.get("command_bar_customize_dialog.available_actions")));
		
		JScrollPane scrollPane = new JScrollPane(commandBarAvailableButtonsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		panel.add(scrollPane);
		
		return panel;
	}
	
	private JPanel createCommandBarPanel() {
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
			
			@Override
            public void setText(String t) {
				super.setText(t);
				componentChanged();
			}
			
			@Override
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
	
	private static class TransferableButton implements Transferable {
    	public static DataFlavor buttonFlavor = new DataFlavor(CommandBarButtonForDisplay.class, null);
    	
    	private JButton button;
    	
    	public TransferableButton(JButton button) {
    		this.button = button;
    	}
    	
    	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    		return button;
    	}
    	
    	public DataFlavor[] getTransferDataFlavors() {
    		return new DataFlavor[] {buttonFlavor};
    	}
    
    	public boolean isDataFlavorSupported(DataFlavor flavor) {
    		return buttonFlavor.equals(flavor);
    	}
    }
	
	private static class CommandBarButtonListCellRenderer implements ListCellRenderer {
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			return value == null ? createBoxFiller() : (CommandBarButtonForDisplay) value;
        }
	}

	private static class CommandBarAlternativeButtonListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			return value == null ? createBoxFiller() : (CommandBarButtonForDisplay) value;
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

	//////////////////////////
	///// Helper methods /////
	//////////////////////////
	
	private static Filler createBoxFiller() {
		Box.Filler filler = (Filler) Box.createHorizontalGlue();
		filler.setPreferredSize(CommandBarButtonForDisplay.PREFERRED_SIZE);		
		return filler;
	}
	
	private static int insertInOrder(Vector<JButton> vector, JButton element) {
		if (vector.size() != 0) {
			int index = findPlace(vector, element, BUTTONS_COMPARATOR, 0, vector.size() - 1);
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
