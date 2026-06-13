package com.dpouya.aitexty.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;



import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.accessibility.AccessibilitySettings;
import com.dpouya.aitexty.accessibility.SpeechHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class LocaleController {
    public static Map<String,Language> languages;
    public static boolean isRTL = false;
    private static Language currentLang;
    private static Context context;

    public static void loadLangs(Context context){
        LocaleController.context=context;
        languages=new HashMap<>();
        try {
            String[] list = context.getAssets().list("langs");
            for (String themefile:list) {
                Language lang = readLangFromFile(context, themefile);
                languages.put(lang.langCode,lang);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        currentLang=getDefaultLanguage();
    }

    public static class Language{
        public String langName;
        public String langCode;
        public boolean isRtl;
        private Map<String,String> values;
    }
    private static JSONObject loadJSONFromAsset(Context context, String langfile) {
        JSONObject jsonObject;
        try {
            InputStream is = context.getAssets().open(langfile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            jsonObject=new JSONObject(json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            return null;
        }
        return jsonObject;
    }
    private static Language readLangFromFile(Context context, String langfile) throws JSONException {
        Language lang=new Language();
        JSONObject json = loadJSONFromAsset(context, "langs/" + langfile);
        lang.langName = json.getString("lang_name");
        lang.langCode = json.getString("lang_code");
        lang.isRtl = json.getBoolean("is_rtl");
        lang.values=new HashMap<>();
        Iterator<String> keys = json.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            if(key.equals("lang_name")||key.equals("lang_code")||key.equals("is_rtl")){
                continue;
            }
            lang.values.put(key,json.getString(key));
        }
        return lang;
    }
    public static String getString(String key) {
        if(currentLang!=null&&currentLang.values.containsKey(key)){
            return currentLang.values.get(key);
        }
        return "Error:"+key;
    }
    public static String getString(String key,String... args) {
        try{
            return String.format(getString(key), args);
        } catch (Exception e) {
            return "LOC_ERR:" + key;
        }
    }
    public static void loadLang(){
        Language defaultlang = getDefaultLanguage();
        setLang(defaultlang);
        isRTL = defaultlang.isRtl;
    }
    private static void setLang(Language lang) {
        Locale currentLocal = null;
        if (!TextUtils.isEmpty(lang.langCode))
            currentLocal = new Locale(lang.langCode);
        else
            currentLocal = Locale.getDefault();
        Locale.setDefault(currentLocal);

        Configuration config = new Configuration();
        config.locale = currentLocal;
        context.getResources().updateConfiguration(config,context.getResources().getDisplayMetrics());
    }
    public static Language getDefaultLanguage(){
        String langCode= AppSettings.String(AppSettings.Key.DEFAULT_LANG);
        return languages.get(langCode);
    }
    public static void changeLanguage(String langcode) {
        AppSettings.String(AppSettings.Key.DEFAULT_LANG, langcode);
        currentLang = languages.get(langcode);
        if (currentLang != null) {
            isRTL = currentLang.isRtl;
        }
        loadLang();
        if (context != null) {
            AccessibilitySettings.setVoiceName("");
            SpeechHelper.getInstance(context).applySettings();
        }
        NotificationCenter.getInstance().postNotification(NotificationCenter.didChangedLanguage);
    }
}
