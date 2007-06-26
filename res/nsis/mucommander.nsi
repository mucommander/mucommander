;
; mucommander install script
;

; The name of the installer
Name "muCommander @MU_VERSION@"

; The file to write
OutFile @MU_OUT@

; Installer icon
Icon @MU_ICON@
UninstallIcon @MU_ICON@

; Discard NSIS' window when install is complete
AutoCloseWindow true
ShowInstDetails nevershow

; The default installation directory
InstallDir $PROGRAMFILES\muCommander
; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM SOFTWARE\muCommander "Install_Dir"

; The text to prompt the user to enter a directory
ComponentText "This will install muCommander on your computer."
; The text to prompt the user to enter a directory
DirText "Choose a directory to install muCommander in :"

; The stuff to install
Section "muCommander @MU_VERSION@ (required)"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  ; Copy muCommander files
  File /oname=muCommander.exe @MU_EXE@
  File /oname=readme.txt @MU_README@
  File /oname=license.txt @MU_LICENSE@
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\muCommander "Install_Dir" "$INSTDIR"
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "DisplayName" "muCommander (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteUninstaller "uninstall.exe"
  
  ; Create Start Menu directory and shortcuts
  CreateDirectory "$SMPROGRAMS\muCommander"
  CreateShortCut "$SMPROGRAMS\muCommander\muCommander.lnk" '$INSTDIR\muCommander.exe' "" "" 0 SW_SHOWMINIMIZED
  CreateShortCut "$SMPROGRAMS\muCommander\Read Me.lnk" "$INSTDIR\readme.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\License.lnk" "$INSTDIR\license.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "" 0 
  
  ; Open readme.txt
  Exec "notepad.exe $INSTDIR\readme.txt"
SectionEnd

; Quick launch shortcut (optional section)
Section "Quick Launch shortcut"
  CreateShortCut "$QUICKLAUNCH\muCommander.lnk" '$INSTDIR\muCommander.exe' "" "" 0 SW_SHOWMINIMIZED
SectionEnd

; Desktop shortcut (optional section)
Section "Desktop shortcut"
  CreateShortCut "$DESKTOP\muCommander.lnk" '$INSTDIR\muCommander.exe' "" "" 0 SW_SHOWMINIMIZED
SectionEnd

; uninstall stuff

UninstallText "This will uninstall muCommander. Hit next to continue."

; special uninstall section.
Section "Uninstall"
  ; remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander"
  DeleteRegKey HKLM SOFTWARE\muCommander
  ; remove files
  Delete $INSTDIR\muCommander.exe
  Delete $INSTDIR\muCommander.lnk
  Delete $INSTDIR\readme.txt
  Delete $INSTDIR\license.txt
  ; MUST REMOVE UNINSTALLER, too
  Delete $INSTDIR\uninstall.exe
  ; remove shortcuts, if any.
  Delete "$SMPROGRAMS\muCommander\*.*"
  Delete "$QUICKLAUNCH\muCommander.lnk"
  Delete "$DESKTOP\muCommander.lnk"
  ; remove directories used.
  RMDir "$SMPROGRAMS\muCommander"
  RMDir "$INSTDIR"
SectionEnd

; eof
