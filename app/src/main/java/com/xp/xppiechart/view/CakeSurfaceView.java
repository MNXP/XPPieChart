package com.xp.xppiechart.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;

import com.xp.xppiechart.Bean.CakeValue;
import com.xp.xppiechart.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 饼图
 */
public class CakeSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {
    private float ANGLE_NUM = 3.6f;
    private boolean isDrawByAnim = true;
    private boolean isSolid = true;
    private String defaultColor ="#FABD3B";
    private Paint paint;
    private List<CakeValue> cakeValues = new ArrayList<>();

    //绘制的角度

    private float curAngle;
    /**
     * 当前绘制的项
     */
    private int curItem = 0;
    /**
     * 所占百分比
     **/
    private float[] itemFrame;
    private SurfaceHolder holder = null;

    /**
     * 旋转展现动画
     */
    private ValueAnimator cakeValueAnimator;
    private boolean isFirst = true;
    /**
     * 动画持续时间
     */
    private int duration = 2000;


    private RectF pieOval;
    private RectF pieOvalWrite;
    private RectF pieOvalIn;
    private int screenW;

    private Paint piePaintIn;


    public CakeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int width = 20;
        int widthWrite = 5;
        int widthXY = 10;//微调距离
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CakeSurfaceView);
        if (typedArray != null) {
            width = typedArray.getInt(R.styleable.CakeSurfaceView_ringWidth,20);
            widthWrite = typedArray.getInt(R.styleable.CakeSurfaceView_solidWidth,5);
            widthXY = typedArray.getInt(R.styleable.CakeSurfaceView_fineTuningWidth,10);
            duration = typedArray.getInt(R.styleable.CakeSurfaceView_duration,2000);
            isSolid = typedArray.getBoolean(R.styleable.CakeSurfaceView_isSolid,true);
            isDrawByAnim = typedArray.getBoolean(R.styleable.CakeSurfaceView_isDrawByAnim,true);
            defaultColor = typedArray.getString(R.styleable.CakeSurfaceView_defaultColor);
        }
        if (defaultColor== null)
            defaultColor = "#FABD3B";
        width = DensityUtils.dip2px(context, width);
        widthWrite = DensityUtils.dip2px(context, widthWrite);
        widthXY = DensityUtils.dip2px(context, widthXY);
        // 使用渐减interpolator
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(false);
        this.setFocusable(true);
        this.setBackgroundColor(Color.parseColor("#00ffffff"));
        initValueAnimator();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        screenW = DensityUtils.getScreenWidth(context);


        int pieCenterX = screenW / 2;//饼状图中心X
        int pieCenterY = screenW / 3;//饼状图中心Y
        int pieRadius = screenW / 4;// 大圆半径

        //整个饼状图rect
        pieOval = new RectF();
        pieOval.left = pieCenterX - pieRadius;
        pieOval.top = pieCenterY - pieRadius + widthXY;
        pieOval.right = pieCenterX + pieRadius;
        pieOval.bottom = pieCenterY + pieRadius + widthXY;

        //里面的空白rect
        pieOvalIn = new RectF();
        pieOvalIn.left = pieOval.left + width;
        pieOvalIn.top = pieOval.top + width;
        pieOvalIn.right = pieOval.right - width;
        pieOvalIn.bottom = pieOval.bottom - width;


        //里面的空白rect
        pieOvalWrite = new RectF();
        pieOvalWrite.left = pieOvalIn.left - widthWrite;
        pieOvalWrite.top = pieOvalIn.top - widthWrite;
        pieOvalWrite.right = pieOvalIn.right + widthWrite;
        pieOvalWrite.bottom = pieOvalIn.bottom + widthWrite;

        //里面的空白画笔
        piePaintIn = new Paint();
        piePaintIn.setAntiAlias(true);
        piePaintIn.setStyle(Paint.Style.FILL);
        piePaintIn.setColor(Color.parseColor("#ffffff"));

        if (typedArray != null) {
            typedArray.recycle();
        }
    }

    public void setData(List<CakeValue> cakes) {
        if (null != cakeValues) {
            // 初始化cakeValues;
            double sum = getSum(cakes);
            for (int i = 0; i < cakes.size(); i++) {
                if (cakes.get(i).getItemValue() > 0) {
                    double value = 0;
                    value = cakes.get(i).getItemValue() / sum * 100;
                    cakeValues.add(new CakeValue(cakes.get(i).getItemType(), value,
                            cakes.get(i).getColors()));

                }
            }
            if (cakeValues.size() == 0) {
                cakeValues.add(new CakeValue("", 100, defaultColor));
            }
            settleCakeValues(cakeValues.size() - 1);
            // 初始化itemframe
            itemFrame = new float[cakeValues.size()];
            for (int i = 0; i < cakeValues.size(); i++) {
                if (i == 0) {
                    itemFrame[i] = (float) cakeValues.get(i).getItemValue();
                    continue;
                }
                itemFrame[i] = (float) cakeValues.get(i).getItemValue()
                        + itemFrame[i - 1];
            }
        }
    }

    private double getSum(List<CakeValue> mCakes) {
        double sum = 0;
        for (int i = 0; i < mCakes.size(); i++) {
            sum += mCakes.get(i).getItemValue();
        }
        return sum;
    }

    private float getSum(List<CakeValue> mCakes, int index) {
        float sum = 0;
        for (int i = 0; i < mCakes.size() && i < index; i++) {
            sum += mCakes.get(i).getItemValue();
        }
        return sum;
    }

    /**
     * 使用递归保证cakeValues的值的总和必为100
     *
     * @param i
     */
    private void settleCakeValues(int i) {
        float sum = getSum(cakeValues, i);
        CakeValue value = cakeValues.get(i);
        if (sum <= 100f) {
            value.setItemValue(100f - sum);
            cakeValues.set(i, value);
        } else {
            value.setItemValue(0);
            settleCakeValues(i - 1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (isFirst && isDrawByAnim) {
            drawCakeByAnim();
        }
        isFirst = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 动画绘制
     */
    private void drawCakeByAnim() {
        cakeValueAnimator.start();

    }

    /**
     * 绘制饼图
     */
    private void drawCake() {
        if (null == itemFrame)
            return;
        Canvas canvas = null;
        Rect lockRect = new Rect();
        pieOval.round(lockRect);
        canvas = holder.lockCanvas(lockRect);
        if (null != canvas) {
            canvas.drawRect(new Rect(0, 0, screenW, screenW), piePaintIn);
            for (int i = 0; i < cakeValues.size(); i++) {
                paint.setColor(Color.parseColor(cakeValues.get(i).getColors()));
                if (i == 0) {
                    canvas.drawArc(pieOval, 0,
                            (float) cakeValues.get(i).getItemValue() * ANGLE_NUM, true,
                            paint);
                    continue;
                }
                canvas.drawArc(pieOval, itemFrame[i - 1]
                        * ANGLE_NUM, (float) cakeValues.get(i).getItemValue()
                        * ANGLE_NUM, true, paint);
            }

            if (isSolid){
                piePaintIn.setColor(Color.parseColor("#66ffffff"));
                canvas.drawArc(pieOvalWrite, 0, 360, true, piePaintIn);
            }
            piePaintIn.setColor(Color.parseColor("#ffffff"));
            canvas.drawArc(pieOvalIn, 0, 360, true, piePaintIn);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawArc() {

        if (null == itemFrame)
            return;
        Canvas canvas = null;
        Rect lockRect = new Rect();
        pieOval.round(lockRect);
        canvas = holder.lockCanvas(lockRect);
        if (null != canvas) {
            canvas.drawRect(new Rect(0, 0, screenW, screenW), piePaintIn);
            curItem = getCurItem(curAngle);
            int colorIndex = curItem % cakeValues.size();
            if (curItem == itemFrame.length - 1 && colorIndex == 0) {
                colorIndex = 0;
            }
            paint.setColor(Color.parseColor(cakeValues.get(colorIndex).getColors()));
            float curStartAngle = 0;
            float curSweepAngle = curAngle;
            if (curItem > 0) {
                curStartAngle = itemFrame[curItem - 1] * ANGLE_NUM;
                curSweepAngle = curAngle - (itemFrame[curItem - 1] * ANGLE_NUM);
            }
            canvas.drawArc(pieOval, curStartAngle, curSweepAngle, true, paint);

            for (int i = 0; i < curItem; i++) {
                paint.setColor(Color.parseColor(cakeValues.get(i).getColors()));
                if (i == 0) {
                    canvas.drawArc(pieOval, 0,
                            (float) cakeValues.get(i).getItemValue() * ANGLE_NUM, true,
                            paint);

                    continue;
                }
                canvas.drawArc(pieOval, itemFrame[i - 1] * ANGLE_NUM,
                        (float) cakeValues.get(i).getItemValue() * ANGLE_NUM, true,
                        paint);
            }
            if (isSolid){
                piePaintIn.setColor(Color.parseColor("#66ffffff"));
                canvas.drawArc(pieOvalWrite, 0, 360, true, piePaintIn);
            }
            piePaintIn.setColor(Color.parseColor("#ffffff"));
            canvas.drawArc(pieOvalIn, 0, 360, true, piePaintIn);
            holder.unlockCanvasAndPost(canvas);

        }
    }


    /**
     * 获得当前绘制的饼.
     *
     * @param curAngle
     * @return
     */
    private int getCurItem(float curAngle) {
        int res = 0;
        for (int i = 0; i < itemFrame.length; i++) {
            if (curAngle <= itemFrame[i] * ANGLE_NUM) {
                res = i;
                break;
            }
        }
        return res;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isFirst || !isDrawByAnim)
            drawCake();
    }

    // 在surface的大小发生改变时触发
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cakeValueAnimator.cancel();
    }

    private void initValueAnimator() {

        PropertyValuesHolder angleValues = PropertyValuesHolder.ofFloat(
                "angle", 0f, 360f);
        cakeValueAnimator = ValueAnimator.ofPropertyValuesHolder(angleValues);
        cakeValueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mAngle = obj2Float(animation.getAnimatedValue("angle"));
                curAngle = mAngle;
                drawArc();
            }
        });
        cakeValueAnimator.setDuration(duration);
        cakeValueAnimator.setRepeatCount(0);
        cakeValueAnimator.setInterpolator(new DecelerateInterpolator());
        cakeValueAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    private float obj2Float(Object o) {
        return ((Number) o).floatValue();
    }
}
