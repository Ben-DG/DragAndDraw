package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private static final String EXTRA_SUPER = "EXTRA_SUPER";
    private static final String EXTRA_BOXEN = "EXTRA_BOXEN";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;
    OrientationEventListener mOrientationEventListener;
    private int mCurrentOrientation;


    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    // Used when inflating the view from XML
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Paint the boxes a nice semitransparent red (ARGB)
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);

        mOrientationEventListener =
                new OrientationEventListener(getContext(),SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged (int orientation)
            {
                mCurrentOrientation =  (90 * Math.round(orientation / 90)) % 360;
            }
        };

        if(mOrientationEventListener.canDetectOrientation())
        {
            mOrientationEventListener.enable();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_SUPER, super.onSaveInstanceState());
        bundle.putParcelableArrayList(EXTRA_BOXEN, (ArrayList<Box>) mBoxen);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        mBoxen = bundle.getParcelableArrayList(EXTRA_BOXEN);
        super.onRestoreInstanceState(bundle.getParcelable(EXTRA_SUPER));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        rotateBoxen();

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current, mCurrentOrientation);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }
        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }

    public void clearBoxen() {
        mBoxen.clear();
        invalidate();
    }

    private void rotateBoxen() {
        for (Box box : mBoxen) {
            int rotation = box.getOrientation() - mCurrentOrientation;
            box.setOrientation(mCurrentOrientation);
            rotateBox(box, rotation);
        }
    }

    private void rotateBox(Box box, int rotation) {
        Matrix m = new Matrix();
        m.setRotate(rotation);
    }
}
