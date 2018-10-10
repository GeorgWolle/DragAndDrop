package com.doaphotostory.opensource.draganddrop.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.doaphotostory.opensource.draganddrop.DragAndDropEvent;
import com.doaphotostory.opensource.draganddrop.DragAndDropManager;
import com.doaphotostory.opensource.draganddrop.DragAndDroppable;
import com.doaphotostory.opensource.draganddrop.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView greendroid = findViewById(R.id.greendroid);
        final ImageView bluedroid = findViewById(R.id.bluedroid);
        final ImageView trashcan = findViewById(R.id.trashcan);

        DragAndDropManager.with(greendroid)
                .makeDraggable();

        DragAndDropManager.with(bluedroid)
                .addOnHoverDrawable(R.id.trashcan, R.drawable.bluedroid_deny)
                .makeDraggable();

        DragAndDropManager.with(trashcan)
                .makeDroppable()
                .setOnHoverDrawable(R.drawable.trashcan_hover)
                .setOnHoverVibrate(15)
                .setOnDragListener(new DragAndDroppable.OnDragListener() {
                    @Override
                    public boolean onDrag(DragAndDroppable dragAndDroppable, DragAndDropEvent dragEvent) {
                        switch (dragEvent.getAction()) {
                            case DragAndDropEvent.ACTION_DROP:
                                if(dragAndDroppable.getView() == greendroid) {
                                    ((ViewGroup)greendroid.getParent()).removeView(greendroid);
                                } else {
                                    dragAndDroppable.resetPosition();
                                }
                                return true;
                        }
                        return true;
                    }
                });

    }
}
