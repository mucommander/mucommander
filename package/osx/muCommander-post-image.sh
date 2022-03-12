#!/bin/sh
sed -i '' '5i\
  <key>NSSupportsAutomaticGraphicsSwitching<\/key>\
  <true\/>
' build/muCommander.app/Contents/Info.plist 
