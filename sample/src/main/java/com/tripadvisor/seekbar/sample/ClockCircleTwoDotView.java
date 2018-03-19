package com.tripadvisor.seekbar.sample;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by JessieChen on 17/1/24.
 */
public class ClockCircleTwoDotView extends View {
    private final static String TAG = ClockCircleTwoDotView.class.getSimpleName();

    public enum Status {
        FORWARD,
        BACK
    }

    public enum Cycle {
        FIRST,
        SECOND
    }

    private Bitmap bitmapClockBackgroundIcon;

    private Bitmap bitmapClockIcon;

    private static final int STATE_INIT = 1; //初始化

    private static final int STATE_ING = 2; //加载中

    private int MIN_SIZE;     //默认的宽高

    private float mCircleStrokeWidth;  //圆圈的厚度

    private int mCircleRadius; //圆圈的半径

    private float mDotRadius;    //圆点的半径

    private Interpolator mInterpolator; //圆点旋转的插值器

    private Paint circlePaint = new Paint(); //画笔

    private Paint circlePaintArc = new Paint(); //画笔

    private Bitmap bitmapStartTimeIcon;

    private float startTimeDotCenterX; //小圆点的x坐标

    private float startTimeDotCenterY; //小圆点的y坐标

    private Bitmap bitmapEndTimeIcon;

    private float endTimeDotCenterX; //小圆点的x坐标

    private float endTimeDotCenterY; //小圆点的y坐标

    private int clockCenterX; //圆心坐标

    private int clockCenterY;

    private boolean isStartTime = false;

    private boolean isEndTime = false;

    private double detectStartEndTimeDotRangeValue = 1.2;

    private float recordX;

    private float recordY;

    private double recordStartDotAngle = 0;

    private double recordStartDotCycleAngle = 0;

    private Status recordStartDotStatus;

    private Cycle recordStartDotCycle = Cycle.FIRST;

    private double recordEndDotAngle = 0;

    private double recordEndDotCycleAngle = 720;

    private Status recordEndDotStatus;

    private Cycle recordEndDotCycle = Cycle.SECOND;

    private double limitValue = 300;

    private double[] rangePositionArray = {0, 15, 30, 45,
            60, 75, 90, 105,
            120, 135, 150, 165,
            180, 195, 210, 225,
            240, 255, 270, 285,
            300, 315, 330, 345};

    private double[] calculatePositionArray = {0, 15, 30, 45,
            60, 75, 90, 105,
            120, 135, 150, 165,
            180, 195, 210, 225,
            240, 255, 270, 285,
            300, 315, 330, 345,
            360, 375, 390, 405,
            420, 435, 450, 465,
            480, 495, 510, 525,
            540, 555, 570, 585,
            600, 615, 630, 645,
            660, 675, 690, 705};

    public ClockCircleTwoDotView(Context context) {
        super(context);
        init();
    }

    public ClockCircleTwoDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockCircleTwoDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ClockCircleTwoDotView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mCircleStrokeWidth = getContext().getResources()
                .getDimension(R.dimen.circle_stroke_width); //圆圈的厚度和感叹号的厚度
        MIN_SIZE = DensityUtil
                .dip2px(getContext(), 72); //这里的dip2px方法就是简单的将72dp转换为本机对应的px,可以去网上随便搜一个
//        mDotRadius = getContext().getResources().getDimension(R.dimen.dot_size);
        mInterpolator = new AccelerateDecelerateInterpolator();

        circlePaint.setColor(getContext().getResources().getColor(R.color.circle_bg));
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(mCircleStrokeWidth);
        circlePaint.setStyle(Paint.Style.STROKE);

        circlePaintArc.setColor(getContext().getResources().getColor(R.color.dot_track_arc));
        circlePaintArc.setAntiAlias(true);
        circlePaintArc.setStrokeWidth(mCircleStrokeWidth);
        circlePaintArc.setStyle(Paint.Style.STROKE);

//        bitmapClockBackgroundIcon = ((BitmapDrawable) getResources()
//                .getDrawable(R.drawable.schedule_clock_bg)).getBitmap();
        bitmapStartTimeIcon = ((BitmapDrawable) getResources()
                .getDrawable(R.drawable.ic_schedule_timestart)).getBitmap();
        bitmapEndTimeIcon = ((BitmapDrawable) getResources()
                .getDrawable(R.drawable.ic_schedule_timeend)).getBitmap();
        mDotRadius = bitmapStartTimeIcon.getWidth() / 2;
        bitmapClockIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.schedule_clock))
                .getBitmap();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_SIZE, MIN_SIZE);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_SIZE, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, MIN_SIZE);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*注意,绘制的坐标是以当前View的左上角为圆点的,而不是当前View的坐标*/
        //圆心坐标计算
        clockCenterX = getWidth() / 2;
        clockCenterY = getHeight() / 2;

//        bitmapClockBackgroundIcon = getResizedBitmap(bitmapClockBackgroundIcon,
//                (int) (clockCenterX * 2 - mDotRadius)
//                , (int) (clockCenterY * 2 - mDotRadius));

        bitmapClockIcon = getResizedBitmap(bitmapClockIcon,
                (int) (clockCenterX * 2 - mDotRadius)
                , (int) (clockCenterY * 2 - mDotRadius));

        Log.d(TAG, "同心圓座標 clockCenterX: " + clockCenterX + " clockCenterY: " + clockCenterY);

        //圆圈的半径计算
        int radiusH = (getWidth() - getPaddingRight() - getPaddingLeft()) / 2 - (int) mDotRadius;
        int radiusV = (getHeight() - getPaddingBottom() - getPaddingTop()) / 2 - (int) mDotRadius;
        mCircleRadius = Math.min(radiusV, radiusH);

        Log.d(TAG, "radiusH: " + radiusH + " radiusV: " + radiusV + " mCircleRadius: "
                + mCircleRadius);

        //初始化小圆点位置坐标
        startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                .sin((recordStartDotCycleAngle * Math.PI / 180)));
        startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                .cos((recordStartDotCycleAngle * Math.PI / 180)));

        endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                .sin((recordEndDotCycleAngle / 2 * Math.PI / 180)));

        endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                .cos((recordEndDotCycleAngle / 2 * Math.PI / 180)));

        Log.d(TAG,
                "StartTime座標 startTimeDotCenterX: " + startTimeDotCenterX + " startTimeDotCenterY: "
                        + startTimeDotCenterY);
        Log.d(TAG, "StartTime座標 endTimeDotCenterX: " + endTimeDotCenterX + " endTimeDotCenterY: "
                + endTimeDotCenterY);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw" + " endTimeDotCenterX: " + endTimeDotCenterX + " endTimeDotCenterY: " + endTimeDotCenterY);

//        canvas.drawBitmap(bitmapClockBackgroundIcon, mDotRadius/2, mDotRadius/2, null);

        canvas.drawBitmap(bitmapClockIcon, mDotRadius/2, mDotRadius/2, null);

//        canvas.drawBitmap(bitmapClockIcon, mDotRadius, mDotRadius, null);

        canvas.drawCircle(clockCenterX, clockCenterY, mCircleRadius, circlePaint);

        float startAngle = (float) calculateCorrectionAngle(recordStartDotAngle);

        float endAngle = (float) calculateCorrectionAngle(recordEndDotAngle);

//        Log.d(TAG,"startAngle: " + startAngle + " sweepAngle: " + sweepAngle);

        float totalAngle = (float) (recordEndDotCycleAngle - recordStartDotCycleAngle);

        Log.d(TAG, "startAngle: " + startAngle + " endAngle: " + endAngle);

        Log.d(TAG, "startAngle totalAngle: " + totalAngle);

        if (totalAngle > 360) {
            if (startAngle < endAngle) {
                canvas.drawArc(clockCenterX - mCircleRadius,
                        clockCenterY - mCircleRadius,
                        ((2 * mCircleRadius) + mDotRadius),
                        ((2 * mCircleRadius) + mDotRadius),
                        (startAngle - 90),
                        (endAngle - startAngle),
                        false,
                        circlePaintArc);
                Log.d(TAG, "JC 1 startAngle: " + startAngle + " sweepAngle: " + (endAngle - startAngle));
            } else {
                canvas.drawArc(clockCenterX - mCircleRadius,
                        clockCenterY - mCircleRadius,
                        ((2 * mCircleRadius) + mDotRadius),
                        ((2 * mCircleRadius) + mDotRadius),
                        (endAngle - 90),
                        (startAngle - endAngle),
                        false,
                        circlePaintArc);
                Log.d(TAG, "JC 2 endAngle: " + endAngle + " sweepAngle: " + (startAngle - endAngle));
            }
        } else {
            if (startAngle > endAngle) {
                canvas.drawArc(clockCenterX - mCircleRadius,
                        clockCenterY - mCircleRadius,
                        ((2 * mCircleRadius) + mDotRadius),
                        ((2 * mCircleRadius) + mDotRadius),
                        (startAngle - 90),
                        360 - (startAngle - endAngle),
                        false,
                        circlePaintArc);
                Log.d(TAG, "JC 3 startAngle: " + startAngle + " sweepAngle: " + (startAngle - endAngle));
            } else {
                canvas.drawArc(clockCenterX - mCircleRadius,
                        clockCenterY - mCircleRadius,
                        ((2 * mCircleRadius) + mDotRadius),
                        ((2 * mCircleRadius) + mDotRadius),
                        (startAngle - 90),
                        (endAngle - startAngle),
                        false,
                        circlePaintArc);
                Log.d(TAG, "JC 4 startAngle: " + startAngle + " sweepAngle: " + (endAngle - startAngle));
            }
        }

        canvas.drawBitmap(bitmapStartTimeIcon,
                startTimeDotCenterX - (bitmapStartTimeIcon.getWidth() / 2),
                startTimeDotCenterY - (bitmapStartTimeIcon.getHeight() / 2),
                null);

        canvas.drawBitmap(bitmapEndTimeIcon,
                endTimeDotCenterX - (bitmapEndTimeIcon.getWidth() / 2),
                endTimeDotCenterY - (bitmapEndTimeIcon.getHeight() / 2),
                null);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public boolean onTouchEvent(MotionEvent event) {

//        Log.d(TAG,"onTouchEvent PointerId: " + event.getPointerId(event.getActionIndex()));

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                if (!isStartTime && !isEndTime) {

                    if(event.getX() >= (endTimeDotCenterX - mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getX()<= (endTimeDotCenterX + mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getY() >= (endTimeDotCenterY - mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getY() <= (endTimeDotCenterY + mDotRadius* detectStartEndTimeDotRangeValue)){
                        isEndTime = true;
                    }
                    else if(event.getX() >= (startTimeDotCenterX - mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getX()<= (startTimeDotCenterX + mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getY() >= (startTimeDotCenterY - mDotRadius* detectStartEndTimeDotRangeValue)
                            && event.getY() <= (startTimeDotCenterY + mDotRadius* detectStartEndTimeDotRangeValue)){
                        isStartTime = true;
                    }

                }

                Log.d(TAG, "ACTION_DOWN isStartTime: " + isStartTime + " isEndTime: " + isEndTime);
//                Log.d(TAG,"x: " + event.getX() + " y: " + event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isStartTime = false;
                isEndTime = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isStartTime = false;
                isEndTime = false;
                break;
            case MotionEvent.ACTION_MOVE:

                Log.d(TAG, "ACTION_MOVE isStartTime: " + isStartTime + " isEndTime: " + isEndTime);

                if (isStartTime) {

                    if (angle(recordX, recordY) - angle(event.getX(), event.getY()) > Math.PI
                            || angle(event.getX(), event.getY()) > angle(recordX, recordY)) {
                        recordStartDotStatus = Status.FORWARD;
                        Log.d(TAG, "Clock: 順時針");
                    } else {
                        recordStartDotStatus = Status.BACK;
                        Log.d(TAG, "Clock: 逆時針");
                    }
                    recordX = event.getX();
                    recordY = event.getY();

                    refreshStartDotPosition(event.getX() - clockCenterX,
                            event.getY() - clockCenterY);
                } else if (isEndTime) {

                    if (angle(recordX, recordY) - angle(event.getX(), event.getY()) > Math.PI
                            || angle(event.getX(), event.getY()) > angle(recordX, recordY)) {
                        recordEndDotStatus = Status.FORWARD;
                        Log.d(TAG, "Clock: 順時針");
                    } else {
                        recordEndDotStatus = Status.BACK;
                        Log.d(TAG, "Clock: 逆時針");
                    }
                    recordX = event.getX();
                    recordY = event.getY();

                    refreshEndDotPosition(event.getX() - clockCenterX, event.getY() - clockCenterY);
                }

                invalidate();//更新
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP isStartTime: " + isStartTime + " isEndTime: " + isEndTime);

                if (isStartTime) {
                    if (recordStartDotCycleAngle == 0) {
                        startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                                .sin((recordStartDotCycleAngle * Math.PI / 180)));
                        startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                                .cos((recordStartDotCycleAngle * Math.PI / 180)));
                    } else if (recordStartDotCycleAngle == 720) {
                        startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                                .sin((recordStartDotCycleAngle / 2 * Math.PI / 180)));
                        startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                                .cos((recordStartDotCycleAngle / 2 * Math.PI / 180)));
                    } else {
                        correctionStartDotLastPosition(event.getX() - clockCenterX,
                                event.getY() - clockCenterY);
                    }
                    callbackStartTimeAngle();
                } else if (isEndTime) {
                    if (recordEndDotCycleAngle == 0) {
                        endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                                .sin((recordEndDotCycleAngle * Math.PI / 180)));
                        endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                                .cos((recordEndDotCycleAngle * Math.PI / 180)));
                    } else if (recordEndDotCycleAngle == 720) {
                        endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                                .sin((recordEndDotCycleAngle / 2 * Math.PI / 180)));
                        endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                                .cos((recordEndDotCycleAngle / 2 * Math.PI / 180)));
                    } else {
                        correctionEndDotLastPosition(event.getX() - clockCenterX,
                                event.getY() - clockCenterY);
                    }
                    callbackEndTimeAngle();
                }
                isStartTime = false;
                isEndTime = false;
                invalidate();//更新
                break;
        }
        return true;
    }

    public double angle(double x1, double y1) {
        return Math.atan2(x1 - clockCenterX, clockCenterY - y1);
    }

    private double calculateCorrectionAngle(double realAngle) {
        boolean isNotCheckFinish = false;

        Log.d(TAG, "calculateCorrectionAngle realAngle: " + realAngle);

        for (int index = 0; index < rangePositionArray.length - 1; index++) {

            if (realAngle > rangePositionArray[index] && realAngle <= rangePositionArray[index
                    + 1]) {
                if ((index + 1) % 2 == 0) {
                    realAngle = rangePositionArray[index + 1];
                } else {
                    realAngle = rangePositionArray[index];
                }
                isNotCheckFinish = true;
            }
        }

        if (!isNotCheckFinish && realAngle > rangePositionArray[rangePositionArray.length - 1]) {
            realAngle = 360;
        }

        return realAngle;
    }

    private double calculateCorrectionAngle(double realAngle, Cycle cycle) {
        boolean isNotCheckFinish = false;

        if (cycle.equals(Cycle.SECOND) && realAngle <= 360) {
            realAngle += 360;
        }

        for (int index = 0; index < calculatePositionArray.length - 1; index++) {

            if (realAngle > calculatePositionArray[index] && realAngle <= calculatePositionArray[
                    index + 1]) {
                if ((index + 1) % 2 == 0) {
                    realAngle = calculatePositionArray[index + 1];
                } else {
                    realAngle = calculatePositionArray[index];
                }
                isNotCheckFinish = true;
            }
        }

        if (!isNotCheckFinish && realAngle > calculatePositionArray[calculatePositionArray.length
                - 1]) {
            realAngle = 720;
        }

        return realAngle;
    }

    private void correctionStartDotLastPosition(float x, float y) {
        double realAngle = Math.atan2(x, -y);

        if (realAngle < 0) {
            realAngle += 2 * Math.PI;
        }

        realAngle = Math.toDegrees(realAngle);

        Log.d(TAG, "correctionEndDotLastPosition realAngle(before): " + realAngle);

        realAngle = calculateCorrectionAngle(realAngle);

        Log.d(TAG, "correctionStartDotLastPosition realAngle(after): " + realAngle);

        startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                .sin((realAngle * Math.PI / 180)));
        startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                .cos((realAngle * Math.PI / 180)));
    }

    private void correctionEndDotLastPosition(float x, float y) {
        double realAngle = Math.atan2(x, -y);

        if (realAngle < 0) {
            realAngle += 2 * Math.PI;
        }

        realAngle = Math.toDegrees(realAngle);

        Log.d(TAG, "correctionEndDotLastPosition realAngle(before): " + realAngle);

        realAngle = calculateCorrectionAngle(realAngle);

        Log.d(TAG, "correctionEndDotLastPosition realAngle(after): " + realAngle);

        endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                .sin((realAngle * Math.PI / 180)));
        endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                .cos((realAngle * Math.PI / 180)));
    }

    private void refreshStartDotPosition(float x, float y) {

        double realAngle = (float) Math.atan2(x, -y);

        if (realAngle < 0) {
            realAngle += 2 * Math.PI;
        }

        Log.d(TAG, "Start time dot angle: " + Math.toDegrees(realAngle));

        double realcalculateAngle = calculateCorrectionAngle(Math.toDegrees(realAngle),
                recordStartDotCycle);

        Log.d(TAG, "Start time dot calculateCorrectionAngle: " + realcalculateAngle);

        if (recordStartDotCycleAngle == 0) {
            if (recordStartDotStatus.equals(Status.BACK)) {
                startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                        .sin((recordStartDotCycleAngle * Math.PI / 180)));
                startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                        .cos((recordStartDotCycleAngle * Math.PI / 180)));
                Log.d(TAG, "Start time dot recordStartDotCycleAngle == 0");
            } else {
                if (realcalculateAngle > 0 && realcalculateAngle < 90) {
                    recordStartDotAngle = Math.toDegrees(realAngle);
                    recordStartDotCycleAngle = calculateCorrectionAngle(recordStartDotAngle,
                            recordStartDotCycle);
                    Log.d(TAG, "Start time dot angle(record): " + recordStartDotAngle
                            + " recordStartDotCycleAngle: " + recordStartDotCycleAngle);

                    startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                            .sin(realAngle));
                    startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                            .cos(realAngle));
                }
            }
        } else if (recordStartDotCycleAngle == 720) {
            if (recordStartDotStatus.equals(Status.FORWARD)) {
                startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.
                        sin((recordStartDotCycleAngle / 2 * Math.PI / 180)));
                startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.
                        cos((recordStartDotCycleAngle / 2 * Math.PI / 180)));
                Log.d(TAG, "Start time dot recordStartDotCycleAngle == 720");
            } else {
                if (realcalculateAngle < 720 && realcalculateAngle > 630) {
                    recordStartDotAngle = Math.toDegrees(realAngle);
                    recordStartDotCycleAngle = calculateCorrectionAngle(recordStartDotAngle,
                            recordStartDotCycle);
                    Log.d(TAG, "Start time dot angle(record): " + recordStartDotAngle
                            + " recordStartDotCycleAngle: " + recordStartDotCycleAngle);

                    startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                            .sin(realAngle));
                    startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                            .cos(realAngle));
                }
            }
        } else {

            if (recordStartDotAngle > Math.toDegrees(realAngle)) {

                if (recordStartDotAngle - Math.toDegrees(realAngle) > limitValue) {
                    if (recordStartDotCycle.equals(Cycle.SECOND)) {
                        recordStartDotCycle = Cycle.FIRST;
                    } else {
                        recordStartDotCycle = Cycle.SECOND;
                    }
                }

                Log.d(TAG, "Start time dot cycle: " + recordStartDotCycle + " : "
                        + recordStartDotAngle);
            } else {

                if (Math.toDegrees(realAngle) - recordStartDotAngle > limitValue) {
                    if (recordStartDotCycle.equals(Cycle.FIRST)) {
                        recordStartDotCycle = Cycle.SECOND;
                    } else {
                        recordStartDotCycle = Cycle.FIRST;
                    }
                }

                Log.d(TAG, "Start time dot cycle: " + recordStartDotCycle + " : "
                        + recordStartDotAngle);
            }

            if (calculateCorrectionAngle(Math.toDegrees(realAngle), recordStartDotCycle)
                    < recordEndDotCycleAngle) {

                recordStartDotAngle = Math.toDegrees(realAngle);
                recordStartDotCycleAngle = calculateCorrectionAngle(recordStartDotAngle,
                        recordStartDotCycle);
                Log.d(TAG, "Start time dot angle(record): " + recordStartDotAngle
                        + " recordStartDotCycleAngle: " + recordStartDotCycleAngle);

                startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.sin(realAngle));
                startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.cos(realAngle));
            } else {

                startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.
                        sin(Math.toRadians(recordEndDotCycleAngle - 30)));
                startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.
                        cos(Math.toRadians(recordEndDotCycleAngle - 30)));

                if (recordStartDotCycle.equals(Cycle.SECOND)) {
                    recordStartDotAngle =
                            Math.toDegrees(Math.toRadians(recordEndDotCycleAngle - 30)) - 360;
                } else {
                    recordStartDotAngle = Math
                            .toDegrees(Math.toRadians(recordEndDotCycleAngle - 30));
                }

                recordStartDotCycleAngle = calculateCorrectionAngle(recordStartDotAngle,
                        recordStartDotCycle);

                Log.d(TAG, "JC Start time dot > end time dot end: " + recordEndDotCycleAngle +
                        " start: " + recordStartDotCycleAngle + " : " + recordStartDotAngle);

                isStartTime = false;
                isEndTime = false;
            }
        }
        callbackStartTimeAngle();
    }

    private void refreshEndDotPosition(float x, float y) {

        double realAngle = (float) Math.atan2(x, -y);

        if (realAngle < 0) {
            realAngle += 2 * Math.PI;
        }

        Log.d(TAG, "End time dot angle: " + Math.toDegrees(realAngle));

        double realcalculateAngle = calculateCorrectionAngle(Math.toDegrees(realAngle),
                recordEndDotCycle);

        Log.d(TAG, "End time dot calculateCorrectionAngle: " + realcalculateAngle);

        if (recordEndDotCycleAngle == 0) {
            if (recordEndDotStatus.equals(Status.BACK)) {
                endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                        .sin((recordEndDotCycleAngle * Math.PI / 180)));
                endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                        .cos((recordEndDotCycleAngle * Math.PI / 180)));
                Log.d(TAG, "End time dot recordStartDotCycleAngle == 0");
            } else {
                if (realcalculateAngle > 0 && realcalculateAngle < 90) {
                    recordEndDotAngle = Math.toDegrees(realAngle);
                    recordEndDotCycleAngle = calculateCorrectionAngle(recordEndDotAngle,
                            recordEndDotCycle);
                    Log.d(TAG, "End time dot angle(record): " + recordEndDotAngle
                            + " recordEndDotCycleAngle: " + recordEndDotCycleAngle);

                    endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                            .sin(realAngle));
                    endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                            .cos(realAngle));
                }
            }
        } else if (recordEndDotCycleAngle == 720) {
            if (recordEndDotStatus.equals(Status.FORWARD)) {
                endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.
                        sin((recordEndDotCycleAngle / 2 * Math.PI / 180)));
                endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.
                        cos((recordEndDotCycleAngle / 2 * Math.PI / 180)));
                Log.d(TAG, "End time dot recordStartDotCycleAngle == 720");
            } else {
                if (realcalculateAngle < 720 && realcalculateAngle > 630) {
                    recordEndDotAngle = Math.toDegrees(realAngle);
                    recordEndDotCycleAngle = calculateCorrectionAngle(recordEndDotAngle,
                            recordEndDotCycle);
                    Log.d(TAG, "End time dot angle(record): " + recordEndDotAngle
                            + " recordEndDotCycleAngle: " + recordEndDotCycleAngle);

                    endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                            .sin(realAngle));
                    endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                            .cos(realAngle));
                }
            }
        } else {

            if (recordEndDotAngle > Math.toDegrees(realAngle)) {

                if (recordEndDotAngle - Math.toDegrees(realAngle) > limitValue) {
                    if (recordEndDotCycle.equals(Cycle.SECOND)) {
                        recordEndDotCycle = Cycle.FIRST;
                    } else {
                        recordEndDotCycle = Cycle.SECOND;
                    }
                }
                Log.d(TAG, "End time dot cycle: " + recordEndDotCycle + " : " + recordEndDotAngle);
            } else {

                if (Math.toDegrees(realAngle) - recordEndDotAngle > limitValue) {
                    if (recordEndDotCycle.equals(Cycle.FIRST)) {
                        recordEndDotCycle = Cycle.SECOND;
                    } else {
                        recordEndDotCycle = Cycle.FIRST;
                    }
                }
                Log.d(TAG, "End time dot cycle: " + recordEndDotCycle + " : " + recordEndDotAngle);
            }

            if (calculateCorrectionAngle(Math.toDegrees(realAngle), recordEndDotCycle)
                    > recordStartDotCycleAngle) {

                recordEndDotAngle = Math.toDegrees(realAngle);
                recordEndDotCycleAngle = calculateCorrectionAngle(recordEndDotAngle,
                        recordEndDotCycle);
                Log.d(TAG, "End time dot angle(record): " + recordEndDotAngle
                        + " recordStartDotCycleAngle: " + recordEndDotCycleAngle);

                endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.sin(realAngle));
                endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.cos(realAngle));
            } else {

                endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                        .sin(Math.toRadians(recordStartDotCycleAngle + 30)));
                endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                        .cos(Math.toRadians(recordStartDotCycleAngle + 30)));

                if (recordEndDotCycle.equals(Cycle.SECOND)) {
                    recordEndDotAngle =
                            Math.toDegrees(Math.toRadians(recordStartDotCycleAngle + 30)) - 360;
                } else {
                    recordEndDotAngle = Math
                            .toDegrees(Math.toRadians(recordStartDotCycleAngle + 30));
                }
                recordEndDotCycleAngle = calculateCorrectionAngle(recordEndDotAngle,
                        recordEndDotCycle);

                Log.d(TAG, "JC End time dot > start time dot start: " + recordStartDotCycleAngle +
                        " end: " + recordEndDotCycleAngle + " : " + recordEndDotAngle);

                isStartTime = false;
                isEndTime = false;
            }
        }
        callbackEndTimeAngle();
    }


    /** Clock
     * set function
     */

    public void setInitClock(int aStartTime, int aEndTime) {

        recordStartDotCycleAngle = aStartTime*30;

        Log.d(TAG,"setInitClock start dot angle: " + recordStartDotCycleAngle);

        if(recordStartDotCycleAngle > 360) {
            recordStartDotCycle = Cycle.SECOND;
            recordStartDotAngle = recordStartDotCycleAngle - 360;
        }else {
            recordStartDotCycle = Cycle.FIRST;
            recordStartDotAngle = recordStartDotCycleAngle;
        }

        startTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math.
                sin(Math.toRadians(recordStartDotAngle)));
        startTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math.
                cos(Math.toRadians(recordStartDotAngle)));

        recordEndDotCycleAngle = aEndTime*30;

        Log.d(TAG,"setInitClock end dot angle: " + recordEndDotCycleAngle);

        if(recordEndDotCycleAngle > 360) {
            recordEndDotCycle = Cycle.SECOND;
            recordEndDotAngle = recordEndDotCycleAngle - 360;
        }else {
            recordEndDotCycle = Cycle.FIRST;
            recordEndDotAngle = recordEndDotCycleAngle;
        }

        endTimeDotCenterX = clockCenterX + (float) (mCircleRadius * Math
                .sin(Math.toRadians(recordEndDotAngle)));
        endTimeDotCenterY = clockCenterY - (float) (mCircleRadius * Math
                .cos(Math.toRadians(recordEndDotAngle)));

        isStartTime = false;
        isEndTime = false;

        invalidate();//更新
    }

    /** Clock
     * callback function
     */
    private ClockCallBackFunction mClockCallBackFunction;

    public void setClockCallBackFunction(ClockCallBackFunction aClockCallBackFunction)
    {
        mClockCallBackFunction = aClockCallBackFunction;
    }

    public interface ClockCallBackFunction {

        public void reFreshStatTimeFunction();

        public void reFreshEndTimeFunction();
    }

    private void callbackStartTimeAngle() {
        if (mClockCallBackFunction!=null) {
            ClockCircleTwoDotBean.setStartAngle(recordStartDotCycleAngle);
            mClockCallBackFunction.reFreshStatTimeFunction();
        }
    }

    private void callbackEndTimeAngle() {
        if (mClockCallBackFunction!=null) {
            ClockCircleTwoDotBean.setEndAngle(recordEndDotCycleAngle);
            mClockCallBackFunction.reFreshEndTimeFunction();
        }
    }

}
