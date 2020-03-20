package com.mucommander.ui.macos;

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

import com.apple.laf.AquaTabbedPaneContrastUI;
import com.apple.laf.AquaTabbedPaneUI;

public class TabbedPaneUICustomizer {

	private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
	private static final Insets CONTENT_BORDER_INSETS = new Insets(5, 0, 0, 0);
	
	public static void customizeTabbedPaneUI(JTabbedPane tabbedPane) {
		TabbedPaneUI tabbedPaneUI = tabbedPane.getUI();
		
		if (tabbedPaneUI instanceof AquaTabbedPaneUI)
			tabbedPane.setUI(new CompactAquaTabbedPaneUI());
		else if (tabbedPaneUI instanceof AquaTabbedPaneContrastUI)
			tabbedPane.setUI(new CompactAquaTabbedPaneContrastUI());
	}
	
	private static class CompactAquaTabbedPaneUI extends AquaTabbedPaneUI {

		@Override
		protected Insets getContentBorderInsets(int arg0) {
			return CONTENT_BORDER_INSETS;
		}
		
		@Override
		protected Insets getTabAreaInsets(int arg0) {
			return EMPTY_INSETS;
		}
		
		@Override
		protected Insets getContentDrawingInsets(int arg0) {
			return EMPTY_INSETS;
		}

		/**
		 * No content border
		 */
		@Override
		protected void paintContentBorder(final Graphics g, final int tabPlacement, final int selectedIndex) {
		}
	}
	
	private static class CompactAquaTabbedPaneContrastUI extends AquaTabbedPaneContrastUI {

		@Override
		protected Insets getContentBorderInsets(int arg0) {
			return CONTENT_BORDER_INSETS;
		}

		@Override
		protected Insets getTabAreaInsets(int arg0) {
			return EMPTY_INSETS;
		}

		@Override
		protected Insets getContentDrawingInsets(int arg0) {
			return EMPTY_INSETS;
		}

		/**
		 * No content border
		 */
		@Override
		protected void paintContentBorder(final Graphics g, final int tabPlacement, final int selectedIndex) {
		}
	}
}
