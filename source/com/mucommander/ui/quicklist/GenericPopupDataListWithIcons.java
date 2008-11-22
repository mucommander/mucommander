package com.mucommander.ui.quicklist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.mucommander.ui.quicklist.item.DataList;

/**
 * 
 * @author Arik Hadas
 */
public abstract class GenericPopupDataListWithIcons extends DataList {
	
	public abstract Icon getImageIconOfItem(final Object item);
	
	public GenericPopupDataListWithIcons() {
		super();
		setCellRenderer(new CellWithIconRenderer());
	}
	
	public GenericPopupDataListWithIcons(Object[] data) {
		super(data);
		setCellRenderer(new CellWithIconRenderer());
	}

	private class CellWithIconRenderer extends DefaultListCellRenderer {
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// Let superclass deal with most of it...
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// Add its icon
			Object item = list.getModel().getElementAt(index);
			Icon icon = getImageIconOfItem(item);
			setIcon(resizeIcon(icon));

			return this;
		}
		
		private Icon resizeIcon(Icon icon) {
			if (icon instanceof ImageIcon) {
				Image image = ((ImageIcon) icon).getImage();
				final Dimension dimension = this.getPreferredSize();
				final double height = dimension.getHeight();
				final double width = (height / icon.getIconHeight()) * icon.getIconWidth();
				image = image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH);
				return new ImageIcon(image);
			}

			return icon;
		}
	}
}