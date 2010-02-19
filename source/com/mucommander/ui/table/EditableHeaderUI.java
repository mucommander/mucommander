/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.table;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

class EditableHeaderUI extends BasicTableHeaderUI {

	@Override
    protected MouseInputListener createMouseInputListener() {
		return new MouseInputHandler((EditableHeader) header);
	}

	public class MouseInputHandler extends BasicTableHeaderUI.MouseInputHandler {
		private Component dispatchComponent;

		protected EditableHeader header;

		public MouseInputHandler(EditableHeader header) {
			this.header = header;
		}

		private void setDispatchComponent(MouseEvent e) {
			Component editorComponent = header.getEditorComponent();
			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(header, p, editorComponent);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(
					editorComponent, p2.x, p2.y);
		}

		private boolean repostEvent(MouseEvent e) {
			if (dispatchComponent == null) {
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(header, e,
					dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			return true;
		}

		@Override
        public void mousePressed(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			super.mousePressed(e);

			if (header.getResizingColumn() == null) {
				Point p = e.getPoint();
				TableColumnModel columnModel = header.getColumnModel();
				int index = columnModel.getColumnIndexAtX(p.x);
				if (index != -1) {
					if (header.editCellAt(index, e)) {
						setDispatchComponent(e);
						repostEvent(e);
					}
				}
			}
		}

		@Override
        public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			repostEvent(e);
			dispatchComponent = null;
		}
	}
}
