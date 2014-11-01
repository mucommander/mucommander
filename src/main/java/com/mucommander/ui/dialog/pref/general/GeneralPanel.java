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

package com.mucommander.ui.dialog.pref.general;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.PlainDocument;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefComboBox;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.dialog.pref.component.PrefTextField;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.YBoxPanel;


/**
 * 'General' preferences panel.
 *
 * @author Maxence Bernard
 */
class GeneralPanel extends PreferencesPanel implements ItemListener, ActionListener, DocumentListener {

    // Language
    private String languages[];
    private PrefComboBox languageComboBox;
	
    // Date/time format
    private PrefRadioButton time12RadioButton;
    private PrefComboBox dateFormatComboBox;
    private PrefTextField dateSeparatorField;
    private PrefCheckBox showSecondsCheckBox;
    private PrefCheckBox showCenturyCheckBox;
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
        String currentLang = MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE);
        String lang;
        languageComboBox = new PrefComboBox() {
			public boolean hasChanged() {
				return !languages[getSelectedIndex()].equals(MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE));
			}
        };

        // Use a custom combo box renderer to display language icons 
        class LanguageComboBoxRenderer extends BasicComboBoxRenderer {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                String language = (String)value;
                label.setText(Translator.get("language."+language));
                label.setIcon(IconManager.getIcon(IconManager.LANGUAGE_ICON_SET, language+".png"));

                return label;
            }
        }
        languageComboBox.setRenderer(new LanguageComboBoxRenderer());
		
        // Add combo items and select current language (defaults to EN if current language can't be found)
        int languageIndex = -1;
        for(int i=0; i<languages.length; i++) {
            lang = languages[i];
            languageComboBox.addItem(lang);

            if(lang.equalsIgnoreCase(currentLang))
                languageIndex = i;
            else if(languageIndex==-1 && lang.equalsIgnoreCase("en"))
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
        dateFormatComboBox = new PrefComboBox() {
			public boolean hasChanged() {
				return !getDateFormatString().equals(MuConfigurations.getPreferences().getVariable(MuPreference.DATE_FORMAT));
			}
        };
        String dateFormat = MuConfigurations.getPreferences().getVariable(MuPreference.DATE_FORMAT);
        String separator = MuConfigurations.getPreferences().getVariable(MuPreference.DATE_SEPARATOR, MuPreferences.DEFAULT_DATE_SEPARATOR);
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
        dateSeparatorField = new PrefTextField(1) {
			public boolean hasChanged() {
				return !getText().equals(MuConfigurations.getPreferences().getVariable(MuPreference.DATE_SEPARATOR));
			}
        };
        // Limit the number of characters in the text field to 1 and enforces only non-alphanumerical characters
        PlainDocument doc = new PlainDocument() {
                @Override
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

        showCenturyCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_century")) {
			public boolean hasChanged() {
				return isSelected() != (MuConfigurations.getPreferences().getVariable(MuPreference.DATE_FORMAT).indexOf("yyyy")!=-1);
			}
        };
        showCenturyCheckBox.setSelected(dateFormat.indexOf("yyyy")!=-1);
        showCenturyCheckBox.addItemListener(this);
        dateFormatPanel.add(showCenturyCheckBox);
        dateFormatPanel.addSpace(10);
        gridPanel.add(dateFormatPanel);

        // Time format
        YBoxPanel timeFormatPanel = new YBoxPanel();
        timeFormatPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.time")));

        time12RadioButton = new PrefRadioButton(Translator.get("prefs_dialog.time_12_hour")) {
			public boolean hasChanged() {
				String timeFormat = MuConfigurations.getPreferences().getVariable(MuPreference.TIME_FORMAT);
		        return isSelected() != (timeFormat.equals(HOUR_12_TIME_FORMAT) || timeFormat.equals(HOUR_12_TIME_FORMAT_WITH_SECONDS)); 
			}
        };
        time12RadioButton.addActionListener(this);
        PrefRadioButton time24RadioButton = new PrefRadioButton(Translator.get("prefs_dialog.time_24_hour")) {
			public boolean hasChanged() {
				String timeFormat = MuConfigurations.getPreferences().getVariable(MuPreference.TIME_FORMAT);
		        return isSelected() != (timeFormat.equals(HOUR_24_TIME_FORMAT) || timeFormat.equals(HOUR_24_TIME_FORMAT_WITH_SECONDS));
			}
        };
        time24RadioButton.addActionListener(this);
        
        String timeFormat = MuConfigurations.getPreferences().getVariable(MuPreference.TIME_FORMAT);
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

        showSecondsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_seconds")) {
			public boolean hasChanged() {
				return isSelected() != (MuConfigurations.getPreferences().getVariable(MuPreference.TIME_FORMAT).indexOf(":ss")!=-1);
			}
        };
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
        
        languageComboBox.addDialogListener(parent);
        time12RadioButton.addDialogListener(parent);
        time24RadioButton.addDialogListener(parent);
        dateFormatComboBox.addDialogListener(parent);
        dateSeparatorField.addDialogListener(parent);
        showSecondsCheckBox.addDialogListener(parent);
        showCenturyCheckBox.addDialogListener(parent);
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
    @Override
    protected void commit() {
    	MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, languages[languageComboBox.getSelectedIndex()]);
    	MuConfigurations.getPreferences().setVariable(MuPreference.DATE_FORMAT, getDateFormatString());
    	MuConfigurations.getPreferences().setVariable(MuPreference.DATE_SEPARATOR, dateSeparatorField.getText());
    	MuConfigurations.getPreferences().setVariable(MuPreference.TIME_FORMAT, getTimeFormatString());
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
