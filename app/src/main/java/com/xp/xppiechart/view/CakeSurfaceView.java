package com.xp.xppiechart.view;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 饼图
 *
 * @author {acorn}
 */
public class CakeSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {
    private static final float ANGLE_NUM = 3.6f;
    private static final boolean isDrawByAnim = true;
    private List<String> ARC_COLORS = new ArrayList<String>();
    private Paint paint;
    /**
     * 起始角度
     */
    private float startAngle = 0;

    /**
     * 角数组
     */
    private List<CakeValue> cakeValues = new ArrayList<CakeValue>();

    /*************
     * 动画
     **********/
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
    private int drawCount = 0;
    /**
     * 动画持续时间
     */
    private static final int DURATION = 2000;

    private int pieCenterX, pieCenterY, pieRadius;
    private RectF pieOval;
    private RectF pieOvalIn;
    private int screenW;
    private int width;
    private int widthXY;// 下移的长度

    private Paint piePaintIn;

    public CakeSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CakeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
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

        width = DensityUtils.dip2px(context, 15);
        widthXY = DensityUtils.dip2px(context, 10);

        pieCenterX = screenW / 2;
        pieCenterY = screenW / 3;
        pieRadius = screenW / 4;// 大圆半径

        pieOval = new RectF();
        pieOval.left = pieCenterX - pieRadius;
        pieOval.top = pieCenterY - pieRadius + widthXY;
        pieOval.right = pieCenterX + pieRadius;
        pieOval.bottom = pieCenterY + pieRadius + widthXY;

        pieOvalIn = new RectF();
        pieOvalIn.left = pieOval.left + width;
        pieOvalIn.top = pieOval.top + width;
        pieOvalIn.right = pieOval.right - width;
        pieOvalIn.bottom = pieOval.bottom - width;

        piePaintIn = new Paint();
        piePaintIn.setAntiAlias(true);
        piePaintIn.setStyle(Paint.Style.FILL);
        piePaintIn.setColor(Color.parseColor("#f4f4f4"));
    }

    public void setData(List<CakeValue> cakes) {
        if (null != cakeValues) {
            // 初始化cakeValues;
            double sum = getSum(cakes);
            for (int i = 0; i < cakes.size(); i++) {
                if (cakes.get(i).value > 0) {
                    double value = 0;
                    value = cakes.get(i).value / sum * 100;
                    cakeValues.add(new CakeValue(cakes.get(i).itemType, value,
                            cakes.get(i).colors));
                    ARC_COLORS.add(cakes.get(i).colors);

                }
            }
            if (cakeValues.size() == 0) {
                cakeValues.add(new CakeValue("", 100, ""));
                ARC_COLORS.add("#FABD3B");
            }
            settleCakeValues(cakeValues.size() - 1);
            // 初始化itemframe
            itemFrame = new float[cakeValues.size()];
            for (int i = 0; i < cakeValues.size(); i++) {
                if (i == 0) {
                    itemFrame[i] = (float) cakeValues.get(i).value;
                    continue;
                }
                itemFrame[i] = (float) cakeValues.get(i).value
                        + itemFrame[i - 1];
            }
        }
    }

    private double getSum(List<CakeValue> mCakes) {
        double sum = 0;
        for (int i = 0; i < mCakes.size(); i++) {
            sum += mCakes.get(i).value;
        }
        return sum;
    }

    private float getSum(List<CakeValue> mCakes, int index) {
        float sum = 0;
        for (int i = 0; i < mCakes.size() && i < index; i++) {
            sum += mCakes.get(i).value;
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
            value.value = 100f - sum;
            cakeValues.set(i, value);
        } else {
            value.value = 0;
            settleCakeValues(i - 1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (drawCount == 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && isDrawByAnim) {
            drawCakeByAnim();
        }
        drawCount = 1;
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
                    canvas.drawArc(pieOval, startAngle,
                            (float) cakeValues.get(i).value * ANGLE_NUM, true,
                            paint);
                    continue;
                }
                // 如果越界(超过100,即360度),就不画了
                if (itemFrame[i - 1] >= 100
                        || cakeValues.get(i).value + itemFrame[i - 1] > 100) {
                    break;
                }
                canvas.drawArc(pieOval, startAngle + itemFrame[i - 1]
                        * ANGLE_NUM, (float) cakeValues.get(i).value
                        * ANGLE_NUM, true, paint);
            }

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
            for (int i = 0; i < curItem; i++) {
                paint.setColor(Color.parseColor(cakeValues.get(i).getColors()));
                if (i == 0) {
                    canvas.drawArc(pieOval, startAngle,
                            (float) cakeValues.get(i).value * ANGLE_NUM, true,
                            paint);

                    continue;
                }
                canvas.drawArc(pieOval, itemFrame[i - 1] * ANGLE_NUM,
                        (float) cakeValues.get(i).value * ANGLE_NUM, true,
                        paint);
            }
            curItem = getCurItem(curAngle);
            int colorIndex = curItem % ARC_COLORS.size();
            if (curItem == itemFrame.length - 1 && colorIndex == 0) {
                colorIndex = 0;
            }
            paint.setColor(Color.parseColor(ARC_COLORS.get(colorIndex)));
            float curStartAngle = 0;
            float curSweepAngle = curAngle;
            if (curItem > 0) {
                curStartAngle = itemFrame[curItem - 1] * ANGLE_NUM;
                curSweepAngle = curAngle - (itemFrame[curItem - 1] * ANGLE_NUM);
            }

            canvas.drawArc(pieOval, curStartAngle, curSweepAngle, true, paint);
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
        // Log.v("ts", "surface create");
        // 只有刚开始打开的时候播放,防止重播闪烁,当点击home键返回时,直接绘制.
        if (drawCount == 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && isDrawByAnim) {
        } else {
            drawCake();

        }

    }

    // 在surface的大小发生改变时触发
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cakeValueAnimator.cancel();
        startAngle = 0;
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
        cakeValueAnimator.setDuration(DURATION);
        cakeValueAnimator.setRepeatCount(0);
        cakeValueAnimator.setInterpolator(new DecelerateInterpolator());
        cakeValueAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    private float obj2Float(Object o) {
        return ((Number) o).floatValue();
    }

    public static class CakeValue {
        // 名称
        private String itemType;
        //
        private double value;
        private String colors;
        private int sw;

        public CakeValue(String itemType, double itemValue, String color) {
            this.itemType = itemType;
            this.value = itemValue;
            this.colors = color;
        }

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public double getItemValue() {
            return value;
        }

        public void setItemValue(double itemValue) {
            this.value = itemValue;
        }

        public String getColors() {
            return colors;
        }

        public void setColors(String color) {
            this.colors = color;
        }

        public int getSw() {
            return sw;
        }

        public void setSw(int sw) {
            this.sw = sw;
        }

    }
}
