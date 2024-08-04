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

/**
 * Enumeration of built-in actions
 * @author Arik Hadas
 */
public enum ActionType {
    AddBookmark("AddBookmark"),
    BatchRename("BatchRename"),
    BringAllToFront("BringAllToFront"),
    CalculateChecksum("CalculateChecksum"),
    ChangeDate("ChangeDate"),
    ChangeLocation("ChangeLocation"),
    ChangePermissions("ChangePermissions"),
    CheckForUpdates("CheckForUpdates"),
    CloneTabToOtherPanel("CloneTabToOtherPanel"),
    CloseDuplicateTabs("CloseDuplicateTabs"),
    CloseOtherTabs("CloseOtherTabs"),
    CloseTab("CloseTab"),
    CloseWindow("CloseWindow"),
    CombineFiles("CombineFiles"),
    CompareFolders("CompareFolders"),
    ConnectToServer("ConnectToServer"),
    Copy("Copy"),
    CopyFileBaseNames("CopyFileBaseNames"),
    CopyFileNames("CopyFileNames"),
    CopyFilePaths("CopyFilePaths"),
    CopyFilesToClipboard("CopyFilesToClipboard"),
    CustomizeCommandBar("CustomizeCommandBar"),
    Delete("Delete"),
    Donate("Donate"),
    DuplicateTab("DuplicateTab"),
    Edit("Edit"),
    EditBookmarks("EditBookmarks"),
    EditCredentials("EditCredentials"),
    Email("Email"),
    EmptyTrash("EmptyTrash"),
    ExploreBookmarks("ExploreBookmarks"),
    Find("Find"),
    FocusNext("FocusNext"),
    FocusPrevious("FocusPrevious"),
    GarbageCollect("GarbageCollect"),
    GoBack("GoBack"),
    GoForward("GoForward"),
    GoToDocumentation("GoToDocumentation"),
    GoToForums("GoToForums"),
    GoToHome("GoToHome"),
    GoToParent("GoToParent"),
    GoToParentInBothPanels("GoToParentInBothPanels"),
    GoToParentInOtherPanel("GoToParentInOtherPanel"),
    GoToRoot("GoToRoot"),
    GoToWebsite("GoToWebsite"),
    InternalEdit("InternalEdit"),
    InternalView("InternalView"),
    InvertSelection("InvertSelection"),
    LocalCopy("LocalCopy"),
    MarkAll("MarkAll"),
    MarkExtension("MarkExtension"),
    MarkGroup("MarkGroup"),
    MarkNextBlock("MarkNextBlock"),
    MarkNextPage("MarkNextPage"),
    MarkNextRow("MarkNextRow"),
    MarkPreviousBlock("MarkPreviousBlock"),
    MarkPreviousPage("MarkPreviousPage"),
    MarkPreviousRow("MarkPreviousRow"),
    MarkSelectedFile("MarkSelectedFile"),
    MarkToFirstRow("MarkToFirstRow"),
    MarkToLastRow("MarkToLastRow"),
    MaximizeWindow("MaximizeWindow"),
    MinimizeWindow("MinimizeWindow"),
    Mkdir("Mkdir"),
    Mkfile("Mkfile"),
    Move("Move"),
    MoveTabToOtherPanel("MoveTabToOtherPanel"),
    NewTab("NewTab"),
    NewWindow("NewWindow"),
    NextTab("NextTab"),
    Open("Open"),
    OpenAs("OpenAs"),
    OpenCommandPrompt("OpenCommandPrompt"),
    OpenInBothPanels("OpenInBothPanels"),
    OpenInNewTab("OpenInNewTab"),
    OpenInOtherPanel("OpenInOtherPanel"),
    OpenNatively("OpenNatively"),
    OpenTrash("OpenTrash"),
    OpenURLInBrowser("OpenURLInBrowser"),
    Pack("Pack"),
    PasteClipboardFiles("PasteClipboardFiles"),
    PermanentDelete("PermanentDelete"),
    PopupLeftDriveButton("PopupLeftDriveButton"),
    PopupRightDriveButton("PopupRightDriveButton"),
    PreviousTab("PreviousTab"),
    QuickFind("QuickFind"),
    Quit("Quit"),
    RecallNextWindow("RecallNextWindow"),
    RecallPreviousWindow("RecallPreviousWindow"),
    RecallWindow10("RecallWindow10"),
    RecallWindow1("RecallWindow1"),
    RecallWindow2("RecallWindow2"),
    RecallWindow3("RecallWindow3"),
    RecallWindow4("RecallWindow4"),
    RecallWindow5("RecallWindow5"),
    RecallWindow6("RecallWindow6"),
    RecallWindow7("RecallWindow7"),
    RecallWindow8("RecallWindow8"),
    RecallWindow9("RecallWindow9"),
    Redo("Redo"),
    Refresh("Refresh"),
    Rename("Rename"),
    ReportBug("ReportBug"),
    RevealInDesktop("RevealInDesktop"),
    ReverseSortOrder("ReverseSortOrder"),
    Save("Save"),
    SelectFirstRow("SelectFirstRow"),
    SelectLastRow("SelectLastRow"),
    SelectNextBlock("SelectNextBlock"),
    SelectNextPage("SelectNextPage"),
    SelectNextRow("SelectNextRow"),
    SelectPreviousBlock("SelectPreviousBlock"),
    SelectPreviousPage("SelectPreviousPage"),
    SelectPreviousRow("SelectPreviousRow"),
    SetSameFolder("SetSameFolder"),
    SetTabTitle("SetTabTitle"),
    ShowAbout("ShowAbout"),
    ShowBookmarksQL("ShowBookmarksQL"),
    ShowDebugConsole("ShowDebugConsole"),
    ShowFilePopupMenu("ShowFilePopupMenu"),
    ShowFileProperties("ShowFileProperties"),
    ShowInEnclosingFolder("ShowInEnclosingFolder"),
    ShowKeyboardShortcuts("ShowKeyboardShortcuts"),
    ShowParentFoldersQL("ShowParentFoldersQL"),
    ShowPreferences("ShowPreferences"),
    ShowRecentExecutedFilesQL("ShowRecentExecutedFilesQL"),
    ShowRecentLocationsQL("ShowRecentLocationsQL"),
    ShowRootFoldersQL("ShowRootFoldersQL"),
    ShowServerConnections("ShowServerConnections"),
    ShowTabsQL("ShowTabsQL"),
    SortByDate("SortByDate"),
    SortByExtension("SortByExtension"),
    SortByGroup("SortByGroup"),
    SortByName("SortByName"),
    SortByOwner("SortByOwner"),
    SortByPermissions("SortByPermissions"),
    SortBySize("SortBySize"),
    SplitEqually("SplitEqually"),
    SplitFile("SplitFile"),
    SplitHorizontally("SplitHorizontally"),
    SplitVertically("SplitVertically"),
    Stop("Stop"),
    SwapFolders("SwapFolders"),
    SwitchActiveTable("SwitchActiveTable"),
    ToggleAutoSize("ToggleAutoSize"),
    ToggleCommandBar("ToggleCommandBar"),
    ToggleDateColumn("ToggleDateColumn"),
    ToggleExtensionColumn("ToggleExtensionColumn"),
    ToggleGroupColumn("ToggleGroupColumn"),
    ToggleHiddenFiles("ToggleHiddenFiles"),
    ToggleLockTab("ToggleLockTab"),
    ToggleOwnerColumn("ToggleOwnerColumn"),
    ToggleShowParentFolder("ToggleShowParentFolder"),
    TogglePermissionsColumn("TogglePermissionsColumn"),
    ToggleShowFoldersFirst("ToggleShowFoldersFirst"),
    ToggleSizeColumn("ToggleSizeColumn"),
    ToggleStatusBar("ToggleStatusBar"),
    ToggleTerminal("ToggleTerminal"),
    ToggleToolBar("ToggleToolBar"),
    ToggleTree("ToggleTree"),
    ToggleUseSinglePanel("ToggleSinglePanel"),
    UnmarkAll("UnmarkAll"),
    UnmarkGroup("UnmarkGroup"),
    Unpack("Unpack"),
    Undo("Undo"),
    View("View");
    

    private String id;

    ActionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
