package com.mucommander.preferences.lookandfeel;

import java.awt.image.BufferedImage;

class LAFInfo {

    final String name;
    final String className;
    final BufferedImage image;

    LAFInfo(String name, String className, BufferedImage image) {
        this.name = name;
        this.className = className;
        this.image = image;
    }
}
