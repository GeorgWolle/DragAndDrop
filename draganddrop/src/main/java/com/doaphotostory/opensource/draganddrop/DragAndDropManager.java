/**
 * Copyright 2018 Georg Wollmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doaphotostory.opensource.draganddrop;

import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DRAG_ENDED;
import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DRAG_ENTERED;
import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DRAG_EXITED;
import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DRAG_LOCATION;
import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DRAG_STARTED;
import static com.doaphotostory.opensource.draganddrop.DragAndDropEvent.ACTION_DROP;

/**
 * DragAndDropManager is the Manager class to handle the Drag and Drop Events.
 * It's the entry point to make an Android View drag or droppable with a simple call
 * to DragAndDropManager.with({@link View}).
 * Uses {@link DragAndDropEvent} class to manage the DragAndDropEvents
 */
public class DragAndDropManager {

    // LOG TAG
    public static final String TAG = DragAndDropManager.class.getSimpleName();

    // Static holders for drag targets and entered droppables
    private static DragAndDroppable DRAG_TARGET;
    private static DragAndDroppable ENTERED_DROPPABLE = null;
    private static DragAndDroppable.OnDragListener ENTERED_DROPPABLE_LISTENER = null;

    // The hash map of the droppable objects and its listeners that will be iterated through
    // on every drag event
    private static ArrayList<DragAndDroppable> mDroppableListeners = new ArrayList<>();

    /**
     * The onDrag method is called if a drag on a {@link DragAndDroppable} has been started
     * and various {@link DragAndDropEvent}s occur.
     * It saves the current DRAG_TARGET and possible droppables that are hovered and informs
     * all registered droppables about {@link DragAndDropEvent}.
     * @param dragAndDroppable the {@link DragAndDroppable} that is being dragged
     * @param event the motion event of the drag
     */
    protected static void onDrag(DragAndDroppable dragAndDroppable, MotionEvent event) {
        DragAndDropEvent ev;
        if (DRAG_TARGET == null) {
            DRAG_TARGET = dragAndDroppable;
            ev = new DragAndDropEvent(event, ACTION_DRAG_STARTED);
        } else {
            // Check through all droppable listeners to see if the draggable is hovering
            // a droppable
            for (DragAndDroppable dragAndDroppableIterator : mDroppableListeners) {
                View view = dragAndDroppableIterator.getView();
                int[] a = new int[2];
                view.getLocationOnScreen(a);
                RectF bounds = new RectF(a[0], a[1], a[0] + view.getWidth(), a[1] + view.getHeight());
                // Check the bounds of the droppable
                if (bounds.contains((int) (event.getRawX()), (int) (event.getRawY()))) {
                    if(ENTERED_DROPPABLE_LISTENER != null)
                        return;
                    ENTERED_DROPPABLE_LISTENER = dragAndDroppableIterator.getOnDragListener();
                    ENTERED_DROPPABLE_LISTENER.onDrag(dragAndDroppable, new DragAndDropEvent(event, ACTION_DRAG_ENTERED));
                    ENTERED_DROPPABLE = dragAndDroppableIterator;
                    ENTERED_DROPPABLE.onDragHover();
                    return;
                }
                // If ENTERED_DROPPABLE_LISTENER != null we've been hovered but exited the area
                // (otherwise we would allready've been returned from this method) so fire the
                // ACTION_DRAG_EXITED EVENT
                if (ENTERED_DROPPABLE_LISTENER != null) {
                    ENTERED_DROPPABLE_LISTENER.onDrag(dragAndDroppable, new DragAndDropEvent(event, ACTION_DRAG_EXITED));
                    ENTERED_DROPPABLE_LISTENER = null;
                    ENTERED_DROPPABLE.onDragExit();
                }
            }
            // If we're still here it's a normal ACTION_DRAG_LOCATION
            ev = new DragAndDropEvent(event, ACTION_DRAG_LOCATION);
        }
        // Inform all Droppable Listeners about the Event
        for (DragAndDroppable dragAndDroppableIterator : mDroppableListeners) {
            dragAndDroppableIterator.getOnDragListener().onDrag(dragAndDroppable, ev);
        }
    }

    /**
     * The onDrop method is called when the drag has stopped (the user dropped the object)
     * The method checks wether the drop happens on hovering a droppable target and if yes
     * it informs by sending an ACTION_DROP.
     * However DRAG_TARGET is reset and all listeners are informed about the drop.
     * @param dragAndDroppable the {@link DragAndDroppable} that is being dropped
     * @param event the motion event of the drop
     */
    public static void onDrop(DragAndDroppable dragAndDroppable, MotionEvent event) {
        if (ENTERED_DROPPABLE_LISTENER != null) {
            ENTERED_DROPPABLE_LISTENER.onDrag(dragAndDroppable, new DragAndDropEvent((event), ACTION_DROP));
            ENTERED_DROPPABLE_LISTENER = null;
            ENTERED_DROPPABLE.onDragExit();
            ENTERED_DROPPABLE = null;
        }
        DRAG_TARGET = null;
        DragAndDropEvent ev = new DragAndDropEvent(event, ACTION_DRAG_ENDED);
        for (DragAndDroppable dragAndDroppableIterator : mDroppableListeners) {
            dragAndDroppableIterator.getOnDragListener().onDrag(dragAndDroppable, ev);
        }
    }

    /**
     * Droppables register with here to get {@link DragAndDropEvent} if a {@link DragAndDroppable} is
     * being dragged and maybe hovered over the droppable
     * @param dragAndDroppable the droppable to register for {@link DragAndDropEvent}
     */
    public static void addDroppableListener(DragAndDroppable dragAndDroppable) {
        mDroppableListeners.add(dragAndDroppable);
    }

    /**
     * Entry point to create a drag and, or droppable object from your
     * Android view
     * Just call DragAndDropManager.with({@link View}).makeDraggable() to make
     * your object draggable (.makeDroppable to make it droppable)
     * @param view the original view
     * @return a new {@link DragAndDroppable} object
     */
    public static DragAndDroppable with(View view) {
        return new DragAndDroppable(view);
    }
}
