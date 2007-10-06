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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 'General' preferences panel.
 *
 * @author Maxence Bernard
 */
class GeneralPanel extends PreferencesPanel implements ItemListener, ActionListener, DocumentListener {

    // Language
    private String languages[];
    private JComboBox languageComboBox;
	
    // Date/time format
    private JRadioButton time12RadioButton;
    private JRadioButton time24RadioButton;
    private JComboBox dateFormatComboBox;
    private JTextField dateSeparatorField;
    private JCheckBox showSecondsCheckBox;
    private JCheckBox showCenturyCheckBox;
    private JLabel previewLabel;
    private Date exampleDate;

    private final static String DAY = Translator.get("prefs_dialog.day");
    private final static String MONTH = Translator.get("prefs_dialog.month");
    private final static String YEAR = Translator.get("prefs_dialog.year");


    private final static String DATE_FORMAT_LABELS[] = {
        MONTH+"/"+DAY+"/"+YEAR,
        DAY+"/"+MONTH+"/"+YEAR,
        YEAR+"/"+MONTH+"/"+DAY,
        MONTH+"/"+YEAR+"/"+DAY,
        DAY+"/"+YEAR+"/"+MONTH,
        YEAR+"/"+DAY+"/"+MONTH
    };

    private final static String DATE_FORMATS[] = {
        "MM/dd/yy",
        "dd/MM/yy",
        "yy/MM/dd",
        "MM/yy/dd",
        "dd/yy/MM",
        "yy/dd/MM"
    };

    private final static String DATE_FORMATS_WITH_CENTURY[] = {
        "MM/dd/yyyy",
        "dd/MM/yyyy",
        "yyyy/MM/dd",
        "MM/yyyy/dd",
        "dd/yyyy/MM",
        "yyyy/dd/MM"
    };

    private final static String HOUR_12_TIME_FORMAT = "hh:mm a";
    private final static String HOUR_12_TIME_FORMAT_WITH_SECONDS = "hh:mm:ss a";
    private final static String HOUR_24_TIME_FORMAT = "HH:mm";
    private final static String HOUR_24_TIME_FORMAT_WITH_SECONDS = "HH:mm:ss";

	
    public GeneralPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.general_tab"));

        setLayout(new BorderLayout());
        
        YBoxPanel mainPanel = new YBoxPanel();
        JPanel tempPanel;

        // Language
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        languagePanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.language")));
        this.languages = Translator.getAvailableLanguages();
        String currentLang = MuConfiguration.getVariable(MuConfiguration.LANGUAGE);
        String lang;
        languageComboBox = new JComboBox();

        // Use a custom combo box renderer to display language icons 
        class LanguageComboBoxRenderer extends BasicComboBoxRenderer {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                String language = (String)value;
                label.setText(Translator.get("language."+language));
                label.setIcon(IconManager.getIcon(IconManager.LANGUAGE_ICON_SET, language+".png"));

                return label;
            }
        }
        languageComboBox.setRenderer(new LanguageComboBoxRenderer());
		
        // Default language index
        int languageIndex = 0;
        for(int i=0; i<languages.length; i++) {
            if(languages[i].equalsIgnoreCase("en")) {
                languageIndex = i;
                break;
            }
        }
        // Add combo items and select current language
        for(int i=0; i<languages.length; i++) {
            lang = languages[i];
//            languageComboBox.addItem(Translator.get("language."+lang));
            languageComboBox.addItem(lang);
            if(lang.equalsIgnoreCase(currentLang))
                languageIndex = i;
        }
        languageComboBox.setSelectedIndex(languageIndex);
        languagePanel.add(languageComboBox);
        mainPanel.add(languagePanel);
        mainPanel.addSpace(10);
		
        // Date & time format panel
        YBoxPanel dateTimeFormatPanel = new YBoxPanel();
        dateTimeFormatPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.date_time")));

        JPanel gridPanel = new JPanel(new GridLayout(1, 2));

        YBoxPanel dateFormatPanel = new YBoxPanel();
        dateFormatPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.date")));

        // Date format combo
        dateFormatComboBox = new JComboBox();
        String dateFormat = MuConfiguration.getVariable(MuConfiguration.DATE_FORMAT);
        String separator = MuConfiguration.getVariable(MuConfiguration.DATE_SEPARATOR, MuConfiguration.DEFAULT_DATE_SEPARATOR);
        int dateFormatIndex = 0;
        String buffer = dateFormat.replace(separator.charAt(0), '/');
        for(int i=0; i<DATE_FORMATS.length; i++) {
            dateFormatComboBox.addItem(DATE_FORMAT_LABELS[i]);
            if(buffer.equals(DATE_FORMATS[i]) || buffer.equals(DATE_FORMATS_WITH_CENTURY[i]))
                dateFormatIndex = i;
        }        
        dateFormatComboBox.setSelectedIndex(dateFormatIndex);
        dateFormatComboBox.addItemListener(this);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(dateFormatComboBox);
        tempPanel.add(Box.createHorizontalGlue());
        dateFormatPanel.add(tempPanel);

        // Date separator field
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("prefs_dialog.date_separator")+": "));
        dateSeparatorField = new JTextField(1);
        // Limit the number of characters in the text field to 1 and enforces only non-alphanumerical characters
        PlainDocument doc = new PlainDocument() {
                public void insertString(int param, String str, javax.swing.text.AttributeSet attributeSet) throws javax.swing.text.BadLocationException {
                    // Limit field to 1 character max
                    if (str != null && this.getLength() + str.length() > 1)
                        return;
				
                    // Reject letters and digits, as they don't make much sense,
                    // plus letters would be misinterpreted by SimpleDateFormat
                    if (str != null) {
                        int len = str.length();
                        for(int i=0; i<len; i++)
                            if(Character.isLetterOrDigit(str.charAt(i)))
                                return;
                    }
					

                    super.insertString(param, str, attributeSet);
                }
            };
        dateSeparatorField.setDocument(doc);
        dateSeparatorField.setText(separator);
        doc.addDocumentListener(this);
        tempPanel.add(dateSeparatorField);
        tempPanel.add(Box.createHorizontalGlue());
        dateFormatPanel.add(tempPanel);

        showCenturyCheckBox = new JCheckBox(Translator.get("prefs_dialog.show_century"));
        showCenturyCheckBox.setSelected(dateFormat.indexOf("yyyy")!=-1);
        showCenturyCheckBox.addItemListener(this);
        dateFormatPanel.add(showCenturyCheckBox);
        dateFormatPanel.addSpace(10);
        gridPanel.add(dateFormatPanel);

        // Time format
        YBoxPanel timeFormatPanel = new YBoxPanel();
        timeFormatPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.time")));

        time12RadioButton = new JRadioButton(Translator.get("prefs_dialog.time_12_hour"));
        time12RadioButton.addActionListener(this);
        time24RadioButton = new JRadioButton(Translator.get("prefs_dialog.time_24_hour"));
        time24RadioButton.addActionListener(this);
        
        String timeFormat = MuConfiguration.getVariable(MuConfiguration.TIME_FORMAT);
        if(timeFormat.equals(HOUR_12_TIME_FORMAT) || timeFormat.equals(HOUR_12_TIME_FORMAT_WITH_SECONDS))
            time12RadioButton.setSelected(true);
        else
            time24RadioButton.setSelected(true);
            
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(time12RadioButton);
        buttonGroup.add(time24RadioButton);

        timeFormatPanel.add(time12RadioButton);
        timeFormatPanel.add(time24RadioButton);
        timeFormatPanel.addSpace(10);

        showSecondsCheckBox = new JCheckBox(Translator.get("prefs_dialog.show_seconds"));
        showSecondsCheckBox.setSelected(timeFormat.indexOf(":ss")!=-1);
        showSecondsCheckBox.addItemListener(this);
        timeFormatPanel.add(showSecondsCheckBox);
        timeFormatPanel.addSpace(10);
        gridPanel.add(timeFormatPanel);

        dateTimeFormatPanel.add(gridPanel);

        // Date/time preview
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("example")+": "));
        previewLabel = new JLabel();
        Calendar calendar = Calendar.getInstance(); 
        calendar.set(calendar.get(Calendar.YEAR)-1, 11, 31, 23, 59);
        exampleDate = calendar.getTime();
        updatePreviewLabel();
        tempPanel.add(previewLabel);

        dateTimeFormatPanel.add(tempPanel);
      
        mainPanel.add(dateTimeFormatPanel);

        add(mainPanel, BorderLayout.NORTH);
    }

    private String getTimeFormatString() {
        boolean showSeconds = showSecondsCheckBox.isSelected();

        if(time12RadioButton.isSelected())
            return showSeconds?HOUR_12_TIME_FORMAT_WITH_SECONDS:HOUR_12_TIME_FORMAT;
        else
            return showSeconds?HOUR_24_TIME_FORMAT_WITH_SECONDS:HOUR_24_TIME_FORMAT;
    }

    private String getDateFormatString() {
        int selectedIndex = dateFormatComboBox.getSelectedIndex();
        return CustomDateFormat.replaceDateSeparator(showCenturyCheckBox.isSelected()?DATE_FORMATS_WITH_CENTURY[selectedIndex]:DATE_FORMATS[selectedIndex], dateSeparatorField.getText());
    }

    private void updatePreviewLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                                           getDateFormatString()
                                                           +" "+getTimeFormatString());
        previewLabel.setText(dateFormat.format(exampleDate));
        previewLabel.repaint();
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    protected void commit() {
        MuConfiguration.setVariable(MuConfiguration.LANGUAGE, languages[languageComboBox.getSelectedIndex()]);
        MuConfiguration.setVariable(MuConfiguration.DATE_FORMAT, getDateFormatString());
        MuConfiguration.setVariable(MuConfiguration.DATE_SEPARATOR, dateSeparatorField.getText());
        MuConfiguration.setVariable(MuConfiguration.TIME_FORMAT, getTimeFormatString());
    }


    //////////////////////////
    // ItemListener methods //
    //////////////////////////
	
    public void itemStateChanged(ItemEvent e) {
        updatePreviewLabel();
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        updatePreviewLabel();
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        updatePreviewLabel();
    }
	
    public void insertUpdate(DocumentEvent e) {
        updatePreviewLabel();
    }
	
    public void removeUpdate(DocumentEvent e) {
        updatePreviewLabel();
    }
}
