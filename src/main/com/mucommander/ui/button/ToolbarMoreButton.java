/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.button;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.ui.icon.IconManager;

/*
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

/**
 * Provides a way for toolbar buttons to be displayed when the toolbar itself doesn't have enough space for all buttons.
 * The buttons that are cropped are displayed in a secondary toolbar, using a vertical layout.
 * <p>
 * To use this Feature replace:
 * <code>
 *   frame.getContentPane().add(toolbar, BorderLayout.NORTH);
 * </code>
 * with 
 * <code>
 *   frame.getContentPane().add(MoreButton.wrapToolBar(toolBar), BorderLayout.NORTH);
 * </code>
 * </p>
 * <p>
 * This class is based on the code of Santhosh Kumar T, see
 * <a href="http://www.jroller.com/santhosh/entry/jtoolbar_with_more_button">this link</a> for more information.
 * </p>
 *
 * @author Santhosh Kumar T, Leo Welsch
 */
public class ToolbarMoreButton extends JToggleButton implements ActionListener {

  private static JToolBar moreToolbar;
  JToolBar toolbar;

  protected ToolbarMoreButton(final JToolBar toolbar) {
    super(IconManager.getIcon(IconManager.COMMON_ICON_SET, "more.png"));
    this.toolbar = toolbar;
    addActionListener(this);
    setFocusPainted(false);

    setMargin(new Insets(0, 0, 0, 0));
    setContentAreaFilled(false);
    setBorderPainted(false);
    // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard)
    if (OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrentOrHigher()) {
      putClientProperty("JComponent.sizeVariant", "small");
      putClientProperty("JButton.buttonType", "textured");
    }

    // paint border only when necessary
    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        setBorderPainted(false);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        setBorderPainted(true);
      }
    });

    // hide & seek
    toolbar.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        int nbToolbarComponents = toolbar.getComponentCount();

        final boolean aFlag = nbToolbarComponents>0 && !isVisible(toolbar.getComponent(nbToolbarComponents-1), null);
        setVisible(aFlag);
        moreToolbar.setVisible(aFlag);
      }
    });
  }

    // check visibility
  // partially visible is treated as not visible
  private boolean isVisible(Component comp, Rectangle rect) {
    if (rect == null) {
      rect = toolbar.getVisibleRect();
    }
    return comp.getLocation().x + comp.getWidth() <= rect.getWidth();
  }

  public void actionPerformed(ActionEvent e) {
    Component[] comp = toolbar.getComponents();
    Rectangle visibleRect = toolbar.getVisibleRect();
    for (int i = 0; i < comp.length; i++) {
      if (!isVisible(comp[i], visibleRect)) {
        JPopupMenu popup = new JPopupMenu();
        for (; i < comp.length; i++) {
          if (comp[i] instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) comp[i];
            if (button.getAction() != null) {
              popup.add(button.getAction());
            }
          } else if (comp[i] instanceof JSeparator) {
            popup.addSeparator();
          }
        }

        // on popup close make more-button unselected
        popup.addPopupMenuListener(new PopupMenuListener() {

          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            setSelected(false);
          }

          public void popupMenuCanceled(PopupMenuEvent e) {
          }

          public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          }
        });
        popup.show(this, 0, getHeight());
      }
    }
  }

  public static JPanel wrapToolBar(JToolBar toolbar) {
    moreToolbar = new JToolBar();
    moreToolbar.setRollover(true);
    moreToolbar.setFloatable(false);
    moreToolbar.add(new ToolbarMoreButton(toolbar));
    moreToolbar.setBorderPainted(false);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(toolbar, BorderLayout.CENTER);
    panel.add(moreToolbar, BorderLayout.EAST);

    return panel;
  }
}
