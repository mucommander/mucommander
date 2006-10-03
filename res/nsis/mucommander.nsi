;
; mucommander install script
;

; The name of the installer
Name "muCommander 0.8 beta3"

; The file to write
OutFile "mucommander-0_8_beta3.exe"

; Installer icon
Icon "mucommander.ico"

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
Section "muCommander 0.8 beta3 (required)"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  ; Copy muCommander files
  File "..\dist\mucommander.jar"
  File "mucommander.bat"
  File "mucommander.ico"
  File "..\readme.txt"
  File "..\license.txt"
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\muCommander "Install_Dir" "$INSTDIR"
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "DisplayName" "muCommander (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteUninstaller "uninstall.exe"
  
  ; Create Start Menu directory and shortcuts
  CreateDirectory "$SMPROGRAMS\muCommander"
  CreateShortCut "$SMPROGRAMS\muCommander\muCommander.lnk" '$INSTDIR\mucommander.bat' "" "$INSTDIR\mucommander.ico" 0 SW_SHOWMINIMIZED
;  CreateShortCut "$SMPROGRAMS\muCommander\muCommander.lnk" '%JAVA_HOME%\bin\javaw.exe' "-jar mucommander.jar" "$INSTDIR\mucommander.ico" 0
  CreateShortCut "$SMPROGRAMS\muCommander\muCommander.jar.lnk" '$INSTDIR\mucommander.jar' "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\Read Me.lnk" "$INSTDIR\readme.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\License.lnk" "$INSTDIR\license.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "" 0 
  
  ; Open readme.txt
  Exec "notepad.exe $INSTDIR\readme.txt"
SectionEnd

; Quick launch shortcut (optional section)
Section "Quick Launch shortcut"
  CreateShortCut "$QUICKLAUNCH\muCommander.lnk" '$INSTDIR\mucommander.bat' "" "$INSTDIR\mucommander.ico" 0 SW_SHOWMINIMIZED
SectionEnd

; Desktop shortcut (optional section)
Section "Desktop shortcut"
  CreateShortCut "$DESKTOP\muCommander.lnk" '$INSTDIR\mucommander.bat' "" "$INSTDIR\mucommander.ico" 0 SW_SHOWMINIMIZED
SectionEnd

; uninstall stuff

UninstallText "This will uninstall muCommander. Hit next to continue."

; special uninstall section.
Section "Uninstall"
  ; remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander"
  DeleteRegKey HKLM SOFTWARE\muCommander
  ; remove files
  Delete $INSTDIR\mucommander.jar
  Delete $INSTDIR\mucommander.bat
  Delete $INSTDIR\mucommander.ico
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
