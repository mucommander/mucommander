package com.mucommander.ui.dnd;

import com.mucommander.ui.FolderPanel;

/**
 * This class gives information about the context in which a drag-and-drop operation is being performed.
 * The getters are static since only one drag-and-drop operation can be performed at the same time. The information
 * returned by the getters is meaningful only when a drag-and-drop is being carried out.
 *
 * @see FileDragSourceListener
 * @author Maxence Bernard
 */
public class DnDContext {

    /** Has the drag operation been initiated by muCommander ? */
    private static boolean dragInitiatedByMucommander;

    /** FolderPanel instance which initiated the drag */
    private static FolderPanel dragInitiator;

    /** Current drag gesture modifiers */
    private static int dragGestureModifiersEx;


    /**
     * Returns <code>true<code> if the current drag has been initiated by muCommander, i.e. *not* by another application.
     * The returned value has a meaning only if a drag operation is currently being performed.
     */
    public static boolean isDragInitiatedByMucommander() {
        return dragInitiatedByMucommander;
    }

    /**
     * This method is called by {@link FileDragSourceListener}.
     */
    static void setDragInitiatedByMucommander(boolean b) {
        dragInitiatedByMucommander = b;
    }


    /**
     * Returns the {@link FolderPanel} instance that initiated the drag operation.
     * This method returns <code>null</code> if the current drag has not been initiated by muCommander.
     */
    public static FolderPanel getDragInitiator() {
        return dragInitiator;
    }

    /**
     * This method is called by {@link FileDragSourceListener}.
     */
    static void setDragInitiator(FolderPanel fp) {
        dragInitiator = fp;
    }


    /**
     * Returns the extended modifiers that are currently pressed while dragging.
     * This method returns <code>0</code> if the current drag has not been initiated by muCommander.
     */
    public static int getDragGestureModifiersEx() {
        return dragGestureModifiersEx;
    }
    
    /**
     * This method is called by {@link FileDragSourceListener}.
     */
    static void setDragGestureModifiersEx(int modifiersEx) {
        dragGestureModifiersEx = modifiersEx;

//        if(Debug.ON) Debug.trace("gestureModifiersEx="+modifiersEx);
//        if(Debug.ON) Debug.trace("getModifiersExText="+ InputEvent.getModifiersExText(modifiersEx));
//        if(Debug.ON) Debug.trace("is shift down="+((modifiersEx&InputEvent.SHIFT_DOWN_MASK)!=0));
//        if(Debug.ON) Debug.trace("is ctrl down="+((modifiersEx&InputEvent.CTRL_DOWN_MASK)!=0));
//        if(Debug.ON) Debug.trace("is alt down="+((modifiersEx&InputEvent.ALT_DOWN_MASK)!=0));
//        if(Debug.ON) Debug.trace("is meta down="+((modifiersEx&InputEvent.META_DOWN_MASK)!=0));
    }
}
