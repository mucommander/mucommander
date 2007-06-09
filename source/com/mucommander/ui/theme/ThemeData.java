 package com.mucommander.ui.theme;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * Base class for all things Theme.
 * <p>
 * The role of <code>ThemeData</code> is twofold:<br/>
 * - theme data storage.<br/>
 * - default values retrievals and notification.<br/>
 * </p>
 * <p>
 * In the current version, theme data is solely composed of assorted colors and fonts. <code>ThemeData</code>
 * offers methods to {@link #setColor(int,Color) set}, {@link #getColor(int) retrieve}, {@link #isIdentical(ThemeData,boolean) compare}
 * and {@link #cloneData() clone} these values.
 * </p>
 * <p>
 * One of its major constraints is that it can <b>never</b> return <code>null</code> values for the items it contains. Whenever a specific
 * value hasn't been set, <code>ThemeData</code> will seemlessly provide the rest of the world with default values retrieved from the current
 * look&amp;feel.
 * </p>
 * <p>
 * This default values system means that theme items can change outside of anybody's control: Swing UI properties can be updated, the current
 * look&amp;feel can be modified to a new one... <code>ThemeData</code> will track this changes and make sure that the proper event are dispatched
 * to listeners.
 * </p>
 * <p>
 * In theory, classes that use the theme API should not need to worry about default value modifications. This is already managed internally, and
 * if the change affects any of the themes being listened on, the event will be propagated to them. There might special cases where it's necessary,
 * however, for which <code>ThemeData</code> provides a {@link #addDefaultValuesListener(ThemeListener) listening} mechanism.
 * </p>
 * @see Theme
 * @see ThemeManager
 * @see javax.swing.UIManager
 * @author Nicolas Rinaudo
 */
public class ThemeData {
    // - Dirty hack ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // This is an effort to make the ThemeData class a bit easier to maintain, but I'm the first
    // to admit it's rather dirty.
    // 
    // For optimisation reasons, we're storing the fonts and colors in arrays, using their
    // identifiers as indexes in the array. This, however, means that lots of bits of code
    // must be updated whenever a font or color is added or removed. The probability of
    // someone forgeting this is, well, 100%.
    //
    // For this reason, we've declared the number of font and colors as constants.
    // People are still going to forget to update these constants, but at least it'll be
    // a lot easier to fix.

    /**
     * Number of known fonts.
     * <p>
     * Since font identifiers are contiguous, it is possible to explore all fonts contained
     * by an instance of theme data by looping from 0 to {@link #FONT_COUNT}.
     * </p>
     */
    public static final int FONT_COUNT  = 6;

    /**
     * Number of known colors.
     * <p>
     * Since color identifiers are contiguous, it is possible to explore all colors contained
     * by an instance of theme data by looping from 0 to {@link #COLOR_COUNT}.
     * </p>
     */
    public static final int COLOR_COUNT = 74;



    // - Font definitions ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Font used in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     * </p>
     */
    public static final int FILE_TABLE_FONT = 0;

    /**
     * Font used to display shell output.
     * <p>
     * This defaults to the current <code>JTextArea</code> font.
     * </p>
     */
    public static final int SHELL_FONT = 1;

    /**
     * Font used in the file editor and viewer.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     * </p>
     */
    public static final int EDITOR_FONT = 2;

    /**
     * Font used in the location bar.
     * <p>
     * This defaults to the current <code>JTextField</code> font.
     * </p>
     */
    public static final int LOCATION_BAR_FONT = 3;

    /**
     * Font used in the shell history widget.
     * <p>
     * This defaults to the current <code>JTextField</code> font.
     * </p>
     */
    public static final int SHELL_HISTORY_FONT = 4;

    /**
     * Font used in the status bar.
     * <p>
     * This defaults to the current <code>JLabel</code> font.
     * </p>
     */
    public static final int STATUS_BAR_FONT = 5;



    // - Color definitions ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Color used to paint the folder panels' borders.
     * <p>
     * This defaults to <code>Color.GRAY</code>.
     * </p>
     */
    public static final int FILE_TABLE_BORDER_COLOR = 0;

    /**
     * Color used to paint the folder panel's background color.
     * <p>
     * Note that this only applies to bits of a folder panel that do not
     * contain files.
     * </p>
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int FILE_TABLE_BACKGROUND_COLOR = 1;

    /**
     * Color used to paint the folder panel's background color when it doesn't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_TABLE_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR = 2;

    /**
     * Color used to paint hidden files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int HIDDEN_FILE_FOREGROUND_COLOR = 3;

    /**
     * Color used to paint the background of hidden files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int HIDDEN_FILE_BACKGROUND_COLOR = 4;

    /**
     * Color used to paint hidden files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR = 5;

    /**
     * Color used to paint the background of hidden files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR = 6;

    /**
     * Color used to paint the background of selected hidden files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int HIDDEN_FILE_SELECTED_BACKGROUND_COLOR = 7;

    /**
     * Color used to paint the background of selected hidden files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 8;

    /**
     * Color used to paint selected hidden files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int HIDDEN_FILE_SELECTED_FOREGROUND_COLOR = 9;

    /**
     * Color used to paint selected hidden files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 10;

    /**
     * Color used to paint folders text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int FOLDER_FOREGROUND_COLOR = 11;

    /**
     * Color used to paint the background of folders in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int FOLDER_BACKGROUND_COLOR = 12;

    /**
     * Color used to paint folders text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FOLDER_UNFOCUSED_FOREGROUND_COLOR = 13;

    /**
     * Color used to paint the background of folders in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FOLDER_UNFOCUSED_BACKGROUND_COLOR = 14;

    /**
     * Color used to paint the background of selected folders in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int FOLDER_SELECTED_BACKGROUND_COLOR = 15;

    /**
     * Color used to paint the background of selected folders in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 16;

    /**
     * Color used to paint selected folders text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int FOLDER_SELECTED_FOREGROUND_COLOR = 17;

    /**
     * Color used to paint selected folders text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 18;

    /**
     * Color used to paint archives text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int ARCHIVE_FOREGROUND_COLOR = 19;

    /**
     * Color used to paint the background of archives in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int ARCHIVE_BACKGROUND_COLOR = 20;

    /**
     * Color used to paint archives text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int ARCHIVE_UNFOCUSED_FOREGROUND_COLOR = 21;

    /**
     * Color used to paint the background of archives in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int ARCHIVE_UNFOCUSED_BACKGROUND_COLOR = 22;

    /**
     * Color used to paint the background of selected archives in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int ARCHIVE_SELECTED_BACKGROUND_COLOR = 23;

    /**
     * Color used to paint the background of selected archives in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 24;

    /**
     * Color used to paint selected archives text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int ARCHIVE_SELECTED_FOREGROUND_COLOR = 25;

    /**
     * Color used to paint selected archives text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 26;

    /**
     * Color used to paint symlinks text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int SYMLINK_FOREGROUND_COLOR = 27;

    /**
     * Color used to paint the background of symlinks in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int SYMLINK_BACKGROUND_COLOR = 28;

    /**
     * Color used to paint symlinks text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int SYMLINK_UNFOCUSED_FOREGROUND_COLOR = 29;

    /**
     * Color used to paint the background of symlinks in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int SYMLINK_UNFOCUSED_BACKGROUND_COLOR = 30;

    /**
     * Color used to paint the background of selected symlinks in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int SYMLINK_SELECTED_BACKGROUND_COLOR = 31;

    /**
     * Color used to paint the background of selected symlinks in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 32;

    /**
     * Color used to paint selected symlinks text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int SYMLINK_SELECTED_FOREGROUND_COLOR = 33;

    /**
     * Color used to paint selected symlinks text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 34;

    /**
     * Color used to paint marked files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int MARKED_FOREGROUND_COLOR = 35;

    /**
     * Color used to paint the background of marked files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int MARKED_BACKGROUND_COLOR = 36;

    /**
     * Color used to paint marked files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int MARKED_UNFOCUSED_FOREGROUND_COLOR = 37;

    /**
     * Color used to paint the background of marked files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int MARKED_UNFOCUSED_BACKGROUND_COLOR = 38;

    /**
     * Color used to paint the background of selected marked files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int MARKED_SELECTED_BACKGROUND_COLOR = 39;

    /**
     * Color used to paint the background of selected marked files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 40;

    /**
     * Color used to paint selected marked files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int MARKED_SELECTED_FOREGROUND_COLOR = 41;

    /**
     * Color used to paint selected marked files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 42;

    /**
     * Color used to paint plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     * </p>
     */
    public static final int FILE_FOREGROUND_COLOR = 43;

    /**
     * Color used to paint the background of plain files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     * </p>
     */
    public static final int FILE_BACKGROUND_COLOR = 44;

    /**
     * Color used to paint plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FILE_UNFOCUSED_FOREGROUND_COLOR = 45;

    /**
     * Color used to paint the background of plain files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FILE_UNFOCUSED_BACKGROUND_COLOR = 46;

    /**
     * Color used to paint the background of selected plain files in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection background color.
     * </p>
     */
    public static final int FILE_SELECTED_BACKGROUND_COLOR = 47;

    /**
     * Color used to paint the background of selected plain files in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_SELECTED_BACKGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR = 48;

    /**
     * Color used to paint selected plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     * </p>
     */
    public static final int FILE_SELECTED_FOREGROUND_COLOR = 49;

    /**
     * Color used to paint selected plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     * </p>
     */
    public static final int FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR = 50;

    /**
     * Color used to paint shell commands output.
     * <p>
     * This defaults to the current <code>JTextArea</code> foreground color.
     * </p>
     */
    public static final int SHELL_FOREGROUND_COLOR = 51;

    /**
     * Color used to paint the background of shell commands output.
     * <p>
     * This defaults to the current <code>JTextArea</code> background color.
     * </p>
     */
    public static final int SHELL_BACKGROUND_COLOR = 52;

    /**
     * Color used to paint shell commands output when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection foreground color.
     * </p>
     */
    public static final int SHELL_SELECTED_FOREGROUND_COLOR = 53;

    /**
     * Color used to paint the background of shell commands output when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection background color.
     * </p>
     */
    public static final int SHELL_SELECTED_BACKGROUND_COLOR = 54;

    /**
     * Color used to paint the shell history's text.
     * <p>
     * This defaults to the current <code>JTextField</code> foreground color.
     * </p>
     */
    public static final int SHELL_HISTORY_FOREGROUND_COLOR = 55;

    /**
     * Color used to paint the shell history's background.
     * <p>
     * This defaults to the current <code>JTextField</code> background color.
     * </p>
     */
    public static final int SHELL_HISTORY_BACKGROUND_COLOR = 56;

    /**
     * Color used to paint the shell history's text when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection foreground color.
     * </p>
     */
    public static final int SHELL_HISTORY_SELECTED_FOREGROUND_COLOR = 57;

    /**
     * Color used to paint the shell history's background when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color.
     * </p>
     */
    public static final int SHELL_HISTORY_SELECTED_BACKGROUND_COLOR = 58;

    /**
     * Color used to paint the file editor / viewer's text.
     * <p>
     * This defaults to the current <code>JTextArea</code> foreground color.
     * </p>
     */
    public static final int EDITOR_FOREGROUND_COLOR = 59;

    /**
     * Color used to paint the file editor / viewer's background.
     * <p>
     * This defaults to the current <code>JTextArea</code> background color.
     * </p>
     */
    public static final int EDITOR_BACKGROUND_COLOR = 60;

    /**
     * Color used to paint the file editor / viewer's foreground when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection foreground color.
     * </p>
     */
    public static final int EDITOR_SELECTED_FOREGROUND_COLOR = 61;

    /**
     * Color used to paint the file editor / viewer's background when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection background color.
     * </p>
     */
    public static final int EDITOR_SELECTED_BACKGROUND_COLOR = 62;

    /**
     * Color used to paint the location's bar text.
     * <p>
     * This defaults to the current <code>JTextField</code> foreground color.
     * </p>
     */
    public static final int LOCATION_BAR_FOREGROUND_COLOR = 63;

    /**
     * Color used to paint the location's bar background.
     * <p>
     * This defaults to the current <code>JTextField</code> background color.
     * </p>
     */
    public static final int LOCATION_BAR_BACKGROUND_COLOR = 64;

    /**
     * Color used to paint the location's bar text when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection foreground color.
     * </p>
     */
    public static final int LOCATION_BAR_SELECTED_FOREGROUND_COLOR = 65;

    /**
     * Color used to paint the location's bar background when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color.
     * </p>
     */
    public static final int LOCATION_BAR_SELECTED_BACKGROUND_COLOR = 66;

    /**
     * Color used to paint the location's bar background when used as a progress bar.
     * <p>
     * Note that this color is painted over the location's bar background and foreground. In order
     * for anything to be visible under it, it needs to have an alpha transparency component.
     * </p>
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color, with an
     * alpha transparency value of 64.
     * </p>
     */
    public static final int LOCATION_BAR_PROGRESS_COLOR = 67;

    /**
     * Color used to paint the status bar's text.
     * <p>
     * This defaults to the current <code>JLabel</code> foreground color.
     * </p>
     */
    public static final int STATUS_BAR_FOREGROUND_COLOR = 68;

    /**
     * Color used to paint the status bar's background
     * <p>
     * This defaults to the current <code>JLabel</code> background color.
     * </p>
     */
    public static final int STATUS_BAR_BACKGROUND_COLOR = 69;

    /**
     * Color used to paint the status bar's border.
     * <p>
     * This defaults to <code>Color.GRAY</code>.
     * </p>
     */
    public static final int STATUS_BAR_BORDER_COLOR = 70;

    /**
     * Color used to paint the status bar's drive usage color when there's plenty of space left.
     * <p>
     * This defaults to <code>0x70EC2B</code>.
     * </p>
     */
    public static final int STATUS_BAR_OK_COLOR = 71;

    /**
     * Color used to paint the status bar's drive usage color when there's an average amount of space left.
     * <p>
     * This defaults to <code>0xFF7F00</code>.
     * </p>
     */
    public static final int STATUS_BAR_WARNING_COLOR = 72;

    /**
     * Color used to paint the status bar's drive usage color when there's dangerously little space left.
     * <p>
     * This defaults to <code>Color.RED</code>.
     * </p>
     */
    public static final int STATUS_BAR_CRITICAL_COLOR = 73;



    // - Default fonts -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // The following fields are look&feel dependant values for the fonts that are used by
    // themes. We need to monitor them, as they are prone to change through UIManager.

    /** Default font for text area components. */
    private static Font  DEFAULT_TEXT_AREA_FONT;
    /** Default font for text field components. */
    private static Font  DEFAULT_TEXT_FIELD_FONT;
    /** Default font for label components. */
    private static Font  DEFAULT_LABEL_FONT;
    /** Default font for table components. */
    private static Font  DEFAULT_TABLE_FONT;



    // - Default colors ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // The following fields are look&feel dependant values for the colors that are used by
    // themes. We need to monitor them, as they are prone to change through UIManager.

    /** Default foreground color for text area components. */
    private static Color DEFAULT_TEXT_AREA_COLOR;
    /** Default background color for text area components. */
    private static Color DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
    /** Default selection foreground color for text area components. */
    private static Color DEFAULT_TEXT_AREA_SELECTION_COLOR;
    /** Default selection background color for text area components. */
    private static Color DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
    /** Default foreground color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_COLOR;
    /** Default progress color for {@link com.mucommander.ui.comp.progress.ProgressTextField} components. */
    private static Color DEFAULT_TEXT_FIELD_PROGRESS_COLOR;
    /** Default background color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
    /** Default selection foreground color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_SELECTION_COLOR;
    /** Default selection background color for text field components. */
    private static Color DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
    /** Default foreground color for table components. */
    private static Color DEFAULT_TABLE_COLOR;
    /** Default background color for table components. */
    private static Color DEFAULT_TABLE_BACKGROUND_COLOR;
    /** Default selection foreground color for table components. */
    private static Color DEFAULT_TABLE_SELECTION_COLOR;
    /** Default selection background color for table components. */
    private static Color DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;



    // - Listeners -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Listeners on the default font and colors. */
    private static WeakHashMap listeners = new WeakHashMap();



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All the colors contained by the theme. */
    private Color[] colors;
    /** All the fonts contained by the theme. */
    private Font[]  fonts;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Starts listening on default colors and fonts.
     */
    static {UIManager.addPropertyChangeListener(new DefaultValuesListener());}


    /**
     * Creates an empty set of theme data.
     * <p>
     * <code>ThemeData</code> instances created that way will return default values for every
     * single one of their items.
     * </p>
     * @see #cloneData()
     */
    public ThemeData() {
        colors = new Color[COLOR_COUNT];
        fonts  = new Font[FONT_COUNT];
    }

    /**
     * Creates a new set of theme data.
     * <p>
     * The content of <code>from</code> will be copied in the new theme data. Note that
     * since we're copying the arrays themselves, rather than creating new ones and copying
     * each color and font individually, <code>from</code> will be unreliable at the end of this
     * call.
     * </p>
     * <p>
     * This constructor is only meant for optimisation purposes. When transforming
     * theme data in a proper theme, using this constructor allows us to not duplicate
     * all the fonts and color. It's a risky constructor to use, however, and should not be exposed
     * outside of the scope of the package.
     * </p>
     * @param from theme data from which to import values.
     */
    ThemeData(ThemeData from) {
        this();
        fonts  = from.fonts;
        colors = from.colors;
    }



    // - Data import / export ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Clones the current theme data.
     * <p>
     * This method allows callers to decide whether they want to <i>freeze</i> default values or
     * not. Freezing a value means that it will be considered to have been set to the default value,
     * and will not be updated when this default value changes.
     * </p>
     * @param  freezeDefaults whether or not to freeze the data's default values.
     * @return                a clone of the current theme data.
     * @see                   #cloneData()
     */
    public ThemeData cloneData(boolean freezeDefaults) {
        ThemeData data; // New data.
        int       i;    // Used to browse the fonts and colors.

        data = new ThemeData();

        // Clones the theme's colors.
        for(i = 0; i < COLOR_COUNT; i++)
            data.colors[i] = freezeDefaults ? getColor(i) : colors[i];

        // Clones the theme's fonts.
        for(i = 0; i < FONT_COUNT; i++)
            data.fonts[i] = freezeDefaults ? getFont(i) : fonts[i];

        return data;
    }

    /**
     * Clones the theme data without freezing default values.
     * <p>
     * This is a convenience method, and is exactly equivalent to calling <code>{@link #cloneData(boolean) cloneData(false)}</code>.
     * </p>
     * @return a clone of the current theme data.
     */
    public ThemeData cloneData() {return cloneData(false);}

    /**
     * Imports the specified data in the current one.
     * <p>
     * This method can be dangerous in that it overwrites every single value
     * of the current data without hope of retrieval. Moreoever, if something were to
     * go wrong during the operation and an exception was raised, the current data would
     * find itself in an invalid state, where some of its values would have been updated but
     * not all of them. It is up to callers to deal with these issues.
     * </p>
     * <p>
     * Values overwritting is done through the use of the current instance's {@link #setColor(int,Color)}
     * and {@link #setFont(int,Font)} methods. This allows subclasses to plug their own code here. A good
     * example of that is {@link Theme}, which will automatically trigger font and color events when
     * importing data.
     * </p>
     * @param data data to import.
     */
    public void importData(ThemeData data) {
        int i;

        // Imports the theme's colors.
        for(i = 0; i < COLOR_COUNT; i++)
            setColor(i, data.colors[i]);

        // Imports the theme's fonts.
        for(i = 0; i < FONT_COUNT; i++)
            setFont(i, data.fonts[i]);
    }



    // - Items setting --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the specified color to the specified value.
     * <p>
     * Use a value of <code>null</code> to restore the color to it's default value.
     * </p>
     * <p>
     * This method will return <code>false</code> if it didn't actually change the theme data.
     * This is checked through the use of <code>{@link #isColorDifferent(int,Color) isColorDifferent(}id,color)</code>.
     * </p>
     * <p>
     * Note that even if the color is found to be identical, the previous value will be overwritten -
     * this is a design choice, meant for these cases where developers need to work with home-made
     * subclasses of <code>Color</code>.
     * </p>
     * @param  id    identifier of the color to set.
     * @param  color value to which the color should be set.
     * @return       <code>true</code> if the call actually changed the data, <code>false</code> otherwise.
     */
    public synchronized boolean setColor(int id, Color color) {
        boolean buffer; // Used to store the result of isColorDifferent.

        buffer = isColorDifferent(id, color);
        colors[id] = color;

        return buffer;
    }

    /**
     * Sets the specified font to the specified value.
     * <p>
     * Use a value of <code>null</code> to restore the font to it's default value.
     * </p>
     * <p>
     * This method will return <code>false</code> if it didn't actually change the theme data.
     * This is checked through the use of <code>{@link #isFontDifferent(int,Font) isFontDifferent(}id, font)</code>.
     * </p>
     * <p>
     * Note that even if the font is found to be identical, the previous value will be overwritten -
     * this is a design choice, meant for these cases where developers need to work with home-made
     * subclasses of <code>Font</code>.
     * </p>
     * @param  id   identifier of the font to set.
     * @param  font value to which the font should be set.
     * @return      <code>true</code> if the call actually changed the data, <code>false</code> otherwise.
     */
    public synchronized boolean setFont(int id, Font font) {
        boolean buffer; // Used to store the result of isFontDifferent.

        buffer = isFontDifferent(id, font);
        fonts[id] = font;

        return buffer;
    }


    // - Items retrieval -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the requested color.
     * <p>
     * If the requested color wasn't set, its default value will be returned.
     * </p>
     * @param  id identifier of the color to retrieve.
     * @return    the requested color, or its default value if not set.
     * @see       #getDefaultColor(int)
     * @see       #isColorSet(int)
     */
    public synchronized Color getColor(int id) {
        checkColorIdentifier(id);
        return (colors[id] == null) ? getDefaultColor(id) : colors[id];
    }

    /**
     * Returns the requested font.
     * <p>
     * If the requested font wasn't set, its default value will be returned.
     * </p>
     * @param  id identifier of the font to retrieve.
     * @return    the requested font, or its default value if not set.
     * @see       #getDefaultFont(int)
     * @see       #isFontSet(int)
     */
    public synchronized Font getFont(int id) {
        checkFontIdentifier(id);
        return (fonts[id] == null) ? getDefaultFont(id) : fonts[id];
    }


    /**
     * Returns <code>true</code> if the specified color is set.
     * @param  id identifier of the color to check for.
     * @return    <code>true</code> if the specified color is set, <code>false</code> otherwise.
     * @see       #getDefaultColor(int)
     */
    public boolean isColorSet(int id) {return colors[id] != null;}

    /**
     * Returns <code>true</code> if the specified font is set.
     * @param  id identifier of the font to check for.
     * @return    <code>true</code> if the specified font is set, <code>false</code> otherwise.
     * @see       #getDefaultFont(int)
     */
    public boolean isFontSet(int id) {return fonts[id] != null;}

    /**
     * Returns the default value for the specified color.
     * <p>
     * Default values are look&amp;feel dependant, and are subject to change during the application's
     * life time.<br/>
     * Classes that need to monitor such changes can register themselves using {@link #addDefaultValuesListener(ThemeListener)}.
     * </p>
     * @param  id identifier of the color whose default value should be retrieved.
     * @return    the default value for the specified color.
     * @see       #addDefaultValuesListener(ThemeListener)
     */
    public static Color getDefaultColor(int id) {
        // Makes sure id is a legal color identifier.
        checkColorIdentifier(id);

        switch(id) {
            // File table background colors.
        case FILE_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR:
        case FOLDER_UNFOCUSED_BACKGROUND_COLOR:
        case ARCHIVE_UNFOCUSED_BACKGROUND_COLOR:
        case SYMLINK_UNFOCUSED_BACKGROUND_COLOR:
        case HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR:
        case MARKED_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_TABLE_BACKGROUND_COLOR:
        case HIDDEN_FILE_BACKGROUND_COLOR:
        case FOLDER_BACKGROUND_COLOR:
        case ARCHIVE_BACKGROUND_COLOR:
        case SYMLINK_BACKGROUND_COLOR:
        case MARKED_BACKGROUND_COLOR:
        case FILE_BACKGROUND_COLOR:
	    return getTableBackgroundColor();

            // File table foreground colors (everything except marked
            // defaults to the l&f specific table foreground color).
        case HIDDEN_FILE_FOREGROUND_COLOR:
        case FOLDER_FOREGROUND_COLOR:
        case ARCHIVE_FOREGROUND_COLOR:
        case SYMLINK_FOREGROUND_COLOR:
        case FILE_UNFOCUSED_FOREGROUND_COLOR:
        case HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR:
        case FOLDER_UNFOCUSED_FOREGROUND_COLOR:
        case ARCHIVE_UNFOCUSED_FOREGROUND_COLOR:
        case SYMLINK_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_FOREGROUND_COLOR:
	    return getTableColor();

            // Marked files foreground colors (they have to be different
            // of the standard file foreground colors).
        case MARKED_FOREGROUND_COLOR:
        case MARKED_UNFOCUSED_FOREGROUND_COLOR:
        case MARKED_SELECTED_FOREGROUND_COLOR:
        case MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
            return Color.RED;

            // Text areas default foreground colors.
        case SHELL_FOREGROUND_COLOR:
        case EDITOR_FOREGROUND_COLOR:
            return getTextAreaColor();

            // Text areas default background colors.
        case SHELL_BACKGROUND_COLOR:
        case EDITOR_BACKGROUND_COLOR:
            return getTextAreaBackgroundColor();

            // Text fields default foreground colors.
        case SHELL_HISTORY_FOREGROUND_COLOR:
        case LOCATION_BAR_FOREGROUND_COLOR:
        case STATUS_BAR_FOREGROUND_COLOR:
            return getTextFieldColor();

            // Text fields default background colors.
        case LOCATION_BAR_BACKGROUND_COLOR:
        case SHELL_HISTORY_BACKGROUND_COLOR:
            return getTextFieldBackgroundColor();

            // The location bar progress color is a bit of a special case,
            // as it requires alpha transparency.
        case LOCATION_BAR_PROGRESS_COLOR:
	    Color color;

	    color = getTextFieldSelectionBackgroundColor();
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);

            // Selected table background colors.
        case HIDDEN_FILE_SELECTED_BACKGROUND_COLOR:
        case FOLDER_SELECTED_BACKGROUND_COLOR:
        case ARCHIVE_SELECTED_BACKGROUND_COLOR:
        case SYMLINK_SELECTED_BACKGROUND_COLOR:
        case MARKED_SELECTED_BACKGROUND_COLOR:
        case FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR:
        case FILE_SELECTED_BACKGROUND_COLOR:
	    return getTableSelectionBackgroundColor();

            // Border colors.
        case FILE_TABLE_BORDER_COLOR:
        case STATUS_BAR_BORDER_COLOR:
            return Color.GRAY;

            // Foreground color for selected elements in the file table.
        case HIDDEN_FILE_SELECTED_FOREGROUND_COLOR:
        case FOLDER_SELECTED_FOREGROUND_COLOR:
        case ARCHIVE_SELECTED_FOREGROUND_COLOR:
        case SYMLINK_SELECTED_FOREGROUND_COLOR:
        case HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR:
        case FILE_SELECTED_FOREGROUND_COLOR:
	    return getTableSelectionColor();

            // Foreground color for selected text area elements.
        case SHELL_SELECTED_FOREGROUND_COLOR:
        case EDITOR_SELECTED_FOREGROUND_COLOR:
            return getTextAreaSelectionColor();

            // Background color for selected text area elements.
        case SHELL_SELECTED_BACKGROUND_COLOR:
        case EDITOR_SELECTED_BACKGROUND_COLOR:
            return getTextAreaSelectionBackgroundColor();

            // Foreground color for selected text fields elements.
        case LOCATION_BAR_SELECTED_FOREGROUND_COLOR:
        case SHELL_HISTORY_SELECTED_FOREGROUND_COLOR:
            return getTextFieldSelectionColor();

            // Background color for selected text fields elements.
        case SHELL_HISTORY_SELECTED_BACKGROUND_COLOR:
        case LOCATION_BAR_SELECTED_BACKGROUND_COLOR:
            return getTextFieldSelectionBackgroundColor();

            // Status bar defaults.
        case STATUS_BAR_BACKGROUND_COLOR:
            return new Color(0xD5D5D5);

            // Status bar 'ok' color.
        case STATUS_BAR_OK_COLOR:
            return new Color(0x70EC2B);

            // Status bar 'warning' color.
        case STATUS_BAR_WARNING_COLOR:
            return new Color(0xFF7F00);

            // Status bar 'critical' color.
        case STATUS_BAR_CRITICAL_COLOR:
            return Color.RED;
        }

        // This should never happen.
        return null;
    }

    /**
     * Returns the default value for the specified font.
     * <p>
     * Default values are look&amp;feel dependant, and are subject to change during the application's
     * life time.<br/>
     * Classes that need to monitor such changes can register themselves using {@link #addDefaultValuesListener(ThemeListener)}.
     * </p>
     * @param  id identifier of the font whose default value should be retrieved.
     * @return    the default value for the specified font.
     * @see       #addDefaultValuesListener(ThemeListener)
     */
    public static final Font getDefaultFont(int id) {
        checkFontIdentifier(id);

	switch(id) {
            // Table font.
        case FILE_TABLE_FONT:
            return getTableFont();

	    // Text Area font.
        case EDITOR_FONT:
        case SHELL_FONT:
	    return getTextAreaFont();

	    // Text Field font.
        case LOCATION_BAR_FONT:
        case SHELL_HISTORY_FONT:
	    return getTextFieldFont();

            // Label fonts.
        case STATUS_BAR_FONT:
	    return getLabelFont();
        }

        // This should never happen.
        return null;
    }



    // - Comparison ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified data and the current one are identical.
     * <p>
     * Comparisons is done by calling {@link #isFontDifferent(int,Font,boolean)} and {@link #isColorDifferent(int,Color,boolean)}
     * on every font and color. Refer to the documentation of these methods for more information on using the <code>ignoreDefaults</code>
     * parameter.
     * </p>
     * @param  data           data against which to compare.
     * @param  ignoreDefaults whether or not to compare default values.
     * @return                <code>true</code> if the specified data and the current one are identical, <code>false</code> otherwise.
     * @see                   #isFontDifferent(int,Font,boolean)
     * @see                   #isColorDifferent(int,Color,boolean)
     */
    public boolean isIdentical(ThemeData data, boolean ignoreDefaults) {
        int i;

        // Compares the colors.
        for(i = 0; i < COLOR_COUNT; i++)
            if(isColorDifferent(i, data.colors[i] , ignoreDefaults))
                return false;

        // Compares the fonts.
        for(i = 0; i < FONT_COUNT; i++)
            if(isFontDifferent(i, data.fonts[i], ignoreDefaults))
                return false;

        return true;
    }

    /**
     * Returns <code>true</code> if the current data is identical to the specified one, using default values when items haven't been set.
     * <p>
     * This is a convenience method, and is strictly equivalent to calling {@link #isIdentical(ThemeData,boolean) isIdentical(data, false)}.
     * </p>
     * @param  data data against which to compare.
     * @return      <code>true</code> if the specified data and the current one are identical, <code>false</code> otherwise.
     */
    public boolean isIdentical(ThemeData data) {return isIdentical(data, false);}

    /**
     * Checks whether the current font and the specified one are different from one another.
     * <p>
     * This is a convenience method, and is stricly equivalent to calling
     * <code>{@link #isFontDifferent(int,Font,boolean) isFontDifferent(}id, font, false)</code>.
     * </p>
     * @param  id   identifier of the font to check.
     * @param  font font to check.
     * @return      <code>true</code> if <code>font</code> is different from the one defined in the data.
     * @see         #isFontDifferent(int,Font,boolean)
     * @see         #isColorDifferent(int,Color)
     */
    public boolean isFontDifferent(int id, Font font) {return isFontDifferent(id, font, false);}

    /**
     * Checks whether the current font and the specified one are different from one another.
     * <p>
     * Setting <code>ignoreDefaults</code> to <code>false</code> will compare both fonts from a 'user' point of view: comparison
     * will be done on the values that are used by the rest of the application. It might however be necessary to consider
     * fonts to be different if one is set but not the other. This can be achieved by setting <code>ignoreDefaults</code> to <code>true</code>.
     * </p>
     * @param  id             identifier of the font to check.
     * @param  font           font to check.
     * @param  ignoreDefaults whether or not to ignore defaults if the requested item doesn't have a value.
     * @return                <code>true</code> if <code>font</code> is different from the one defined in the data.
     * @see                   #isFontDifferent(int,Font)
     * @see                   #isColorDifferent(int,Color)
     */
    public synchronized boolean isFontDifferent(int id, Font font, boolean ignoreDefaults) {
        checkFontIdentifier(id);

        // If the specified font is null, the only way for both fonts to be equal is for fonts[id]
        // to be null as well.
        if(font == null)
            return fonts[id] != null;

        // If fonts[id] is null and we're set to ignore defaults, both fonts are different.
        // If we're set to use defaults, we must compare font and the default value for id.
        if(fonts[id] == null)
            return ignoreDefaults ? true : !getDefaultFont(id).equals(font);

        // 'Standard' case: both fonts are set, compare them normally.
        return !font.equals(fonts[id]);
    }

    /**
     * Checks whether the current color and the specified one are different from one another.
     * <p>
     * This is a convenience method, and is stricly equivalent to calling
     * <code>{@link #isColorDifferent(int,Color,boolean) isColorDifferent(}id, color, false)</code>.
     * </p>
     * @param  id   identifier of the color to check.
     * @param  color color to check.
     * @return      <code>true</code> if <code>color</code> is different from the one defined in the data.
     * @see         #isColorDifferent(int,Color,boolean)
     * @see         #isFontDifferent(int,Font)
     */
    public boolean isColorDifferent(int id, Color color) {return isColorDifferent(id, color, false);}

    /**
     * Checks whether the current color and the specified one are different from one another.
     * <p>
     * Setting <code>ignoreDefaults</code> to <code>false</code> will compare both colors from a 'user' point of view: comparison
     * will be done on the values that are used by the rest of the application. It might however be necessary to consider
     * colors to be different if one is set but not the other. This can be achieved by setting <code>ignoreDefaults</code> to <code>true</code>.
     * </p>
     * @param  id             identifier of the color to check.
     * @param  color           color to check.
     * @param  ignoreDefaults whether or not to ignore defaults if the requested item doesn't have a value.
     * @return                <code>true</code> if <code>color</code> is different from the one defined in the data.
     * @see                   #isColorDifferent(int,Color)
     * @see                   #isFontDifferent(int,Font)
     */
    public synchronized boolean isColorDifferent(int id, Color color, boolean ignoreDefaults) {
        checkColorIdentifier(id);

        // If the specified color is null, the only way for both colors to be equal is for colors[id]
        // to be null as well.
        if(color == null)
            return colors[id] != null;

        // If colors[id] is null and we're set to ignore defaults, both colors are different.
        // If we're set to use defaults, we must compare color and the default value for id.
        if(colors[id] == null)
            return ignoreDefaults ? true : !getDefaultColor(id).equals(color);

        // 'Standard' case: both colors are set, compare them normally.
        return !color.equals(colors[id]);
    }



    // - Theme events --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Registers the specified theme listener.
     * <p>
     * The listener will receive {@link FontChangedEvent font} and {@link ColorChangedEvent color} events whenever
     * one of the default values has been changed, by a modification to the current look&amp;feel for example.
     * </p>
     * <p>
     * It is not necessary for 'themable' components to listen to default values, as they are automatically propagated
     * through {@link Theme} and {@link ThemeManager}.
     * </p>
     * <p>
     * Note that listeners are stored as weak references, to make sure that the API doesn't keep ghost copies of listeners
     * whose usefulness is long since past. This forces callers to make sure they keep a copy of the listener's instance: if
     * they do not, the instance will be weakly linked and garbage collected out of existence.
     * </p>
     * @param listener theme listener to register.
     * @see            #removeDefaultValuesListener(ThemeListener)
     */
    public static void addDefaultValuesListener(ThemeListener listener) {listeners.put(listener, null);}

    /**
     * Removes the specified instance from the list of registered theme listeners.
     * <p>
     * Note that since listeners are stored as weak references, calling this method is not strictly necessary. As soon
     * as a listener instance is not referenced anymore, it will automatically be caught and destroyed by the garbage
     * collector.
     * </p>
     * @param listener instance to remove from the list of registered theme listeners.
     * @see            #addDefaultValuesListener(ThemeListener)
     */
    public static void removeDefaultValuesListener(ThemeListener listener) {listeners.remove(listener);}

    /**
     * Dispatches a {@link FontChangedEvent} to all registered listeners.
     * @param id   identifier of the font that changed.
     * @param font new value for the font that changed.
     */
    private static void triggerFontEvent(int id, Font font) {
        Iterator         iterator; // Used to iterate through the listeners.
        FontChangedEvent event;    // Event that will be dispatched.

        // Creates the event.
        event = new FontChangedEvent(null, id, font);

        // Dispatches it.
        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).fontChanged(event);
    }

    /**
     * Dispatches a {@link ColorChangedEvent} to all registered listeners.
     * @param id    identifier of the color that changed.
     * @param color new value for the color that changed.
     */
    private static void triggerColorEvent(int id, Color color) {
        Iterator          iterator; // Used to iterate through the listeners.
        ColorChangedEvent event;    // Event that will be dispatched.

        // Creates the event.
        event = new ColorChangedEvent(null, id, color);

        // Dispatches it.
        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ThemeListener)iterator.next()).colorChanged(event);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Escapes the specified color.
     * <p>
     * This is a workaround for a strange Swing bug: in some cases, if a color is not an instance of
     * <code>java.awt.Color</code> but rather of one of its subclasses, it won't be painted properly.<br/>
     * </p>
     * @param  color color to escape.
     * @return       escaped color.
     */
    private static Color escapeColor(Color color) {return new Color(color.getRGB(), (color.getRGB() & 0xFF000000) != 0xFF000000);}

    /**
     * Checks whether the specified color identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color identifier.
     */
    private static void checkColorIdentifier(int id) {
        if(id < 0 || id >= COLOR_COUNT)
            throw new IllegalArgumentException("Illegal color identifier: " + id);
    }

    /**
     * Checks whether the specified font identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font identifier.
     */
    private static void checkFontIdentifier(int id) {
        if(id < 0 || id >= FONT_COUNT)
            throw new IllegalArgumentException("Illegal font identifier: " + id);
    }



    // - L&F dependant defaults ----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the current look and feel's table font.
     * <p>
     * If {@link #DEFAULT_TABLE_FONT} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default font before being returned.
     * </p>
     * @return the current look and feel's table font.
     */
    private static Font getTableFont() {
        if(DEFAULT_TABLE_FONT == null) {
            if((DEFAULT_TABLE_FONT = UIManager.getDefaults().getFont("Table.font")) == null)
                DEFAULT_TABLE_FONT = new JTable().getFont();
        }
	return DEFAULT_TABLE_FONT;
    }

    /**
     * Returns the current look and feel's text area font.
     * <p>
     * If {@link #DEFAULT_TEXT_AREA_FONT} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default font before being returned.
     * </p>
     * @return the current look and feel's text area font.
     */
    private static Font getTextAreaFont() {
        if(DEFAULT_TEXT_AREA_FONT == null) {
            if((DEFAULT_TEXT_AREA_FONT = UIManager.getDefaults().getFont("TextArea.font")) == null)
                DEFAULT_TEXT_AREA_FONT = new JTable().getFont();
        }
	return DEFAULT_TEXT_AREA_FONT;
    }

    /**
     * Returns the current look and feel's text field font.
     * <p>
     * If {@link #DEFAULT_FIELD_AREA_FONT} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default font before being returned.
     * </p>
     * @return the current look and feel's text field font.
     */
    private static Font getTextFieldFont() {
        if(DEFAULT_TEXT_FIELD_FONT == null) {
            if((DEFAULT_TEXT_FIELD_FONT = UIManager.getDefaults().getFont("TextField.font")) == null)
                DEFAULT_TEXT_FIELD_FONT = new JTable().getFont();
        }
	return DEFAULT_TEXT_FIELD_FONT;
    }

    /**
     * Returns the current look and feel's label font.
     * <p>
     * If {@link #DEFAULT_LABEL_FONT} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default font before being returned.
     * </p>
     * @return the current look and feel's label font.
     */
    private static Font getLabelFont() {
        if(DEFAULT_LABEL_FONT == null) {
            if((DEFAULT_LABEL_FONT = UIManager.getDefaults().getFont("Label.font")) == null)
                DEFAULT_LABEL_FONT = new JTable().getFont();
        }
	return DEFAULT_LABEL_FONT;
    }

    /**
     * Returns the current look and feel's text field 'progress' color.
     * <p>
     * If {@link #DEFAULT_TEXT_FIELD_PROGRESS_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text field 'progress' color.
     */
    private static synchronized Color getTextFieldProgressColor() {
        if(DEFAULT_TEXT_FIELD_PROGRESS_COLOR == null) {
            Color buffer;

            buffer = getTextFieldSelectionBackgroundColor();
            DEFAULT_TEXT_FIELD_PROGRESS_COLOR = escapeColor(new Color(buffer.getRed(), buffer.getGreen(), buffer.getBlue(), 64));
        }
	return DEFAULT_TEXT_AREA_COLOR;
    }

    /**
     * Returns the current look and feel's text area foreground color.
     * <p>
     * If {@link #DEFAULT_TEXT_AREA_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text area foreground color.
     */
    private static synchronized Color getTextAreaColor() {
        if(DEFAULT_TEXT_AREA_COLOR == null) {
            if((DEFAULT_TEXT_AREA_COLOR = UIManager.getDefaults().getColor("TextArea.foreground")) == null)
                DEFAULT_TEXT_AREA_COLOR = new JTextArea().getForeground();
            DEFAULT_TEXT_AREA_COLOR = escapeColor(DEFAULT_TEXT_AREA_COLOR);
        }
	return DEFAULT_TEXT_AREA_COLOR;
    }

    /**
     * Returns the current look and feel's text area background color.
     * <p>
     * If {@link #DEFAULT_TEXT_AREA_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text area background color.
     */
    private static synchronized Color getTextAreaBackgroundColor() {
        if(DEFAULT_TEXT_AREA_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_AREA_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextArea.background")) == null)
                DEFAULT_TEXT_AREA_BACKGROUND_COLOR = new JTextArea().getBackground();
            DEFAULT_TEXT_AREA_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_AREA_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
    }

    /**
     * Returns the current look and feel's text area selection foreground color.
     * <p>
     * If {@link #DEFAULT_TEXT_AREA_SELECTION_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text area selection foreground color.
     */
    private static synchronized Color getTextAreaSelectionColor() {
        if(DEFAULT_TEXT_AREA_SELECTION_COLOR == null) {
            if((DEFAULT_TEXT_AREA_SELECTION_COLOR = UIManager.getDefaults().getColor("TextArea.selectionForeground")) == null)
                DEFAULT_TEXT_AREA_SELECTION_COLOR = new JTextArea().getSelectionColor();
            DEFAULT_TEXT_AREA_SELECTION_COLOR = escapeColor(DEFAULT_TEXT_AREA_SELECTION_COLOR);
        }
	return DEFAULT_TEXT_AREA_SELECTION_COLOR;
    }

    /**
     * Returns the current look and feel's text area selection background color.
     * <p>
     * If {@link #DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text area selection background color.
     */
    private static synchronized Color getTextAreaSelectionBackgroundColor() {
        if(DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextArea.selectionBackground")) == null)
            DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = new JTextArea().getSelectedTextColor();
            DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
    }

    /**
     * Returns the current look and feel's text field foreground color.
     * <p>
     * If {@link #DEFAULT_TEXT_FIELD_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text field foreground color.
     */
    private static synchronized Color getTextFieldColor() {
        if(DEFAULT_TEXT_FIELD_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_COLOR = UIManager.getDefaults().getColor("TextField.foreground")) == null)
                DEFAULT_TEXT_FIELD_COLOR = new JTextField().getForeground();
            DEFAULT_TEXT_FIELD_COLOR = escapeColor(DEFAULT_TEXT_FIELD_COLOR);
        }
	return DEFAULT_TEXT_FIELD_COLOR;
    }

    /**
     * Returns the current look and feel's text field background color.
     * <p>
     * If {@link #DEFAULT_TEXT_FIELD_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text field background color.
     */
    private static synchronized Color getTextFieldBackgroundColor() {
        if(DEFAULT_TEXT_FIELD_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextField.background")) == null)
                DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = new JTextField().getBackground();
            DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_FIELD_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
    }

    /**
     * Returns the current look and feel's text field selection foreground color.
     * <p>
     * If {@link #DEFAULT_TEXT_FIELD_SELECTION_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text field selection foreground color.
     */
    private static synchronized Color getTextFieldSelectionColor() {
        if(DEFAULT_TEXT_FIELD_SELECTION_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_SELECTION_COLOR = UIManager.getDefaults().getColor("TextField.selectionForeground")) == null)
                DEFAULT_TEXT_FIELD_SELECTION_COLOR = new JTextField().getSelectionColor();
            DEFAULT_TEXT_FIELD_SELECTION_COLOR = escapeColor(DEFAULT_TEXT_FIELD_SELECTION_COLOR);
        }
	return DEFAULT_TEXT_FIELD_SELECTION_COLOR;
    }

    /**
     * Returns the current look and feel's text field selection background color.
     * <p>
     * If {@link #DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's text field selection background color.
     */
    private static synchronized Color getTextFieldSelectionBackgroundColor() {
        if(DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("TextField.selectionBackground")) == null)
                DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = new JTextField().getSelectedTextColor();
            DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
    }

    /**
     * Returns the current look and feel's table foreground color.
     * <p>
     * If {@link #DEFAULT_TABLE_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's table foreground color.
     */
    private static synchronized Color getTableColor() {
        if(DEFAULT_TABLE_COLOR == null) {
            if((DEFAULT_TABLE_COLOR = UIManager.getDefaults().getColor("Table.foreground")) == null)
                DEFAULT_TABLE_COLOR = new JTable().getForeground();
            DEFAULT_TABLE_COLOR = escapeColor(DEFAULT_TABLE_COLOR);
        }
	return DEFAULT_TABLE_COLOR;
    }

    /**
     * Returns the current look and feel's table background color.
     * <p>
     * If {@link #DEFAULT_TABLE_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's table background color.
     */
    private static synchronized Color getTableBackgroundColor() {
        if(DEFAULT_TABLE_BACKGROUND_COLOR == null) {
            if((DEFAULT_TABLE_BACKGROUND_COLOR = UIManager.getDefaults().getColor("Table.background")) == null)
                DEFAULT_TABLE_BACKGROUND_COLOR = new JTable().getBackground();
            DEFAULT_TABLE_BACKGROUND_COLOR = escapeColor(DEFAULT_TABLE_BACKGROUND_COLOR);
        }
        return DEFAULT_TABLE_BACKGROUND_COLOR;
    }

    /**
     * Returns the current look and feel's table foreground selection color.
     * <p>
     * If {@link #DEFAULT_TABLE_SELECTION_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's table foreground selection color.
     */
    private static synchronized Color getTableSelectionColor() {
        if(DEFAULT_TABLE_SELECTION_COLOR == null) {
            if((DEFAULT_TABLE_SELECTION_COLOR = UIManager.getDefaults().getColor("Table.selectionForeground")) == null)
                DEFAULT_TABLE_SELECTION_COLOR = new JTable().getSelectionForeground();
            DEFAULT_TABLE_SELECTION_COLOR = escapeColor(DEFAULT_TABLE_SELECTION_COLOR);
        }
	return DEFAULT_TABLE_SELECTION_COLOR;
    }

    /**
     * Returns the current look and feel's table background selection color.
     * <p>
     * If {@link #DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR} is not <code>null</code>, this method returns its value.
     * Otherwise, it will be set to the current default color before being returned.
     * </p>
     * @return the current look and feel's table background selection color.
     */
    private static synchronized Color getTableSelectionBackgroundColor() {
        if(DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR == null) {
            if((DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("Table.selectionBackground")) == null)
                DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = new JTable().getSelectionBackground();
            DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = escapeColor(DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR);
        }
	return DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;
    }

    /**
     * Resets the default text area color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextAreaColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_COLOR;
        DEFAULT_TEXT_AREA_COLOR = null;

        if(!getTextAreaColor().equals(buffer)) {
            triggerColorEvent(SHELL_FOREGROUND_COLOR, getTextAreaColor());
            triggerColorEvent(EDITOR_FOREGROUND_COLOR, getTextAreaColor());
        }
    }

    /**
     * Resets the default text area background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextAreaBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_BACKGROUND_COLOR;
        DEFAULT_TEXT_AREA_BACKGROUND_COLOR = null;

        if(!getTextAreaBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_BACKGROUND_COLOR, getTextAreaBackgroundColor());
            triggerColorEvent(EDITOR_BACKGROUND_COLOR, getTextAreaBackgroundColor());
        }
    }

    /**
     * Resets the default text area selection color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextAreaSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_SELECTION_COLOR;
        DEFAULT_TEXT_AREA_SELECTION_COLOR = null;

        if(!getTextAreaSelectionColor().equals(buffer)) {
            triggerColorEvent(SHELL_SELECTED_FOREGROUND_COLOR, getTextAreaSelectionColor());
            triggerColorEvent(EDITOR_SELECTED_FOREGROUND_COLOR, getTextAreaSelectionColor());
        }
    }

    /**
     * Resets the default text area selection background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextAreaSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TEXT_AREA_SELECTION_BACKGROUND_COLOR = null;

        if(!getTextAreaSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_SELECTED_BACKGROUND_COLOR, getTextAreaSelectionBackgroundColor());
            triggerColorEvent(EDITOR_SELECTED_BACKGROUND_COLOR, getTextAreaSelectionBackgroundColor());
        }
    }

    /**
     * Resets the default text field foreground color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextFieldColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_COLOR;
        DEFAULT_TEXT_FIELD_COLOR = null;

        if(!getTextFieldColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_FOREGROUND_COLOR, getTextFieldColor());
            triggerColorEvent(LOCATION_BAR_FOREGROUND_COLOR, getTextFieldColor());
            triggerColorEvent(STATUS_BAR_FOREGROUND_COLOR, getTextFieldColor());
        }
    }

    /**
     * Resets the default text field background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextFieldBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_BACKGROUND_COLOR;
        DEFAULT_TEXT_FIELD_BACKGROUND_COLOR = null;

        if(!getTextFieldBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_BACKGROUND_COLOR, getTextFieldBackgroundColor());
            triggerColorEvent(LOCATION_BAR_BACKGROUND_COLOR, getTextFieldBackgroundColor());
            triggerColorEvent(STATUS_BAR_BACKGROUND_COLOR, getTextFieldBackgroundColor());
        }
    }

    /**
     * Resets the default text field selection color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextFieldSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_SELECTION_COLOR;
        DEFAULT_TEXT_FIELD_SELECTION_COLOR = null;
        DEFAULT_TEXT_FIELD_PROGRESS_COLOR  = null;

        if(!getTextFieldSelectionColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, getTextFieldSelectionColor());
            triggerColorEvent(LOCATION_BAR_SELECTED_FOREGROUND_COLOR, getTextFieldSelectionColor());
            triggerColorEvent(LOCATION_BAR_PROGRESS_COLOR, getTextFieldProgressColor());
        }
    }

    /**
     * Resets the default text field selection background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextFieldSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND_COLOR = null;

        if(!getTextFieldSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, getTextFieldSelectionBackgroundColor());
            triggerColorEvent(LOCATION_BAR_SELECTED_BACKGROUND_COLOR, getTextFieldSelectionBackgroundColor());
        }
    }

    /**
     * Resets the default table foreground color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTableColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_COLOR;
        DEFAULT_TABLE_COLOR = null;

        if(!getTableColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FOLDER_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(ARCHIVE_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(SYMLINK_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FILE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FOLDER_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(SYMLINK_UNFOCUSED_FOREGROUND_COLOR, getTableColor());
            triggerColorEvent(FILE_FOREGROUND_COLOR, getTableColor());
        }
    }

    /**
     * Resets the default table background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTableBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_BACKGROUND_COLOR;
        DEFAULT_TABLE_BACKGROUND_COLOR = null;

        if(!getTableBackgroundColor().equals(buffer)) {
            triggerColorEvent(FILE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FOLDER_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(ARCHIVE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(SYMLINK_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(MARKED_UNFOCUSED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FOLDER_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(ARCHIVE_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(SYMLINK_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(MARKED_BACKGROUND_COLOR, getTableBackgroundColor());
            triggerColorEvent(FILE_TABLE_BACKGROUND_COLOR, getTableBackgroundColor());
        }
    }

    /**
     * Resets the default table selection foreground color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTableSelectionColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_SELECTION_COLOR;
        DEFAULT_TABLE_SELECTION_COLOR = null;

        if(!getTableSelectionColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FOLDER_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(ARCHIVE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(SYMLINK_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, getTableSelectionColor());
            triggerColorEvent(FILE_SELECTED_FOREGROUND_COLOR, getTableSelectionColor());
        }
    }

    /**
     * Resets the default table selection background color.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link ColorChangedEvent color events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTableSelectionBackgroundColor() {
        Color buffer;

        buffer = DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR;
        DEFAULT_TABLE_SELECTION_BACKGROUND_COLOR = null;

        if(!getTableSelectionBackgroundColor().equals(buffer)) {
            triggerColorEvent(HIDDEN_FILE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FOLDER_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(ARCHIVE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(SYMLINK_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(MARKED_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
            triggerColorEvent(FILE_SELECTED_BACKGROUND_COLOR, getTableSelectionBackgroundColor());
        }
    }

    /**
     * Resets the default table font.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link FontChangedEvent font events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTableFont() {
        Font buffer;

        buffer = DEFAULT_TABLE_FONT;
        DEFAULT_TABLE_FONT = null;

        if(!getTableFont().equals(buffer))
            triggerFontEvent(FILE_TABLE_FONT, getTableFont());
    }

    /**
     * Resets the default text area font.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link FontChangedEvent font events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextAreaFont() {
        Font buffer;

        buffer = DEFAULT_TEXT_AREA_FONT;
        DEFAULT_TEXT_AREA_FONT = null;

        if(!getTextAreaFont().equals(buffer)) {
            triggerFontEvent(EDITOR_FONT, getTextAreaFont());
            triggerFontEvent(SHELL_FONT, getTextAreaFont());
        }
    }

    /**
     * Resets the default text field font.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link FontChangedEvent font events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetTextFieldFont() {
        Font buffer;

        buffer = DEFAULT_TEXT_FIELD_FONT;
        DEFAULT_TEXT_FIELD_FONT = null;

        if(!getTextFieldFont().equals(buffer)) {
            triggerFontEvent(LOCATION_BAR_FONT, getTextFieldFont());
            triggerFontEvent(SHELL_HISTORY_FONT, getTextFieldFont());
        }
    }

    /**
     * Resets the default label font.
     * <p>
     * In addition to updating the current default color, this method will propagate {@link FontChangedEvent font events}
     * to listeners if necessary.
     * </p>
     */
    private static synchronized void resetLabelFont() {
        Font buffer;

        buffer = DEFAULT_LABEL_FONT;
        DEFAULT_LABEL_FONT = null;

        if(!getLabelFont().equals(buffer))
            triggerFontEvent(STATUS_BAR_FONT, getLabelFont());
    }



    // - Default values listener ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Listens on changes to the default colors.
     * @author Nicolas Rinaudo
     */
    private static class DefaultValuesListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            String property;
            property = event.getPropertyName();
            if(property.equals("lookAndFeel")) {
                resetTextAreaColor();
                resetTextAreaBackgroundColor();
                resetTextAreaSelectionColor();
                resetTextAreaSelectionBackgroundColor();
                resetTextFieldColor();
                resetTextFieldBackgroundColor();
                resetTextFieldSelectionColor();
                resetTextFieldSelectionBackgroundColor();
                resetTableColor();
                resetTableBackgroundColor();
                resetTableSelectionColor();
                resetTableSelectionBackgroundColor();
                resetTableFont();
                resetTextAreaFont();
                resetTextFieldFont();
                resetLabelFont();
            }
            else if(property.equals("TextArea.foreground"))
                resetTextAreaColor();
            else if(property.equals("TextArea.background"))
                resetTextAreaBackgroundColor();
            else if(property.equals("TextArea.selectionForeground"))
                resetTextAreaSelectionColor();
            else if(property.equals("TextArea.selectionBackground"))
                resetTextAreaSelectionBackgroundColor();
            else if(property.equals("TextField.foreground"))
                resetTextFieldColor();
            else if(property.equals("TextField.background"))
                resetTextFieldBackgroundColor();
            else if(property.equals("TextField.selectionForeground"))
                resetTextFieldSelectionColor();
            else if(property.equals("TextField.selectionBackground"))
                resetTextFieldSelectionBackgroundColor();
            else if(property.equals("Table.foreground"))
                resetTableColor();
            else if(property.equals("Table.background"))
                resetTableBackgroundColor();
            else if(property.equals("Table.selectionForeground"))
                resetTableSelectionColor();
            else if(property.equals("Table.selectionBackground"))
                resetTableSelectionBackgroundColor();
            else if(property.equals("Table.font"))
                resetTableFont();
            else if(property.equals("TextArea.font"))
                resetTextAreaFont();
            else if(property.equals("TextField.font"))
                resetTextFieldFont();
            else if(property.equals("Label.font"))
                resetLabelFont();
        }
    }
}
