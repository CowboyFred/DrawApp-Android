package com.example.peka.asgmtpaint;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;
import java.util.Timer;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Peka on 09.03.2018.
 */

public class PaintView extends View {

    public static int BRUSH_SIZE = 20;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 1;
    public static final int MAX_FINGERS = 10;
    private Path[] mPaths = new Path[MAX_FINGERS];
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private ArrayList<FingerPath> redoPaths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int red,green,blue = 0;
    private int strokeWidth;
    private boolean emboss = false;
    private boolean blur = false;
    private boolean swag = false;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private int mToolId = 1; //Default value, 1 for the brush, 2 for the rect, 3 for the oval, 4 for the dot
    private float[] mX = new float[MAX_FINGERS];//Arrays for finger coordinates
    private float[] mY = new float[MAX_FINGERS];

    private Timer mTimer;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {

        //Basic setup for the painting layer
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        //Settings for the blur and emboss effects
        mEmboss = new EmbossMaskFilter(new float[] {1,1,1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL );

    }

    public void init(DisplayMetrics metrics)
    {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;


    }

    public void setColor (int color) {
        currentColor = color;
    }

    public void setTool (int toolId) { mToolId = toolId;}

    public void setEraser () {
        currentColor = backgroundColor;
        mToolId = 1;
    }

    public void undo () {
        if (paths.size() > 0 && redoPaths.size() < 10) //restrict undo actions to 10
        {
            redoPaths.add(paths.get(paths.size() -1 ));
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void redo () {
        if (redoPaths.size() > 0)
        {
            paths.add(redoPaths.get(redoPaths.size() - 1));
            redoPaths.remove(redoPaths.size() - 1);
            invalidate();
        }
    }


    public void setSize (int size) {
        strokeWidth = size;
    }

    public void normal(){
        emboss = false;
        blur = false;

    }

    public void emboss() {
        emboss = true;
        blur = false;
    }

    public void blur() {
        blur = true;
        emboss = false;
    }

    public void swagOn() {
        swag = true;
    }

    public  void  swagOff() {
        swag = false;
    }

    public void  swagColor() {
        if (swag) {
            Random random = new Random();


            red = red + random.nextInt(31) - 15;
            if (red >= 255 || red < 0) {
                red = 0;
            }
            green = green + random.nextInt(31) - 15;
            if (green >= 255 || green < 0) {
                green = 0;
            }
            blue = blue + random.nextInt(31) - 15;
            if (blue >= 255 || blue < 0) {
                blue = 0;
            }

            currentColor = Color.rgb(red, green, blue);
        }
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        redoPaths.clear();
        normal();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        for(FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss){
                mPaint.setMaskFilter(mEmboss);
            }

            else if (fp.blur) {
                mPaint.setMaskFilter(mBlur);
            }

            mCanvas.drawPath(fp.path, mPaint);


        }

        for (Path path: mPaths)
        {
            if (path != null) {
                mPaint.setColor(currentColor);
                mPaint.setStrokeWidth(strokeWidth);
                mPaint.setMaskFilter(null);

                if (emboss){
                    mPaint.setMaskFilter(mEmboss);
                }

                else if (blur) {
                    mPaint.setMaskFilter(mBlur);
                }

                mCanvas.drawPath(path, mPaint);
            }
        }


        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    //Detect the beginning point of drawing
    private void touchStart(float x, float y, int pointerId)
    {
        mPaths[pointerId] = new Path();
        mX[pointerId] = x;
        mY[pointerId] = y;

        switch (mToolId) {
            case 1:
                mPaths[pointerId].moveTo(mX[pointerId], mY[pointerId]);
                break;
            case 4:
                mPaths[pointerId].addCircle(mX[pointerId], mY[pointerId], strokeWidth/2, Path.Direction.CW);
                break;
        }

    }

    //Check the sensibility and make the drawing if finger is moving
    @TargetApi(21)
    private void touchMove (float x, float y, int pointerId)
    {
        float dx = Math.abs(x - mX[pointerId]);
        float dy = Math.abs(y - mY[pointerId]);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            switch (mToolId) {
                case 1:
                    mPaths[pointerId].quadTo(mX[pointerId], mY[pointerId], (x + mX[pointerId]) / 2, (y + mY[pointerId]) / 2);
                    mX[pointerId] = x;
                    mY[pointerId] = y;
                    break;
                case 2:
                    float startPointX, startPointY, endPointX, endPointY;
                    if (mX[pointerId] > x)
                    {
                        startPointX = x;
                        endPointX = mX[pointerId];
                    }
                    else
                    {
                        startPointX = mX[pointerId];
                        endPointX = x;
                    }
                    if (mY[pointerId] > y)
                    {
                        startPointY = y;
                        endPointY = mY[pointerId];
                    }
                    else
                    {
                        startPointY = mY[pointerId];
                        endPointY = y;
                    }
                    mPaths[pointerId].reset();
                    mPaths[pointerId].addRect(startPointX, startPointY, endPointX, endPointY, Path.Direction.CW);
                    break;
                case 3:
                    mPaths[pointerId].reset();
                    mPaths[pointerId].addOval(mX[pointerId], mY[pointerId], x, y, Path.Direction.CW);
                    break;
                case 4:
                    mX[pointerId] = x;
                    mY[pointerId] = y;
                    mPaths[pointerId].addCircle(mX[pointerId], mY[pointerId], strokeWidth/2, Path.Direction.CW);
                    FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPaths[pointerId]);
                    paths.add(fp);
                    mPaths[pointerId] = new Path();
                    break;
            }
        }
    }

    private void touchUp(int pointerId)
    {
        switch (mToolId) {
            case 1:
            mPaths[pointerId].lineTo(mX[pointerId], mY[pointerId]);
            break;
        }

        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPaths[pointerId]);
        paths.add(fp);
        mPaths[pointerId] = new Path();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerCount = event.getPointerCount();
        int cappedPointerCount = pointerCount > MAX_FINGERS ? MAX_FINGERS : pointerCount;
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);




        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && pointerId < MAX_FINGERS) {

            //Timer changes colors during holding and moving a finger
            if (swag && mTimer == null)
            {
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        swagColor();
                        invalidate();
                    }
                },0,10);
            }

            redoPaths.clear();
            touchStart(event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
        }

        else if (action == MotionEvent.ACTION_POINTER_UP && pointerId < MAX_FINGERS) {
            touchUp(pointerId);
            invalidate();
        }

        else if (action == MotionEvent.ACTION_UP && pointerId < MAX_FINGERS) {
            if (mTimer != null)
            {
                mTimer.cancel();
                mTimer = null;
            }
            touchUp(pointerId);
            invalidate();
        }

        else if ((action == MotionEvent.ACTION_MOVE) && pointerId < MAX_FINGERS) {
            for (int i = 0; i < cappedPointerCount; i++) {
                if (mPaths[i] != null) {
                    int index = event.findPointerIndex(i);
                    touchMove(event.getX(index), event.getY(index), event.getPointerId(index));
                    invalidate();
                }
            }
        }



        return true;
    }
}
