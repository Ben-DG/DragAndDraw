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
    private static final String EXTRA_ORIENTATION = "EXTRA_ORIENTATION";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;
    OrientationEventListener mOrientationEventListener;
    private int mCurrentOrientation;
    private boolean hasRotated = false;


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
        bundle.putInt(EXTRA_ORIENTATION, mCurrentOrientation);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        mBoxen = bundle.getParcelableArrayList(EXTRA_BOXEN);
        mCurrentOrientation = bundle.getInt(EXTRA_ORIENTATION);
        super.onRestoreInstanceState(bundle.getParcelable(EXTRA_SUPER));
        hasRotated = true; // better way to detect a screen rotation based view change?
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        if (hasRotated) { // So that this method isn't called every time we call invalidate()
            rotateBoxen();
            hasRotated = !hasRotated;
        }

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
                mCurrentBox = new Box(current, mCurrentOrientation, this.getWidth(),
                        this.getHeight());
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
            box.setScreenWidth(this.getWidth());
            box.setScreenHeight(this.getHeight());
        }
    }

    private void rotateBox(Box box, int rotation) {
        PointF origin = box.getOrigin();
        PointF current = box.getCurrent();

        switch (rotation) {
            case -270:
            case 90:
                origin.set(origin.y, box.getScreenWidth() - origin.x);
                current.set(current.y, box.getScreenWidth() - current.x);
                break;
            case 270:
            case -90:
                origin.set(box.getScreenHeight() - origin.y, origin.x);
                current.set(box.getScreenHeight() - current.y, current.x);
                break;
            case 180:
                origin.set(box.getScreenWidth() - origin.x, box.getScreenHeight() - origin.y);
                current.set(box.getScreenWidth() - current.x, box.getScreenHeight() - current.y);
                break;
            default:
                break;
        }

        box.setOrigin(origin);
        box.setCurrent(current);
    }
}
