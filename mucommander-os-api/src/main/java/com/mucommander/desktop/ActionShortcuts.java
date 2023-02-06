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

package com.mucommander.desktop;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * This class returns default keyboard shortcuts and default alternative keyboard shortcuts for {@code com.mucommander.ui.action.MuAction} subclasses
 * @author Arik Hadas
 */
public class ActionShortcuts {

    public KeyStroke getDefaultKeystroke(ActionType actionId) {
        switch(actionId) {
        case AddBookmark:
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
        case BatchRename:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.ALT_DOWN_MASK);
        case CalculateChecksum:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        case ChangeDate:
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        case ChangeLocation:
            return KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK);
        case ChangePermissions:
            return KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        case CloseOtherTabs:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case CloseTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK);
        case CloseWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        case CompareFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);
        case ConnectToServer:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
        case Copy:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        case CopyFileBaseNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case CopyFileNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case CopyFilePaths:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        case CopyFilesToClipboard:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        case Delete:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        case DuplicateTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_DOWN_MASK);
        case Edit:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        case EditCredentials:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case Email:
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        case ExploreBookmarks:
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case Find:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        case GoBack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK);
        case GoForward:
            return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK);
        case GoToParent:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        case GoToParentInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case GoToParentInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        case GoToRoot:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK);
        case InvertSelection:
            return KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0);
        case LocalCopy:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK);
        case MarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        case MarkExtension:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.SHIFT_DOWN_MASK);
        case MarkGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0);
        case MarkNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK);
        case MarkNextPage:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        case MarkNextRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        case MarkPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK);
        case MarkPreviousPage:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK);
        case MarkPreviousRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK);
        case MarkSelectedFile:
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        case MarkToFirstRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK);
        case MarkToLastRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK);
        case MinimizeWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_DOWN_MASK);
        case Mkdir:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        case Mkfile:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK);
        case Move:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        case NewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);
        case NewWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        case NextTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK);
        case Open:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        case OpenCommandPrompt:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK);
        case OpenInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case OpenInNewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        case OpenInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        case OpenNatively:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        case OpenTrash:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.ALT_DOWN_MASK);
        case Pack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK);
        case PasteClipboardFiles:
            return KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        case PermanentDelete:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.SHIFT_DOWN_MASK);
        case PopupLeftDriveButton:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.ALT_DOWN_MASK);
        case PopupRightDriveButton:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.ALT_DOWN_MASK);
        case PreviousTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK);
        case QuickFind:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK  | KeyEvent.SHIFT_DOWN_MASK);
        case Quit:
            return KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
        case RecallNextWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case RecallPreviousWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow10:
            return KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow1:
            return KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow2:
            return KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow3:
            return KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow4:
            return KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow5:
            return KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow6:
            return KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow7:
            return KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow8:
            return KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.CTRL_DOWN_MASK);
        case RecallWindow9:
            return KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.CTRL_DOWN_MASK);
        case Redo:
            return KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case Refresh:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        case Rename:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK);
        case RevealInDesktop:
            return KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        case RunCommand:
            return KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
        case Save:
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        case SelectFirstRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
        case SelectLastRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
        case SelectNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK);
        case SelectNextPage:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
        case SelectNextRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        case SelectPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK);
        case SelectPreviousPage:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
        case SelectPreviousRow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        case SetSameFolder:
            return KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        case ShowBookmarksQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK);
        case ShowFilePopupMenu:
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        case ShowFileProperties:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK);
        case ShowParentFoldersQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK);
        case ShowRecentExecutedFilesQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK);
        case ShowRecentLocationsQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK);
        case ShowRootFoldersQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK);
        case ShowServerConnections:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case ShowTabsQL:
            return KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK);
        case SortByDate:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.CTRL_DOWN_MASK);
        case SortByExtension:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_DOWN_MASK);
        case SortByGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.CTRL_DOWN_MASK);
        case SortByName:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK);
        case SortByOwner:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_DOWN_MASK);
        case SortByPermissions:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK);
        case SortBySize:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_DOWN_MASK);
        case Stop:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        case SwapFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        case SwitchActiveTable:
            return KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        case ToggleHiddenFiles:
            return KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        case ToggleTerminal:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        case ToggleTree:
            return KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK);
        case UnmarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
        case UnmarkGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0);
        case Unpack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
        case Undo:
            return KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        case View:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        default:
            return null;
        }
    }

    public KeyStroke getDefaultAltKeyStroke(ActionType actionId) {
        switch(actionId) {
        case CloseWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_DOWN_MASK);
        case CopyFilesToClipboard:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK);
        case Delete:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        case GoBack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK);
        case GoForward:
            return KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK);
        case GoToParent:
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        case GoToRoot:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        case InvertSelection:
            return KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case MarkExtension:
            return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK);
        case MarkGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case MarkSelectedFile:
            return KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0);
        case NextTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case Open:
            return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        case PasteClipboardFiles:
            return KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK);
        case PermanentDelete:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK);
        case PreviousTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case ShowFilePopupMenu:
            return KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0);
        case SwitchActiveTable:
            return KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        case UnmarkGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK);
        default:
            return null;
        }
    }
}
