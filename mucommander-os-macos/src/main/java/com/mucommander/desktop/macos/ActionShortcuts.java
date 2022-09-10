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

package com.mucommander.desktop.macos;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.mucommander.desktop.ActionType;

/**
 * For macOS, CTRL is replaced with META key for action shortcuts and the original shortcuts (with CTRL) become the default
 * alternative shortcuts.
 * @author Arik Hadas
 */
public class ActionShortcuts extends com.mucommander.desktop.ActionShortcuts {

    @Override
    public KeyStroke getDefaultKeystroke(ActionType actionId) {
        switch(actionId) {
        case AddBookmark:
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.META_DOWN_MASK);
        case ChangeLocation:
            return KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_DOWN_MASK);
        case CloseOtherTabs:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case CloseTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_DOWN_MASK);
        case CompareFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_DOWN_MASK);
        case ConnectToServer:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.META_DOWN_MASK);
        case CopyFileBaseNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case CopyFileNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case CopyFilesToClipboard:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK);
        case EditCredentials:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case Email:
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK);
        case ExploreBookmarks:
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case Find:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK);
        case GoBack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.META_DOWN_MASK);
        case GoForward:
            return KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.META_DOWN_MASK);
        case GoToParentInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case GoToParentInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.META_DOWN_MASK);
        case MarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK);
        case MarkNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.META_DOWN_MASK);
        case MarkPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.META_DOWN_MASK);
        case NewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.META_DOWN_MASK);
        case NewWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK);
        case NextTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case OpenInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case OpenInNewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.META_DOWN_MASK);
        case OpenInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_DOWN_MASK);
        case Pack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.META_DOWN_MASK);
        case PasteClipboardFiles:
            return KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK);
        case PreviousTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case QuickFind:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case Quit:
            return KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_DOWN_MASK);
        case RecallNextWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case RecallPreviousWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case RecallWindow10:
            return KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.META_DOWN_MASK);
        case RecallWindow1:
            return KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.META_DOWN_MASK);
        case RecallWindow2:
            return KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.META_DOWN_MASK);
        case RecallWindow3:
            return KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.META_DOWN_MASK);
        case RecallWindow4:
            return KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.META_DOWN_MASK);
        case RecallWindow5:
            return KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.META_DOWN_MASK);
        case RecallWindow6:
            return KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.META_DOWN_MASK);
        case RecallWindow7:
            return KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.META_DOWN_MASK);
        case RecallWindow8:
            return KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.META_DOWN_MASK);
        case RecallWindow9:
            return KeyStroke.getKeyStroke(KeyEvent.VK_9, KeyEvent.META_DOWN_MASK);
        case RevealInDesktop:
            return KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_DOWN_MASK);
        case RunCommand:
            return KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.META_DOWN_MASK);
        case Save:
            return KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK);
        case SelectNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_DOWN_MASK);
        case SelectPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_DOWN_MASK);
        case SetSameFolder:
            return KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.META_DOWN_MASK);
        case ShowFilePopupMenu:
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.META_DOWN_MASK);
        case ShowServerConnections:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
        case SortByDate:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.META_DOWN_MASK);
        case SortByExtension:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.META_DOWN_MASK);
        case SortByGroup:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.META_DOWN_MASK);
        case SortByName:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.META_DOWN_MASK);
        case SortByOwner:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.META_DOWN_MASK);
        case SortByPermissions:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.META_DOWN_MASK);
        case SortBySize:
            return KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_DOWN_MASK);
        case SwapFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.META_DOWN_MASK);
        case ToggleTerminal:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_DOWN_MASK);
        case ToggleTree:
            return KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.META_DOWN_MASK);
        case UnmarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_DOWN_MASK);
        case Unpack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_DOWN_MASK);
        default:
            return super.getDefaultKeystroke(actionId);
        }
    }

    @Override
    public KeyStroke getDefaultAltKeyStroke(ActionType actionId) {
        switch(actionId) {
        case AddBookmark:
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
        case ChangeLocation:
            return KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK);
        case CloseOtherTabs:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        case CloseTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK);
        case CompareFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);
        case ConnectToServer:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
        case CopyFileBaseNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case CopyFileNames:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case CopyFilesToClipboard:
            return KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
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
        case GoToParentInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case GoToParentInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        case MarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        case MarkNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK);
        case MarkPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK);
        case NewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);
        case NewWindow:
            return KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        case NextTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK);
        case OpenInBothPanels:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
        case OpenInNewTab:
            return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        case OpenInOtherPanel:
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        case Pack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK);
        case PasteClipboardFiles:
            return KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
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
        case RevealInDesktop:
            return KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        case RunCommand:
            return KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
        case SelectNextBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK);
        case SelectPreviousBlock:
            return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK);
        case SetSameFolder:
            return KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        case ShowFilePopupMenu:
            return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        case ShowServerConnections:
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
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
        case SwapFolders:
            return KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        case ToggleTree:
            return KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK);
        case UnmarkAll:
            return KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
        case Unpack:
            return KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
        default:
            return super.getDefaultAltKeyStroke(actionId);
        }
    }
}
