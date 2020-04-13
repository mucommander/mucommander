/**
 * Copyright (c) 2006-2009, Alexander Potochkin
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the JXLayer project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icepdf.ri.util.jxlayer;

import org.icepdf.ri.util.jxlayer.plaf.LayerUI;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The universal decorator for Swing components
 * with which you can implement various advanced painting effects
 * as well as receive notification of all {@code MouseEvent}s,
 * {@code KeyEvent}s and {@code FocusEvent}s which generated within its borders.
 * <p/>
 * {@code JXLayer} delegates its painting and input events handling
 * to its {@link LayerUI} object which performs the actual decoration.
 * <p/>
 * The custom painting and events notification automatically work
 * for {@code JXLayer} itself and all its subcomponents.
 * This powerful combination makes it possible to enrich existing components
 * with new advanced functionality such as temporary locking of a hierarchy,
 * data tips for compound components, enhanced mouse scrolling etc...
 * <p/>
 * {@code JXLayer} is a great solution if you just need to do custom painting
 * over compound component or catch input events of its subcomponents.
 * <p/>
 * <pre>
 *         // create a component to be decorated with the layer
 *        JPanel panel = new JPanel();
 *        panel.add(new JButton("JButton"));
 *
 *        // This custom layerUI will fill the layer with translucent green
 *        // and print out all mouseMotion events generated within its borders
 *        AbstractLayerUI&lt;JPanel&gt; layerUI = new AbstractLayerUI&lt;JPanel&gt;() {
 *
 *            protected void paintLayer(Graphics2D g2, JXLayer&lt;JPanel&gt; l) {
 *                // this paints the layer as is
 *                super.paintLayer(g2, l);
 *                // fill it with the translucent green
 *                g2.setColor(new Color(0, 128, 0, 128));
 *                g2.fillRect(0, 0, l.getWidth(), l.getHeight());
 *            }
 *
 *            // overridden method which catches MouseMotion events
 *            protected void processMouseMotionEvent(MouseEvent e) {
 *                System.out.println("MouseMotionEvent detected: "
 *                        + e.getX() + " " + e.getY());
 *            }
 *        };
 *
 *        // create the layer for the panel using our custom layerUI
 *        JXLayer&lt;JPanel&gt; layer = new JXLayer&lt;JPanel&gt;(panel, layerUI);
 *
 *        // work with the layer as with any other Swing component
 *        frame.add(layer);
 * </pre>
 * <p/>
 * <b>Note:</b> When a {@code LayerUI} instance is disabled or not set,
 * its {@code JXLayer}s temporary lose all their decorations.
 * <b>Note:</b> {@code JXLayer} is very friendly to your application,
 * it uses only public Swing API and doesn't rely on any global settings
 * like custom {@code RepaintManager} or {@code AWTEventListener}.
 * It neither change the opaque state of its subcomponents
 * nor use the glassPane of its parent frame.
 * <p/>
 * {@code JXLayer} can be used under restricted environment
 * (e.g. unsigned applets)
 *
 * @see #setUI(LayerUI)
 * @see LayerUI
 */
@SuppressWarnings("serial")
public final class JXLayer<V extends Component> extends JComponent
        implements Scrollable, PropertyChangeListener, Accessible {
    private V view;
    // this field is necessary because JComponent.ui is transient
    // when layerUI is serializable
    private LayerUI<? super V> layerUI;
    private JPanel glassPane;
    private boolean isPainting;
    private static final DefaultLayerLayout sharedLayoutInstance =
            new DefaultLayerLayout();
    private long eventMask;

    private static final LayerEventController eventController =
            new LayerEventController();
    private static final long ACCEPTED_EVENTS =
            AWTEvent.COMPONENT_EVENT_MASK |
                    AWTEvent.CONTAINER_EVENT_MASK |
                    AWTEvent.FOCUS_EVENT_MASK |
                    AWTEvent.KEY_EVENT_MASK |
                    AWTEvent.MOUSE_WHEEL_EVENT_MASK |
                    AWTEvent.MOUSE_MOTION_EVENT_MASK |
                    AWTEvent.MOUSE_EVENT_MASK |
                    AWTEvent.INPUT_METHOD_EVENT_MASK |
                    AWTEvent.HIERARCHY_EVENT_MASK |
                    AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK;

    /**
     * Creates a new {@code JXLayer} object with empty view component
     * and empty {@link LayerUI}.
     *
     * @see #setView
     * @see #setUI
     */
    public JXLayer() {
        this(null);
    }

    /**
     * Creates a new {@code JXLayer} object with empty {@link LayerUI}.
     *
     * @param view the component to be decorated with this {@code JXLayer}
     * @see #setUI
     */
    public JXLayer(V view) {
        this(view, null);
    }

    /**
     * Creates a new {@code JXLayer} object with provided view component
     * and {@link LayerUI} object.
     *
     * @param view the component to be decorated
     * @param ui   the {@link LayerUI} delegate
     *             to be used by this {@code JXLayer}
     */
    public JXLayer(V view, LayerUI<V> ui) {
        super.setLayout(sharedLayoutInstance);
        setGlassPane(createGlassPane());
        setView(view);
        setUI(ui);
    }

    /**
     * Returns the {@code JXLayer}'s view component or {@code null}.
     * <br/>This is a bound property.
     *
     * @return the {@code JXLayer}'s view component
     * or {@code null} if none exists
     * @see #setView(Component)
     */
    public V getView() {
        return view;
    }

    /**
     * Sets the {@code JXLayer}'s view component, which can be {@code null}.
     * <br/>This is a bound property.
     *
     * @param view the view component for this {@code JXLayer}
     * @see #getView()
     */
    public void setView(V view) {
        Component oldView = getView();
        if (oldView != null) {
            super.remove(oldView);
        }
        if (view != null) {
            super.addImpl(view, null, getComponentCount());
        }
        this.view = view;
        firePropertyChange("view", oldView, view);
        revalidate();
        repaint();
    }

    /**
     * Sets the {@link LayerUI} which will perform painting
     * and receive input events for this {@code JXLayer}.
     *
     * @param ui the {@link LayerUI} for this {@code JXLayer}
     */
    public void setUI(LayerUI<? super V> ui) {
        this.layerUI = ui;
        super.setUI(ui);
    }

    /**
     * Returns the {@link LayerUI} for this {@code JXLayer}.
     *
     * @return the {@code LayerUI} for this {@code JXLayer}
     */
    public LayerUI<? super V> getUI() {
        return layerUI;
    }

    /**
     * Returns the {@code JXLayer}'s glassPane component or {@code null}.
     * <br/>This is a bound property.
     *
     * @return the {@code JXLayer}'s glassPane component
     * or {@code null} if none exists
     * @see #setGlassPane(JPanel)
     */
    public JPanel getGlassPane() {
        return glassPane;
    }

    /**
     * Sets the {@code JXLayer}'s glassPane component, which can be {@code null}.
     * <br/>This is a bound property.
     *
     * @param glassPane the glassPane component of this {@code JXLayer}
     * @see #getGlassPane()
     */
    public void setGlassPane(JPanel glassPane) {
        Container oldGlassPane = getGlassPane();
        if (oldGlassPane != null) {
            super.remove(oldGlassPane);
        }
        if (glassPane != null) {
            super.addImpl(glassPane, null, 0);
        }
        this.glassPane = glassPane;
        firePropertyChange("glassPane", oldGlassPane, glassPane);
        revalidate();
        repaint();
    }

    /**
     * Called by the constructor methods to create the default
     * {@code glassPane}.
     * By default this method creates a new {@code JPanel}
     * with visibility set to {@code true} and opacity set to {@code false}.
     *
     * @return the default {@code glassPane}
     */
    public JPanel createGlassPane() {
        return new DefaultLayerGlassPane();
    }

    /**
     * This method is not supported by {@code JXLayer}
     * and always throws {@code UnsupportedOperationException}
     *
     * @throws UnsupportedOperationException this method is not supported
     * @see #setView(Component)
     * @see #setGlassPane(JPanel)
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException(
                "Adding components to JXLayer is not supported, " +
                        "use setView() or setGlassPane() instead");
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Component comp) {
        if (comp == getView()) {
            setView(null);
        } else if (comp == getGlassPane()) {
            setGlassPane(null);
        } else {
            super.remove(comp);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeAll() {
        setView(null);
        setGlassPane(null);
    }

    /**
     * Delegates all painting to the {@link LayerUI} object.
     *
     * @param g the {@code Graphics} to render to
     */
    public void paint(Graphics g) {
        if (!isPainting && getUI() != null) {
            isPainting = true;
            super.paintComponent(g);
            isPainting = false;
        } else {
            super.paint(g);
        }
    }

    /**
     * This method is empty, because all painting is done by
     * {@link #paint(Graphics)} and
     * {@link LayerUI#update(Graphics, JComponent)} methods
     */
    protected void paintComponent(Graphics g) {
    }

    /**
     * To enable the correct painting of the glassPane and view component,
     * the {@code JXLayer} overrides the default implementation of
     * this method to return {@code false} when the glassPane is visible.
     *
     * @return false if {@code JXLayer}'s glassPane is visible
     */
    public boolean isOptimizedDrawingEnabled() {
        return !glassPane.isVisible();
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (getUI() != null) {
            getUI().handlePropertyChangeEvent(evt, this);
        }
    }

    /**
     * Sets the bitmask of event types to receive by this {@code JXLayer}.
     * Here is the list of the supported event types:
     * <ul>
     * <li>AWTEvent.COMPONENT_EVENT_MASK</li>
     * <li>AWTEvent.CONTAINER_EVENT_MASK</li>
     * <li>AWTEvent.FOCUS_EVENT_MASK</li>
     * <li>AWTEvent.KEY_EVENT_MASK</li>
     * <li>AWTEvent.MOUSE_WHEEL_EVENT_MASK</li>
     * <li>AWTEvent.MOUSE_MOTION_EVENT_MASK</li>
     * <li>AWTEvent.MOUSE_EVENT_MASK</li>
     * <li>AWTEvent.INPUT_METHOD_EVENT_MASK</li>
     * <li>AWTEvent.HIERARCHY_EVENT_MASK</li>
     * <li>AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK</li>
     * </ul>
     * <p/>
     * If {@code LayerUI} is installed,
     * {@link LayerUI#eventDispatched(AWTEvent, JXLayer)} method
     * will only receive events that match the event mask.
     * <p/>
     * Here is an example how to correclty use this method
     * in the {@code LayerUI} implementations:
     * <pre>
     *    public void installUI(JComponent c) {
     *       super.installUI(c);
     *       JXLayer l = (JXLayer) c;
     *       // this LayerUI will receive only key and focus events
     *       l.setLayerEventMask(AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
     *    }
     *
     *    public void uninstallUI(JComponent c) {
     *       super.uninstallUI(c);
     *       JXLayer l = (JXLayer) c;
     *       // JXLayer must be returned to its initial state
     *       l.setLayerEventMask(0);
     *    }
     * </pre>
     * <p/>
     * By default {@code JXLayer} receives no events.
     *
     * @param layerEventMask the bitmask of event types to receive
     * @throws IllegalArgumentException if the {@code layerEventMask} parameter
     *                                  contains unsupported event types
     * @see #getLayerEventMask()
     */
    public void setLayerEventMask(long layerEventMask) {
        if (layerEventMask != (layerEventMask & ACCEPTED_EVENTS)) {
            throw new IllegalArgumentException(
                    "The event bitmask contains unsupported event types");
        }
        long oldEventMask = getLayerEventMask();
        this.eventMask = layerEventMask;
        firePropertyChange("layerEventMask", oldEventMask, layerEventMask);
        if (layerEventMask != oldEventMask) {
            disableEvents(oldEventMask);
            enableEvents(eventMask);
            eventController.updateAWTEventListener(this);
        }
    }

    /**
     * Returns the bitmap of event mask to receive by this {@code JXLayer}
     * and its {@code LayerUI}.
     * <p/>
     * It means that {@link LayerUI#eventDispatched(AWTEvent, JXLayer)} method
     * will only receive events that match the event mask.
     * <p/>
     * By default {@code JXLayer} receives no events.
     *
     * @return the bitmask of event types to receive for this {@code JXLayer}
     */
    public long getLayerEventMask() {
        return eventMask;
    }

    /**
     * Delegates its functionality to the {@link LayerUI#updateUI(JXLayer)} method,
     * if {@code LayerUI} is set.
     */
    public void updateUI() {
        if (getUI() != null) {
            getUI().updateUI(this);
        }
    }

    /**
     * Returns the preferred size of the viewport for a view component.
     * <p/>
     * If the ui delegate of this layer is not null, this method delegates its
     * implementation to the {@code LayerUI.getPreferredScrollableViewportSize(JXLayer)}
     *
     * @return the preferred size of the viewport for a view component
     * @see Scrollable
     * @see org.icepdf.ri.util.jxlayer.plaf.LayerUI#getPreferredScrollableViewportSize(JXLayer)
     */
    public Dimension getPreferredScrollableViewportSize() {
        if (getUI() != null) {
            return getUI().getPreferredScrollableViewportSize(this);
        }
        return getPreferredSize();
    }

    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one block
     * of rows or columns, depending on the value of orientation.
     * <p/>
     * If the ui delegate of this layer is not null, this method delegates its
     * implementation to the {@code LayerUI.getScrollableBlockIncrement(JXLayer, Rectangle, int, int)}
     *
     * @return the "block" increment for scrolling in the specified direction
     * @see Scrollable
     * @see LayerUI#getScrollableBlockIncrement(JXLayer, Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation, int direction) {
        if (getUI() != null) {
            return getUI().getScrollableBlockIncrement(this, visibleRect,
                    orientation, direction);
        }
        return (orientation == SwingConstants.VERTICAL) ? visibleRect.height :
                visibleRect.width;
    }

    /**
     * Returns false to indicate that the height of the viewport does not
     * determine the height of the layer, unless the preferred height
     * of the layer is smaller than the viewports height.
     * <p/>
     * If the ui delegate of this layer is not null, this method delegates its
     * implementation to the {@code LayerUI.getScrollableTracksViewportHeight(JXLayer)}
     *
     * @return whether the layer should track the height of the viewport
     * @see Scrollable
     * @see LayerUI#getScrollableTracksViewportHeight(JXLayer)
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getUI() != null) {
            return getUI().getScrollableTracksViewportHeight(this);
        }
        if (getParent() instanceof JViewport) {
            return ((getParent()).getHeight() > getPreferredSize().height);
        }
        return false;
    }

    /**
     * Returns false to indicate that the width of the viewport does not
     * determine the width of the layer, unless the preferred width
     * of the layer is smaller than the viewports width.
     * <p/>
     * If the ui delegate of this layer is not null, this method delegates its
     * implementation to the {@code LayerUI.getScrollableTracksViewportWidth(JXLayer)}
     *
     * @return whether the layer should track the width of the viewport
     * @see Scrollable
     * @see LayerUI#getScrollableTracksViewportWidth(JXLayer)
     */
    public boolean getScrollableTracksViewportWidth() {
        if (getUI() != null) {
            return getUI().getScrollableTracksViewportWidth(this);
        }
        if (getParent() instanceof JViewport) {
            return ((getParent()).getWidth() > getPreferredSize().width);
        }
        return false;
    }

    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation.  Ideally,
     * components should handle a partially exposed row or column by
     * returning the distance required to completely expose the item.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a unit scroll.
     * <p/>
     * If the ui delegate of this layer is not null, this method delegates its
     * implementation to the {@code LayerUI.getScrollableUnitIncrement(JXLayer, Rectangle, int, int)}
     *
     * @return The "unit" increment for scrolling in the specified direction.
     * This value should always be positive.
     * @see Scrollable
     * @see LayerUI#getScrollableUnitIncrement(JXLayer, Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
                                          int direction) {
        if (getUI() != null) {
            return getUI().getScrollableUnitIncrement(
                    this, visibleRect, orientation, direction);
        }
        return 1;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (getUI() != null) {
            setUI(getUI());
        }
        if (getLayerEventMask() != 0) {
            eventController.updateAWTEventListener(this);
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }

    /**
     * static AWTEventListener to be shared with all AbstractLayerUIs
     */
    @SuppressWarnings("serial")
    private static class LayerEventController implements AWTEventListener {
        private ArrayList<WeakReference<JXLayer>> layerList =
                new ArrayList<WeakReference<JXLayer>>();

        private long currentEventMask;

        @SuppressWarnings("unchecked")
        public void eventDispatched(AWTEvent event) {
            Object source = event.getSource();
            if (source instanceof Component) {
                Component component = (Component) source;
                while (component != null) {
                    if (component instanceof JXLayer) {
                        JXLayer l = (JXLayer) component;
                        LayerUI ui = l.getUI();
                        if (ui != null &&
                                isEventEnabled(l.getLayerEventMask(),
                                        event.getID())) {
                            ui.eventDispatched(event, l);
                        }
                    }
                    component = component.getParent();
                }
            }
        }

        private boolean layerListContains(JXLayer l) {
            for (WeakReference<JXLayer> layerWeakReference : layerList) {
                if (layerWeakReference.get() == l) {
                    return true;
                }
            }
            return false;
        }

        private void updateAWTEventListener(JXLayer layer) {
            if (!layerListContains(layer) && layer.getLayerEventMask() != 0) {
                layerList.add(new WeakReference<JXLayer>(layer));
            }
            long combinedMask = 0;
            Iterator<WeakReference<JXLayer>> it = layerList.iterator();
            while (it.hasNext()) {
                WeakReference<JXLayer> weakRef = it.next();
                JXLayer currLayer = weakRef.get();
                if (currLayer == null) {
                    it.remove();
                } else {
                    combinedMask |= currLayer.getLayerEventMask();
                }
            }
            if (combinedMask == 0) {
                removeAWTEventListener();
                layerList.clear();
            } else if (getCurrentEventMask() != combinedMask) {
                removeAWTEventListener();
                addAWTEventListener(combinedMask);
            }
        }

        private long getCurrentEventMask() {
            return currentEventMask;
        }

        private void addAWTEventListener(final long eventMask) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    Toolkit.getDefaultToolkit().
                            addAWTEventListener(LayerEventController.this, eventMask);
                    return null;
                }
            });
            currentEventMask = eventMask;
        }

        private void removeAWTEventListener() {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    Toolkit.getDefaultToolkit().
                            removeAWTEventListener(LayerEventController.this);
                    return null;
                }
            });
            currentEventMask = 0;
        }

        private boolean isEventEnabled(long eventMask, int id) {
            return (((eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 &&
                    id >= ComponentEvent.COMPONENT_FIRST &&
                    id <= ComponentEvent.COMPONENT_LAST)
                    || ((eventMask & AWTEvent.CONTAINER_EVENT_MASK) != 0 &&
                    id >= ContainerEvent.CONTAINER_FIRST &&
                    id <= ContainerEvent.CONTAINER_LAST)
                    || ((eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0 &&
                    id >= FocusEvent.FOCUS_FIRST &&
                    id <= FocusEvent.FOCUS_LAST)
                    || ((eventMask & AWTEvent.KEY_EVENT_MASK) != 0 &&
                    id >= KeyEvent.KEY_FIRST &&
                    id <= KeyEvent.KEY_LAST)
                    || ((eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0 &&
                    id == MouseEvent.MOUSE_WHEEL)
                    || ((eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0 &&
                    (id == MouseEvent.MOUSE_MOVED ||
                            id == MouseEvent.MOUSE_DRAGGED))
                    || ((eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0 &&
                    id != MouseEvent.MOUSE_MOVED &&
                    id != MouseEvent.MOUSE_DRAGGED &&
                    id != MouseEvent.MOUSE_WHEEL &&
                    id >= MouseEvent.MOUSE_FIRST &&
                    id <= MouseEvent.MOUSE_LAST)
                    || ((eventMask & AWTEvent.INPUT_METHOD_EVENT_MASK) != 0 &&
                    id >= InputMethodEvent.INPUT_METHOD_FIRST &&
                    id <= InputMethodEvent.INPUT_METHOD_LAST)
                    || ((eventMask & AWTEvent.HIERARCHY_EVENT_MASK) != 0 &&
                    id == HierarchyEvent.HIERARCHY_CHANGED)
                    || ((eventMask & AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK) != 0 &&
                    (id == HierarchyEvent.ANCESTOR_MOVED ||
                            id == HierarchyEvent.ANCESTOR_RESIZED)));
        }
    }

    @SuppressWarnings("serial")
    private static class DefaultLayerLayout implements LayoutManager, Serializable {
        /**
         * {@inheritDoc}
         */
        public void layoutContainer(Container parent) {
            JXLayer layer = (JXLayer) parent;
            Component view = layer.getView();
            Component glassPane = layer.getGlassPane();
            if (view != null) {
                Insets insets = layer.getInsets();
                view.setLocation(insets.left, insets.top);
                view.setSize(layer.getWidth() - insets.left - insets.right,
                        layer.getHeight() - insets.top - insets.bottom);
            }
            if (glassPane != null) {
                glassPane.setLocation(0, 0);
                glassPane.setSize(layer.getWidth(), layer.getHeight());
            }
        }

        /**
         * {@inheritDoc}
         */
        public Dimension minimumLayoutSize(Container parent) {
            JXLayer layer = (JXLayer) parent;
            Insets insets = layer.getInsets();
            Dimension ret = new Dimension(insets.left + insets.right,
                    insets.top + insets.bottom);
            Component view = layer.getView();
            if (view != null) {
                Dimension size = view.getMinimumSize();
                ret.width += size.width;
                ret.height += size.height;
            }
            if (ret.width == 0 || ret.height == 0) {
                ret.width = ret.height = 4;
            }
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        public Dimension preferredLayoutSize(Container parent) {
            JXLayer layer = (JXLayer) parent;
            Insets insets = layer.getInsets();
            Dimension ret = new Dimension(insets.left + insets.right,
                    insets.top + insets.bottom);
            Component view = layer.getView();
            if (view != null) {
                Dimension size = view.getPreferredSize();
                if (size.width > 0 && size.height > 0) {
                    ret.width += size.width;
                    ret.height += size.height;
                }
            }
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        public void addLayoutComponent(String name, Component comp) {
        }

        /**
         * {@inheritDoc}
         */
        public void removeLayoutComponent(Component comp) {
        }
    }

    /**
     * The default glassPane for the {@link JXLayer}.
     * It is a subclass of {@code JPanel} which is non opaque by default.
     */
    @SuppressWarnings("serial")
    private static class DefaultLayerGlassPane extends JPanel {
        /**
         * Creates a new {@link DefaultLayerGlassPane}
         */
        public DefaultLayerGlassPane() {
            setOpaque(false);
        }

        /**
         * First, implementatation of this method iterates through
         * glassPane's child components and returns {@code true}
         * if any of them is visible and contains passed x,y point.
         * After that it checks if no mouseListeners is attached to this component
         * and no mouse cursor is set, then it returns {@code false},
         * otherwise calls the super implementation of this method.
         *
         * @param x the <i>x</i> coordinate of the point
         * @param y the <i>y</i> coordinate of the point
         * @return true if this component logically contains x,y
         */
        public boolean contains(int x, int y) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component c = getComponent(i);
                Point point = SwingUtilities.convertPoint(this, new Point(x, y), c);
                if (c.isVisible() && c.contains(point)) {
                    return true;
                }
            }
            if (getMouseListeners().length == 0
                    && getMouseMotionListeners().length == 0
                    && getMouseWheelListeners().length == 0
                    && !isCursorSet()) {
                return false;
            }
            return super.contains(x, y);
        }
    }
}
