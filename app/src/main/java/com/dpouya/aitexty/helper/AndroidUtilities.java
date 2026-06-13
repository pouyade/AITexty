package com.dpouya.aitexty.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.dpouya.aitexty.ApplicationLoader;

import java.util.ArrayList;


/**
 * Created by Pouya on 2/9/2017.
 */
public class AndroidUtilities {
    private static float density=0;
    public static int dp10=dp(10);
    public static int dp5=dp(5);
    public static int dp1=dp(1);
    public static int dp2=dp(2);
    public static int dp15=dp(15);
    public static int dp4=dp(4);
    public static float halfdp=dp((float) 0.5);
    public static int dp7=dp(7);
    public static int dp8=dp(8);
    public static int dp20 = dp(20);
    public static int dp25 =dp(25);
    public static int dp12 = dp(12);
    public static Context context;
    public static void init(Context context){
        AndroidUtilities.context=context;
    }
    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }
    public static int dp(float value) {
        if(density==0) {
            if(context==null){
                density =ApplicationLoader.getAppContext().getResources().getDisplayMetrics().density;
            }else {
                density = context.getResources().getDisplayMetrics().density;
            }
        }
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }
    public static int getWidth(Context context){
        Display display = ((Activity)context). getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ((Activity)context).getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dp(dpWidth);
    }
    public static float getWidthnodp(Context context){
        Display display = ((Activity)context). getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ((Activity)context).getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dpWidth;
    }
    public static int getHeight(Context context){
        Display display = ((Activity)context). getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ((Activity)context).getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dp(dpHeight);
    }
    public static float getHeightnodp(Context context){
        Display display = ((Activity)context). getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = ((Activity)context).getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return dpHeight;
    }

    public static <T> ArrayList<T> reverse(final ArrayList<T> list) {
        final int size = list.size();
        final int last = size - 1;

        // create a new list, with exactly enough initial capacity to hold the (reversed) list
        final ArrayList<T> result = new ArrayList<>(size);

        // iterate through the list in reverse order and append to the result
        for (int i = last; i >= 0; --i) {
            final T element = list.get(i);
            result.add(element);
        }

        // result now holds a reversed copy of the original list
        return result;
    }

    public static int pxtodp( Context context,float px) {
        return (int) (px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void ChangeGravitiy(View view, int gravity) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.gravity=gravity;
        view.setLayoutParams(layoutparams);
    }

    public static void ChangeMargin(View view, int left,int top,int right,int bottom) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.rightMargin=dp(right);
        layoutparams.topMargin=dp(top);
        layoutparams.leftMargin=dp(left);
        layoutparams.bottomMargin=dp(bottom);
        view.setLayoutParams(layoutparams);
    }

    public static void CloseSoftwareKeyboard(Activity context) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = context.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(context);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void ChangeMarginTop(View view, int margintop) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.topMargin=dp(margintop);
        view.setLayoutParams(layoutparams);
    }

    public static void ChangeMarginRight(View view,int marginright) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.rightMargin=dp(marginright);
        view.setLayoutParams(layoutparams);
    }

    public static void ChangeMarginLeft(View view,int marginleft) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.leftMargin=dp(marginleft);
        view.setLayoutParams(layoutparams);
    }

    public static void ChangeSize(View view,int width,int height) {
        FrameLayout.LayoutParams layoutparams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutparams.width=dp(width);
        layoutparams.height=dp(height);
        view.setLayoutParams(layoutparams);
    }
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
    public static float sp(float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics()
        );
    }
}