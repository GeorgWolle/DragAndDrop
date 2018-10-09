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

import android.view.MotionEvent;

/**
 * DragAndDropEvent is a helper class that's closely related to Android's
 * {@link android.view.DragEvent}
 * See the {@link android.view.DragEvent} documentation to learn about the drag events
 */
public class DragAndDropEvent {

    // DRAG EVENTS
    public static final int ACTION_DRAG_STARTED = 0;
    public static final int ACTION_DRAG_LOCATION = 1;
    public static final int ACTION_DRAG_ENTERED = 2;
    public static final int ACTION_DRAG_EXITED = 3;
    public static final int ACTION_DROP = 4;
    public static final int ACTION_DRAG_ENDED = 5;

    // INSTANCE FIELD HOLDERS
    private MotionEvent mMotionEvent;
    private int mAction;

    /**
     * Constructs a simple DragAndDropEvent by supplying a motionEvent and an action
     * @param motionEvent the motion event that happened during the drag
     * @param action the drag action
     */
    protected DragAndDropEvent(MotionEvent motionEvent, int action) {
        mMotionEvent = motionEvent;
        mAction = action;
    }

    /**
     * GETTERS
     */
    /**
     * Get the action for this Event
     * @return action for this event
     */
    public int getAction() {
        return mAction;
    }

    /**
     * Get the motion event that happened for this drag event
     * @return the motion event for this drag event
     */
    public MotionEvent getMotionEvent() {
        return mMotionEvent;
    }

    /**
     * Overrides the toString method for debugging purposes
     * @return descriptive string for this object
     */
    @Override
    public String toString() {
        return "motionEvent " + mMotionEvent + " / action " + mAction;
    }
}
