/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.layout;

import javax.swing.*;
import java.awt.*;

/**
 * InformationPane is a panel which is suitable for use in dialogs, to give information about the action that the
 * dialog is about to perform.<br>
 * It is made of 3 components:
 * <ul>
 *  <li>A 'main' label which provides a brief description of the action to be performed.
 * By default, this label uses the standard label's font size and a bold style.
 *  <li>A 'caption' label which is displayed under the main label and provides additional information about the action
 * to be performed. This label uses a smaller font size and plain style.
 *  <li>An optional icon displayed to the left of labels. InformationPane makes it easy to use one of the standard
 * <code>JOptionPane</code> icons: error, information, warning, question.
 * </ul>
 *
 * <p>Here is a textual representation of the layout, all components are vertically aligned to the top:
 * <pre>
 *
 * --------------------------
 * | [ICON] | Main label    |
 * |        | Caption label |
 * |________|_______________|
 * 
 * </pre>
 * </p>
 *
 * @author Maxence Bernard
 */
public class InformationPane extends JPanel {

    /** Label used to display the icon, can be null if no icon is used */
    private JLabel iconLabel;
    /** Label used to display the main message */
    private JLabel mainLabel;
    /** Label used to display the caption message */
    private JLabel captionLabel;

    /** Designated the 'error' predefined icon */
    public final static int ERROR_ICON = 1;
    /** Designated the 'information' predefined icon */
    public final static int INFORMATION_ICON = 2;
    /** Designated the 'warning' predefined icon */
    public final static int WARNING_ICON = 3;
    /** Designated the 'question' predefined icon */
    public final static int QUESTION_ICON = 4;

    /**
     * Creates a new InformationPane with no main and caption message.
     */
    public InformationPane() {
        this("", "");
    }

    /**
     * Creates a new InformationPane using the given main and caption messages and no icon. The font style for the main
     * label is set to <code>Font.BOLD</code>.
     *
     * @param mainMessage the message to display in the main label
     * @param captionMessage the message to display in the caption label
     */
    public InformationPane(String mainMessage, String captionMessage) {
        this(mainMessage, captionMessage, Font.BOLD, null);
    }

    /**
     * Creates a new InformationPane using the given main message, caption message, main label font style and
     * predefined icon.
     *
     * @param mainMessage the message to display in the main label
     * @param captionMessage the message to display in the caption label
     * @param mainMessageFontStyle the font style to use in the main label
     * @param predefinedIconId an id designating the predefined icon to display, see constant fields for allowed values
     */
    public InformationPane(String mainMessage, String captionMessage, int mainMessageFontStyle, int predefinedIconId) {
        this(mainMessage, captionMessage, mainMessageFontStyle, getPredefinedIcon(predefinedIconId));
    }

    /**
     * Creates a new InformationPane using the given main message, caption message, main label font style and icon.
     *
     * @param mainMessage the message to display in the main label
     * @param captionMessage the message to display in the caption label
     * @param mainMessageFontStyle the font style to use in the main label
     * @param icon an icon to display, null for no icon
     */
    public InformationPane(String mainMessage, String captionMessage, int mainMessageFontStyle, Icon icon) {
        super(new BorderLayout());

        if(icon!=null)
            setIcon(icon);

        YBoxPanel yPanel = new YBoxPanel();

        mainLabel = new JLabel(mainMessage);
        if(mainMessageFontStyle!=Font.PLAIN)
            setMainLabelFontStyle(mainMessageFontStyle);
        yPanel.add(mainLabel);

        yPanel.addSpace(5);

        captionLabel = new JLabel(captionMessage);
        Font labelFont = mainLabel.getFont();
        captionLabel.setFont(labelFont.deriveFont(Font.PLAIN, labelFont.getSize()-2));
        yPanel.add(captionLabel);

        add(yPanel, BorderLayout.CENTER);
    }

    /**
     * Returns an <code>Icon</code> instance corresponding to the given predefined icon id.
     *
     * @param predefinedIconId an id designating a predefined icon, see constant fields for allowed values
     * @return an Icon instance corresponding to the given predefined icon id 
     */
    public static Icon getPredefinedIcon(int predefinedIconId) {
        String optionPaneIcon;

        switch(predefinedIconId) {
            case ERROR_ICON:
                optionPaneIcon = "errorIcon";
                break;

            case INFORMATION_ICON:
                optionPaneIcon = "informationIcon";
                break;

            case WARNING_ICON:
                optionPaneIcon = "warningIcon";
                break;

            case QUESTION_ICON:
                optionPaneIcon = "questionIcon";
                break;

            default:
                return null;
        }

        return UIManager.getIcon("OptionPane."+optionPaneIcon);
    }

    /**
     * Returns the main label displayed in this InformationPane.
     *
     * @return the main label displayed in this InformationPane
     */
    public JLabel getMainLabel() {
        return mainLabel;
    }

    /**
     * Returns the caption label displayed in this InformationPane.
     *
     * @return the caption label displayed in this InformationPane
     */
    public JLabel getCaptionLabel() {
        return captionLabel;
    }

    /**
     * Changes the icon displayed in this InformationPane to the given one. If <code>null</code> is specified, this
     * InformationPane will not display any icon.
     *
     * @param icon the new icon to display, null for no icon
     */
    public void setIcon(Icon icon) {
        if(icon==null) {
            if(iconLabel!=null) {
                remove(iconLabel);
                iconLabel = null;
            }
        }
        else {
            if(iconLabel==null) {
                iconLabel = new JLabel(" ");
                add(iconLabel, BorderLayout.WEST);
            }

            iconLabel.setIcon(icon);
        }
    }

    /**
     * Changes the font style used by the main label. Allowed values are <code>Font.PLAIN</code>,
     * <code>Font.BOLD</code> and <code>Font.ITALIC</code> or a mix of those combined as a bitwise mask.
     *
     * @param fontStyle the new font style for the main label
     */
    public void setMainLabelFontStyle(int fontStyle) {
        Font labelFont = mainLabel.getFont();
        mainLabel.setFont(labelFont.deriveFont(fontStyle, labelFont.getSize()));
    }

    /**
     * Returns the font style used by the main label as a bitwise mask.
     *
     * @return the font style used by the main label as a bitwise mask
     */
    public int getMainLabelFontStyle() {
        return mainLabel.getFont().getStyle();
    }

}
