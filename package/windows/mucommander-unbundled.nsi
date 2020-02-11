; -*- coding: utf-8 -*-
; mucommander install script
;

; Include Modern UI
!include MUI2.nsh

; The name of the installer
Name "muCommander @MU_VERSION@"

; The file to write
OutFile @MU_OUT@

; Installer icon
!define MUI_ICON @MU_ICON@
!define MUI_UNICON @MU_ICON@

; The default installation directory
InstallDir $PROGRAMFILES\muCommander
; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM SOFTWARE\muCommander "Install_Dir"

; Specifies the requested execution level for Windows Vista. 
; Necessary for correct uninstallation of Start menu shortcuts.
RequestExecutionLevel admin

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!define MUI_COMPONENTSPAGE_NODESC
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN "$INSTDIR\muCommander.exe"
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; Languages
; Installer should support same languages as muCommander.
!insertmacro MUI_LANGUAGE "English" ; first language is the default language
!insertmacro MUI_LANGUAGE "French"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro MUI_LANGUAGE "SpanishInternational"
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro MUI_LANGUAGE "SimpChinese"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro MUI_LANGUAGE "Polish"
!insertmacro MUI_LANGUAGE "Hungarian"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "Slovenian"
!insertmacro MUI_LANGUAGE "Romanian"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro MUI_LANGUAGE "Portuguese"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro MUI_LANGUAGE "Slovak"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro MUI_LANGUAGE "Swedish"
!insertmacro MUI_LANGUAGE "Danish"

; The stuff to install
Section "muCommander @MU_VERSION@ (required)"
  ; Read only section. It will always be set to install.
  SectionIn RO

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  ; Copy muCommander files
  File /oname=muCommander.exe @MU_EXE@
  File /oname=readme.txt @MU_README@
  File /oname=license.txt @MU_LICENSE@
  File @MU_JAR@
  File /r "app"
  File /r "bundle"
  ; Create writable directory for felix-cache
  CreateDirectory "$INSTDIR\felix-cache"
  AccessControl::GrantOnFile "$INSTDIR\felix-cache" "(BU)" "GenericRead + GenericWrite"
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\muCommander "Install_Dir" "$INSTDIR"
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "DisplayName" "muCommander (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
  ; Create Start Menu directory and shortcuts
  CreateDirectory "$SMPROGRAMS\muCommander"
  CreateShortCut "$SMPROGRAMS\muCommander\muCommander.lnk" "$INSTDIR\muCommander.exe" "" "" 0 SW_SHOWMINIMIZED
  ApplicationID::Set "$SMPROGRAMS\muCommander\muCommander.lnk" "muCommander"
  CreateShortCut "$SMPROGRAMS\muCommander\Read Me.lnk" "$INSTDIR\readme.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\License.lnk" "$INSTDIR\license.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\muCommander\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "" 0 
SectionEnd

; Quick launch shortcut (optional section)
Section "Quick Launch shortcut"
  CreateShortCut "$QUICKLAUNCH\muCommander.lnk" "$INSTDIR\muCommander.exe" "" "" 0 SW_SHOWMINIMIZED
  ApplicationID::Set "$QUICKLAUNCH\muCommander.lnk" "muCommander"
SectionEnd

; Desktop shortcut (optional section)
Section "Desktop shortcut"
  CreateShortCut "$DESKTOP\muCommander.lnk" "$INSTDIR\muCommander.exe" "" "" 0 SW_SHOWMINIMIZED
  ApplicationID::Set "$DESKTOP\muCommander.lnk" "muCommander"
SectionEnd

; Special uninstall section.
Section "Uninstall"
  ; remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\muCommander"
  DeleteRegKey HKLM SOFTWARE\muCommander
  ; remove files
  Delete $INSTDIR\muCommander.exe
  Delete $INSTDIR\@MU_JAR@
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
  RMDir /r "$INSTDIR\app"
  RMDir /r "$INSTDIR\bundle"
  RMDir /r "$INSTDIR\felix-cache"
  RMDir "$INSTDIR"
SectionEnd

; eof
