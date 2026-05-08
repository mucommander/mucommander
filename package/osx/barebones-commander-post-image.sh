#!/bin/sh
sed -i '' '5i\
  <key>NSSupportsAutomaticGraphicsSwitching<\/key>\
  <true\/>
' build/barebones-commander.app/Contents/Info.plist 
