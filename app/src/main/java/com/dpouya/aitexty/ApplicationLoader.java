package com.dpouya.aitexty;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.accessibility.SpeechHelper;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;

public class ApplicationLoader extends Application {

    private static Context appContext;
    private static Handler handler;

    public static Context getAppContext() {
        return appContext;
    }

    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        handler = new Handler(Looper.getMainLooper());
        AndroidUtilities.init(this);
        AppSettings.init(this);
        FontAwesome.init(this);
        SpeechHelper.getInstance(this);
        Theme.loadThemes(this);
        LocaleController.loadLangs(this);
        LocaleController.loadLang();
        AppDatabase.getInstance(this);
    }
}
