package com.dpouya.aitexty.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;


import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    private static Context context;

    public enum TypeFaceKey{
        DEFAULT_TYPEFACE,
        DEFAULT_EN_TYPEFACE,
        READING_TYPEFACE,
        ACTIONBAR_TYPEFACE,
    }
    public enum DrawableKey{
        SELECTED_DRAWABLE,
        HOME_TAB_ICON,
        WORD_DECKS_TAB_ICON,
        EXAMS_TAB_ICON,
        SETTINGS_TAB_ICON,
        LEFT_ARROW,
        RIGHT_ARROW,
        MULTI_SELECT,
        SEARCH_DRAWABLE,
        CLOSE_SEARCH_DRAWABLE,
    }


    private static Map<TypeFaceKey,Typeface> typeFaces = new HashMap<>();
    private static Map<DrawableKey,Drawable> drawableMap = new HashMap<>();
    private static Map<DrawableKey,String> drawableMapColorKeys = new HashMap<>();
    private static Map<DrawableKey,FontAwesome.Icon> faIconMap = new HashMap<>();

    public static final String BACKGROUND_COLOR = "background_color";
    public static final String TEXT_COLOR = "text_color";
    public static final String SUB_TEXT_COLOR = "sub_text_color";
    public static final String TEXT_COLOR_EXAM_CATEGORY_ICON_LABEL = "text_color_exam_category_icon_label";
    public static Map<String,Theme> themes=new HashMap<>();
    public static String dayTheme = "Day";
    public static String NightTheme = "Night";
    public static Theme currentTheme;
    public static final String ACTIONBAR_COLOR = "actionbar_color";
    public static final String STATUSBAR_COLOR = "statusbar_color";
    public static final String ACTIONBAR_TEXT_COLOR = "actionbar_text_color";
    public static final String TABS_ICON_COLOR = "tabs_icon_color";
    public static final String TABS_INDICATOR_COLOR = "tabs_indicator_color";
    public static final String TABS_BACKGROUND_COLOR = "tabs_background_color";
    public static final String DAYNIGHT_ICON_COLOR = "day_night_icon_color";
    public static final String REFRESH_ICON_COLOR = "refresh_icon_color";
    public static final String CARD_BACKGROUND_COLOR = "card_background_color";
    public static final String SELECTED_FOREGROUND_COLOR = "selected_foreground_color";
    public static final String SELECTED_BACKGROUND_COLOR = "selected_background_color";
    public static final String USER_SELECTED_FOREGROUND_COLOR = "user_selected_foreground_color";
    public static final String USER_SELECTED_BACKGROUND_COLOR = "user_selected_background_color";
    public static final String HEADER_TEXT_COLOR = "header_text_color";
    public static final String HEADER_LINE_COLOR = "header_line_color";
    public static final String LOGOUT_BUTTON_TEXT_COLOR = "logout_button_text_color";
    public static final String LOGOUT_BUTTON_BACKGROUND_COLOR = "logout_button_background_color";
    public static final String SEARCH_BAR_CLOSE_COLOR = "search_bar_close_color";
    public static final String SEARCH_BAR_BACKGROUND_COLOR = "search_bar_background_color";
    public static final String ACTION_BUTTON_BACKGROUND_COLOR = "action_button_background_color";
    public static final String ACTION_BUTTON_ITEM_BACKGROUND_COLOR = "action_button_item_background_color";
    public static final String ACTION_BUTTON_TEXT_COLOR = "action_button_text_color";
    public static final String ACTION_BUTTON_ITEM_TEXT_COLOR = "action_button_item_text_color";
    public static final String LIKE_ICON_COLOR = "like_icon_color";
    public static final String COMMENT_ICON_COLOR = "comment_icon_color";
    public static final String EMPTY_ICON_COLOR = "empty_icon_color";
    public static final String EMPTY_TEXT_COLOR = "empty_text_color";
    public static final String SEARCH_TEXT_COLOR = "search_text_color";
    public static final String BUTTON_TEXT_COLOR = "button_text_color";
    public static final String BUTTON_VALUE_COLOR = "button_value_color";
    public static final String MESSAGE_INCOMING_BG = "message_incoming_bg";
    public static final String MESSAGE_OUTGOING_BG = "message_outgoing_bg";
    public static final String SPAM_BADGE_COLOR = "spam_badge_color";
    public static final String SEARCH_HIGHLIGHT_COLOR = "search_highlight_color";
    public static final String BLOCK_BADGE_COLOR = "block_badge_color";
    public static final String ENCRYPTED_BADGE_COLOR = "encrypted_badge_color";

    public static Paint readingPaint;
    public static Paint fontCellPaint;

    public Map<String,Integer> colors = new HashMap<>();
    public String name;
    boolean isNight=false;
    public static void loadThemes(Context context){
        Theme.context=context;
        loadTypeFaces();
        themes.clear();
        try {
            String[] list = context.getAssets().list("themes");
            for (String themefile:list) {
                Theme theme = readThemeFromFile(context, themefile);
                themes.put(theme.name,theme);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(AppSettings.Bool(AppSettings.Key.NIGHT_MODE)){
            currentTheme= themes.get("Night");
        }else{
            currentTheme = themes.get(AppSettings.String(AppSettings.Key.CURRENT_THEME));
        }
        loadFaIcons();
        loadDrawables();
        loadResources();
    }

    private static void loadFaIcons() {
        faIconMap.clear();
        addFaIcon(DrawableKey.SELECTED_DRAWABLE, FontAwesome.Icon.CHECK, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.HOME_TAB_ICON, FontAwesome.Icon.HOME, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.WORD_DECKS_TAB_ICON, FontAwesome.Icon.COMMENT, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.EXAMS_TAB_ICON, FontAwesome.Icon.CHECK, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.SETTINGS_TAB_ICON, FontAwesome.Icon.USER_GEAR, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.LEFT_ARROW, FontAwesome.Icon.ARROW_LEFT, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.RIGHT_ARROW, FontAwesome.Icon.ARROW_RIGHT, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.MULTI_SELECT, FontAwesome.Icon.CHECK, ACTIONBAR_COLOR);
        addFaIcon(DrawableKey.SEARCH_DRAWABLE, FontAwesome.Icon.SEARCH, ACTIONBAR_TEXT_COLOR);
        addFaIcon(DrawableKey.CLOSE_SEARCH_DRAWABLE, FontAwesome.Icon.CLOSE, ACTIONBAR_TEXT_COLOR);
    }

    private static void addFaIcon(DrawableKey key, FontAwesome.Icon icon, String colorKey) {
        faIconMap.put(key, icon);
        drawableMapColorKeys.put(key, colorKey);
    }

    public static FontAwesome.Icon getFaIcon(DrawableKey key) {
        return faIconMap.get(key);
    }

    public static int getFaIconColor(DrawableKey key) {
        String colorKey = drawableMapColorKeys.get(key);
        return colorKey != null ? getColor(colorKey) : getColor(ACTIONBAR_TEXT_COLOR);
    }

    private static void loadDrawables() {
        drawableMap.clear();
        for (Map.Entry<DrawableKey, FontAwesome.Icon> entry : faIconMap.entrySet()) {
            drawableMap.put(entry.getKey(), null);
        }
    }

    private static void addDrawable(DrawableKey key, Drawable drawable, String colorKey) {
        drawableMap.put(key, drawable);
        drawableMapColorKeys.put(key, colorKey);
        setDrawableColor(key);
    }
    public static void loadTypeFaces() {
        typeFaces.clear();
        typeFaces.put(TypeFaceKey.DEFAULT_TYPEFACE,getTypeface(AppSettings.String(AppSettings.Key.DEFAULT_FONT_NAME)));
        typeFaces.put(TypeFaceKey.DEFAULT_EN_TYPEFACE,getTypeface("default"));
        typeFaces.put(TypeFaceKey.ACTIONBAR_TYPEFACE,getTypeface(AppSettings.String(AppSettings.Key.ACTIONBAR_FONT_NAME)));
        typeFaces.put(TypeFaceKey.READING_TYPEFACE,getTypeface(AppSettings.String(AppSettings.Key.READING_FONT_NAME)));
    }

    private static void saveCurrentTheme() {
        if(!currentTheme.isNight){
            AppSettings.String(AppSettings.Key.CURRENT_THEME,currentTheme.name);
        }
    }
    private static Theme readThemeFromFile(Context context,String filename) {
        Theme theme=new Theme();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("themes/"+filename)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                String key = mLine.substring(0, mLine.indexOf("=")).trim();
                String value = mLine.substring(mLine.indexOf("=")+1).trim();
                if(key.equalsIgnoreCase("theme_name")){
                    theme.name=value;
                }else if(key.equalsIgnoreCase("night_mode")){
                    theme.isNight=Boolean.parseBoolean(value);
                }else {
                    int val_color = Color.parseColor(value);
                    theme.colors.put(key, val_color);
//                    theme.colors.put(COLOR_KEY.valueOf(key.toUpperCase()), val_color);
                }
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return theme;
    }
    public static boolean isNight(){
        return currentTheme.isNight;
    }
    public static int getColor(String key) {
        try {
            return currentTheme.colors.get(key);
        }catch (Exception e){
            Log.e("Theme", String.format("%s key is not founded", key));
        }
        return 0;
    }

    private static Drawable getDrawable(int resid){
        return getDrawable(context.getDrawable(resid));
    }
    public static Drawable getDrawable(int resid, String color_key){
        return getDrawable(context.getResources().getDrawable(resid),getColor(color_key));
    }
    public static Drawable getDrawable(int resid, int color){
        return getDrawable(context.getResources().getDrawable(resid),color);
    }
    private static Drawable getDrawable(Drawable drawable){
        return getDrawable(drawable,getColor("icon_color"));
    }
    public static Drawable getDrawable(Drawable drawable,int color){
        Drawable newd = drawable.mutate();
        newd.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return newd;
    }
    public static Drawable getDrawable(DrawableKey drawable) {
        return drawableMap.get(drawable);
    }
    private static void setDrawableColor(DrawableKey drawable) {
        if (drawable == null) {
            return;
        }
        Drawable d = drawableMap.get(drawable);
        if (d == null) {
            return;
        }
        d.setColorFilter(new PorterDuffColorFilter(getColor(drawableMapColorKeys.get(drawable)), PorterDuff.Mode.SRC_IN));
    }
    public static Drawable getDrawable(Drawable drawable,String color_key){
        Drawable newd = drawable.mutate();
        newd.setColorFilter(getColor(color_key), PorterDuff.Mode.SRC_IN);
        return newd;
    }
    public static float fontSize(float fontSize){
        Resources r;
        if (context == null) {
            r = Resources.getSystem();
        } else {
            r = context.getResources();
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, r.getDisplayMetrics());
    }
    public static void toggleNightMode() {
        AppSettings.Bool(AppSettings.Key.NIGHT_MODE,!isNight());
        if(!isNight()){
            currentTheme=themes.get("Night");
        }else{
            currentTheme=themes.get(AppSettings.String(AppSettings.Key.CURRENT_THEME));
        }
        saveCurrentTheme();
        notifyViews();
    }
    public static void switchTheme(Theme theme){
        currentTheme=theme;
        saveCurrentTheme();
        loadResources();
        changeDrawableColors();
        notifyViews();
    }

    private static void changeDrawableColors() {
        for (Map.Entry<DrawableKey,Drawable> entry:drawableMap.entrySet()) {
            setDrawableColor(entry.getKey());
        }
    }

    private static void loadResources() {
        readingPaint = new TextPaint();
        readingPaint.setTextSize(fontSize(25));
        readingPaint.setAntiAlias(true);
        readingPaint.setTypeface(getTypeface(TypeFaceKey.READING_TYPEFACE));

        fontCellPaint = new Paint();
        fontCellPaint.setAntiAlias(true);
        fontCellPaint.setColor(getColor(TEXT_COLOR));
        fontCellPaint.setTextSize(fontSize(15));
    }

    private static void notifyViews() {
        NotificationCenter.getInstance().postNotification(NotificationCenter.didChangedTheme);
    }
    public static Typeface getTypeface(String fontName) {
        if (fontName == null || fontName.equals("default")) {
            return Typeface.DEFAULT;
        }
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName + ".ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }
    public static Typeface getTypeface(TypeFaceKey typeFaceKey) {
        return typeFaces.get(typeFaceKey);
    }
}
