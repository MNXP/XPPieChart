package com.xp.xppiechart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.xp.xppiechart.Bean.CakeValue;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * PieChartView
 * Created by XP
 * 2016/7/12
 */
public class PieChartView extends View {

    DecimalFormat df = new DecimalFormat("#0.0");
    private int a = 1;
    private int screenW, screenH;
    /**
     * The paint to draw text, pie and line.
     */
    private Paint textPaint, piePaint, piePaintIn;

    private int width;
    private int widthXY;//下移的长度


    /**
     * The center and the radius of the pie.
     */
    private int pieCenterX, pieCenterY, pieRadius, pieRadiusIn;
    /**
     * The oval to draw the oval in.
     */
    private RectF pieOval;
    private RectF pieOvalIn;
    private float smallMargin;

    private List<CakeValue> mPieItems;
    private float totalValue;
    private static final int SWEEP_INC = 1;
    private int mSweep = 0;
    private int sweep = 0;

    public PieChartView(Context context) {
        super(context);
        init(context);
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        //init screen
        screenW = DensityUtils.getScreenWidth(context);
        screenH = DensityUtils.getScreenHeight(context);

        width = DensityUtils.dip2px(context, 15);
        widthXY = DensityUtils.dip2px(context, 10);

        pieCenterX = screenW / 2;
        pieCenterY = screenW / 3;
        pieRadius = screenW / 4;//大圆半径

        pieRadiusIn = screenW / 5;

        smallMargin = DensityUtils.dip2px(context, 5);

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

//        pieOvalIn.left = pieCenterX - pieRadiusIn;
//        pieOvalIn.top = pieCenterY - pieRadiusIn;
//        pieOvalIn.right = pieCenterX + pieRadiusIn;
//        pieOvalIn.bottom = pieCenterY + pieRadiusIn;

        //The paint to draw text.
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.TRANSPARENT);

        //The paint to draw circle.
        piePaint = new Paint();
        piePaint.setAntiAlias(true);
        piePaint.setStyle(Paint.Style.FILL);


        piePaintIn = new Paint();
        piePaintIn.setAntiAlias(true);
        piePaintIn.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPieItems != null && mPieItems.size() > 0) {
            mSweep = sweep + a;
            for (int i = 0; i < mPieItems.size(); i++) {
                piePaint.setColor(Color.parseColor(mPieItems.get(i).getColors()));
                canvas.drawArc(pieOval, mSweep, mPieItems.get(i).getSw(), true, piePaint);
                mSweep = mSweep + mPieItems.get(i).getSw() + a;
            }

        } else if (mPieItems != null && mPieItems.size() == 0) {
            piePaint.setColor(Color.parseColor("#fabd3b"));
            canvas.drawArc(pieOval, 0, 360, true, piePaint);
        }


        piePaintIn.setColor(Color.WHITE);
        canvas.drawArc(pieOvalIn, 0, 360, true, piePaintIn);

    }


    public void setData(List<CakeValue> pieItems) {
        List<CakeValue> list = new ArrayList<>();
        totalValue = 0;
        for (CakeValue item : pieItems) {
            totalValue += item.getItemValue();
        }
        for (int i = 0; i < pieItems.size(); i++) {
            if (pieItems.get(i).getItemValue() != 0) {
                list.add(pieItems.get(i));
            }
        }
        this.mPieItems = list;
        int num = 0;
        if (mPieItems.size() > 1) {
            num =  a * mPieItems.size();
        }
        for (int i = 0; i < mPieItems.size(); i++) {
            piePaint.setColor(Color.parseColor(mPieItems.get(i).getColors()));
            double s = mPieItems.get(i).getItemValue() / totalValue * (360 - num);
            int sweep = Integer.parseInt(new DecimalFormat("0").format(s));
            mPieItems.get(i).setSw(sweep);
        }
        invalidate();
    }


}
