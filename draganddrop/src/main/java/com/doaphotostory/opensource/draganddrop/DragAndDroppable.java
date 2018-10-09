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

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * DragAndDroppable is an extension of {@link View}, providing a very easy use
 * of a Drag and Drop mechanism for every Android view.
 * Views can be made draggable or droppable (or both) and various methods may be called
 * to provide features for Drag and Dop.
 */
public class DragAndDroppable {

    // LOG TAG
    public static final String TAG = DragAndDroppable.class.getSimpleName();
    // SCREEN DENSITY
    private static float DENSITY = 0f;

    // EXCEPTIONS
    public static final String EXCEPTION_ILLEGAL_STATE_NO_IMAGE_VIEW = "On Hover Drawables may only be set on ImageViews";

    // The original Android View object
    private View mView;
    // Context
    private Context mContext;
    // On Drag Listener for Droppables
    private OnDragListener mOnDragListener;
    // Vibrator
    private Vibrator mVibrator;

    // For ImageView's the original drawable and an optional hover drawable may be set
    private Drawable mOriginalDrawable;

    // Boolean to set if it's a drag (it might be a click also)
    private boolean mDrag = false;

    // Instance fields for the Touch Process
    private int mMoverId = INVALID_POINTER_ID;
    private float mStartX, mStartY;
    private PointF mTouchPoint, mTouchPointStart;

    // Hover
    private Drawable mOnHoverDrawable;
    private int mOnHoverVibrateMs = -1;

    // Boolean indicators
    private boolean mIsDraggable, mIsDroppable;

    /**
     * ON DRAG LISTENER INTERFACE
     */
    public interface OnDragListener {
        boolean onDrag(DragAndDroppable view, DragAndDropEvent dragEvent);
    }

    /**
     * Constructs a DragAndDroppable Object for a specified View
     *
     * @param view The view which will be made drag and, or droppable
     */
    public DragAndDroppable(View view) {
        mView = view;
        mContext = mView.getContext();

        mView.setFocusable(true);
        mView.setFocusableInTouchMode(true);

        setDensity();

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isDraggable())
                    return false;
                // Get the index of the pointer associated with the action.
                int index = motionEvent.getActionIndex();
                int id = motionEvent.getPointerId(index);
                //SWITCH
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mMoverId = id;
                        createTouchPoint(motionEvent);
                        mTouchPointStart = mTouchPoint;
                        mStartX = mView.getX();
                        mStartY = mView.getY();
                        mView.requestFocus();
                        mView.setSelected(true);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float mx = mTouchPointStart.x - motionEvent.getRawX();
                        float my = mTouchPointStart.y - motionEvent.getRawY();
                        if (id != mMoverId) {
                            mMoverId = id;
                            createTouchPoint(motionEvent);
                        }
                        // Drag & Drop seems to be more stable if the object isn't dragged on every minimal touch
                        // so we check if we dragged more than the minimum drag distance of 5px
                        if (mDrag || (Math.abs(mx) > getPx(5) || Math.abs(my) > getPx(5))) {
                            DragAndDropManager.onDrag(DragAndDroppable.this, motionEvent);

                            float x = motionEvent.getRawX() * (1 / ((ViewGroup) mView.getParent()).getScaleX());
                            float y = motionEvent.getRawY() * (1 / ((ViewGroup) mView.getParent()).getScaleY());

                            float dx = x - mTouchPoint.x;
                            float dy = y - mTouchPoint.y;
                            createTouchPoint(motionEvent);

                            mView.setX(mView.getX() + dx);
                            mView.setY(mView.getY() + dy);

                            mDrag = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mDrag) {
                            DragAndDropManager.onDrop(DragAndDroppable.this, motionEvent);
                            mMoverId = INVALID_POINTER_ID;
                            mDrag = false;
                        }
                        return true;
                }
                return true;
            }
        });
    }

    /**
     * Creates a PointF for the raw touch point on the display
     *
     * @param event The Motion Event that happened on touch
     */
    public void createTouchPoint(MotionEvent event) {
        mTouchPoint = new PointF(
                event.getRawX() * (1 / ((ViewGroup) mView.getParent()).getScaleX()),
                event.getRawY() * (1 / ((ViewGroup) mView.getParent()).getScaleY()));
    }

    /**
     * Resets the position of the view to its origin
     */
    public void resetPosition() {
        mView.setX(mStartX);
        mView.setY(mStartY);
    }

    /**
     * Callback that is called on drag hover
     */
    public void onDragHover() {
        // Change the onHoverDrawable if set
        if (mOnHoverDrawable != null)
            ((ImageView) mView).setImageDrawable(mOnHoverDrawable);

        // If the onHoverVibration's not set or we did allready hover - return
        if (mOnHoverVibrateMs == -1)
            return;

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(mOnHoverVibrateMs, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            mVibrator.vibrate(mOnHoverVibrateMs);
        }
    }

    /**
     * Callback that is called on drag exit
     */
    public void onDragExit() {
        ((ImageView) mView).setImageDrawable(mOriginalDrawable);
    }

    /**
     * METHOD CHAINGING
     */
    /**
     * Make the object draggable
     *
     * @return this for method chaining
     */
    public DragAndDroppable makeDraggable() {
        makeDraggable(true);
        return this;
    }

    /**
     * Make the object draggable
     *
     * @param isDraggable wether the object should be draggable
     * @return this for method chaining
     */
    public DragAndDroppable makeDraggable(boolean isDraggable) {
        mIsDraggable = isDraggable;
        return this;
    }

    /**
     * Make the object droppable
     *
     * @return this for method chaining
     */
    public DragAndDroppable makeDroppable() {
        makeDroppable(true);
        return this;
    }

    /**
     * Make the object droppable
     *
     * @param isDroppable wether the object should be droppable
     * @return this for method chaining
     */
    public DragAndDroppable makeDroppable(boolean isDroppable) {
        mIsDroppable = isDroppable;
        return this;
    }

    /**
     * If the View is an ImageView an OnHoverDrawable may be set
     * which is shown onHover of the Droppable
     *
     * @param resID the onHover drawable
     * @return this for method chaining
     * @throws IllegalStateException If the original view is no ImageView IllegalStateException is thrown
     */
    public DragAndDroppable setOnHoverDrawable(int resID) throws IllegalStateException {
        if (!(mView instanceof ImageView))
            throw new IllegalStateException(EXCEPTION_ILLEGAL_STATE_NO_IMAGE_VIEW);
        mOriginalDrawable = ((ImageView) mView).getDrawable();
        mOnHoverDrawable = mContext.getResources().getDrawable(resID);
        return this;
    }

    /**
     * OnHoverHoverVibrate may be set in Milliseconds so that
     * the device vibrates on hovering over a droppable
     *
     * @param onHoverVibrateMs Milliseconds to vibrate
     * @return this for method chaining
     */
    public DragAndDroppable setOnHoverVibrate(int onHoverVibrateMs) {
        if (mVibrator == null)
            mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mOnHoverVibrateMs = onHoverVibrateMs;
        return this;
    }

    /**
     * SETTERS
     */
    /**
     * Set the density of the screen if not done allready
     */
    private void setDensity() {
        if (DENSITY != 0f)
            return;
        //DENSITY
        DENSITY = mView.getResources().getDisplayMetrics().density;
    }

    /**
     * Sets the on drag listener for this object
     *
     * @param onDragListener the on drag listener
     */
    public void setOnDragListener(OnDragListener onDragListener) {
        mOnDragListener = onDragListener;
        DragAndDropManager.addDroppableListener(this);
    }

    /**
     * ISSERS
     */
    /**
     * Is the Object draggable
     *
     * @return wether the object is draggable
     */
    public boolean isDraggable() {
        return mIsDraggable;
    }

    /**
     * Is the Object droppable
     *
     * @return wether the object is droppable
     */
    public boolean isDroppable() {
        return mIsDroppable;
    }

    /**
     * GETTERS
     */
    /**
     * Get the original Android View
     *
     * @return the original android view
     */
    public View getView() {
        return mView;
    }

    /**
     * Get the on drag listener
     * @return the on drag listener
     */
    public OnDragListener getOnDragListener() {
        return mOnDragListener;
    }

    /**
     * Converts DIPs to PX
     *
     * @param dip give in the dip
     * @return the pixels
     */
    public static int getPx(int dip) {
        return (int) (dip * DENSITY);
    }
}
