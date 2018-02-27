package com.xp.xppiechart.view;

import android.content.Context;
import android.view.WindowManager;

public class DensityUtils {
    private static int screenWidth;
    private static int screenHeight;
	public static int dip2px(Context ctx, float dp) {
		float density = ctx.getResources().getDisplayMetrics().density;
		//dp = px/density
		int px = (int) (dp * density + 0.5f);
		return px;
	}

	public static float px2dip(Context ctx, int px) {
		float density = ctx.getResources().getDisplayMetrics().density;
		//dp = px/density
		float dp = px / density;
		return dp;
	}

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {

        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            screenWidth = wm.getDefaultDisplay().getWidth();
        }
        return screenWidth;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {

        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            screenHeight = wm.getDefaultDisplay().getHeight();
        }
        return screenHeight;
    }
}
