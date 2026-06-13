package com.dpouya.aitexty.components;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity implements NotificationCenter.NotificationCenterDelegate {

    private Window windowRef;
    private boolean isKeyboardShowing;
    protected ActionBar actionbar;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        updateLang(null);
        windowRef = getWindow();
        windowRef.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowRef.setStatusBarColor(Theme.getColor(Theme.STATUSBAR_COLOR));
        }

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));
        rootLayout.setFitsSystemWindows(true);

        View content = initViews(this);

        if (showActionbar()) {
            actionbar = new ActionBar(this);
            initActionbar(actionbar);
            rootLayout.addView(actionbar, LayoutHelper.createFrame(
                    LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));
        }

        if (content != null) {
            rootLayout.addView(content, LayoutHelper.createFrame(
                    LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }
        setContentView(rootLayout);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didChangedTheme);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didChangedLanguage);
        addKeyboardListener(rootLayout);
    }

    protected ActionBar getActionbar() {
        return actionbar;
    }

    protected abstract View initViews(Context context);

    protected void initActionbar(ActionBar bar) {
    }

    protected boolean showActionbar() {
        return true;
    }

    protected void onKeyboardVisibilityChanged(boolean open) {
    }

    protected void updateColors() {
        if (windowRef != null) {
            windowRef.setStatusBarColor(Theme.getColor(Theme.STATUSBAR_COLOR));
        }
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));
        }
        if (actionbar != null) {
            actionbar.updateColors();
        }
    }

    private void addKeyboardListener(View contentView) {
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true;
                        onKeyboardVisibilityChanged(true);
                    }
                } else if (isKeyboardShowing) {
                    isKeyboardShowing = false;
                    onKeyboardVisibilityChanged(false);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didChangedTheme);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didChangedLanguage);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLang(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateLang(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    protected void updateLang(Configuration newConfig) {
        LocaleController.Language lang = LocaleController.getDefaultLanguage();
        if (lang == null || lang.langCode == null || lang.langCode.isEmpty()) {
            return;
        }
        Locale locale = new Locale(lang.langCode);
        Locale.setDefault(locale);
        Configuration config = newConfig != null ? newConfig : new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.didChangedTheme) {
            updateColors();
            onThemeChanged();
        } else if (id == NotificationCenter.didChangedLanguage) {
            recreate();
        }
    }

    protected void onThemeChanged() {
    }
}
