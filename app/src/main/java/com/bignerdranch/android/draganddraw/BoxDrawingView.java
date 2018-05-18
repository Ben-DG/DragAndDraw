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

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            // Calculate the center of the rectangle
            float px = ( box.getCurrent().x + box.getOrigin().x ) / 2;
            float py = ( box.getCurrent().y + box.getOrigin().y ) / 2;

            canvas.rotate(box.getAngle(), px, py); // box.getAngle()
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.rotate( -1 * box.getAngle(), px, py );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
                mCurrentBox.setId(event.getPointerId(0));
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if ((event.getPointerCount() >= 2) && (mCurrentBox != null)) {
                    // 2nd finger detected
                    PointF current2 = new PointF(event.getX(1), event.getY(1));
                    mCurrentBox.setOrigin2(current2);
                    mCurrentBox.setId2(event.getPointerId(1));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                // Update rectangle coordinates
                if ((mCurrentBox != null) && (mCurrentBox.getId() == event.getPointerId(0))) {
                    mCurrentBox.setCurrent(current);
                    // 2nd finger
                    if (event.getPointerCount() >= 2) {
                        if (mCurrentBox.getId2() == event.getPointerId(1)) {
                            // 2nd finger detected
                            PointF current2 = new PointF(event.getX(1), event.getY(1));
                            mCurrentBox.setCurrent2(current2);
                            // Angle calculation
                            mCurrentBox.setAngle(calcAngle(mCurrentBox.getOrigin2(), mCurrentBox.getCurrent2()));
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP";
                if (mCurrentBox != null) {
                    Log.i(TAG, "Angle = " + mCurrentBox.getAngle());
                }
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

    /**
     *  The angle between the vertical segment from the center point and the segment
     *  from center to target0 and 360 degrees are North
     */
    public float calcAngle(PointF center, PointF target) {
        float angle = (float) Math.atan2(target.y - center.y, target.x - center.x);
        angle += Math.PI/2.0;
        // Translate to degrees
        angle = (float) Math.toDegrees(angle);

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }
}
