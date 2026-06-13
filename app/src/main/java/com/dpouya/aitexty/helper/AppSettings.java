package com.dpouya.aitexty.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by pouyadark on 8/4/18.
 */


public class AppSettings {


    private static final String SPILLITOR = "@@@!!";



    public enum Key {
        IS_INTRO_COMPLETED,
        CURRENT_THEME,
        NIGHT_MODE,
        DEFAULT_LANG,
        DEFAULT_FONT_NAME,
        ACTIONBAR_FONT_NAME,
        READING_FONT_NAME,
        AI_MODEL_PATH,
        AUTO_REPLY_ENABLED,
        SPAM_AUTO_BLOCK,
        HIDDEN_UNLOCK_METHOD,
        HIDDEN_VAULT_PIN_HASH,
        HIDDEN_VAULT_TIMEOUT_MIN,
        DECOY_NOTIFICATION_STYLE,
        RECENT_SEARCHES,
        LLM_LOADED,
        TTS_SPEAK_BUTTON_ENABLED,
        STT_COMPOSE_ENABLED,
        VOICE_CONTROL_ENABLED,
        TTS_VOICE_NAME,
        TTS_SPEECH_RATE,
    }

    private static final String PREF_NAME = "aitexty_settings";
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static HashMap<String,Object> dictionary = new HashMap<>();
    private static Map<String, Object> defaultmap = new HashMap<String, Object>() {{
        put(Key.IS_INTRO_COMPLETED.name(), false);
        put(Key.CURRENT_THEME.name(), "Day");
        put(Key.NIGHT_MODE.name(), false);
        put(Key.DEFAULT_LANG.name(), "fa");
        put(Key.DEFAULT_FONT_NAME.name(), "default");
        put(Key.ACTIONBAR_FONT_NAME.name(), "default");
        put(Key.READING_FONT_NAME.name(), "default");
        put(Key.AI_MODEL_PATH.name(), "");
        put(Key.AUTO_REPLY_ENABLED.name(), false);
        put(Key.SPAM_AUTO_BLOCK.name(), false);
        put(Key.HIDDEN_UNLOCK_METHOD.name(), "gesture");
        put(Key.HIDDEN_VAULT_PIN_HASH.name(), "");
        put(Key.HIDDEN_VAULT_TIMEOUT_MIN.name(), 5);
        put(Key.DECOY_NOTIFICATION_STYLE.name(), "generic");
        put(Key.RECENT_SEARCHES.name(), "");
        put(Key.LLM_LOADED.name(), false);
        put(Key.TTS_SPEAK_BUTTON_ENABLED.name(), false);
        put(Key.STT_COMPOSE_ENABLED.name(), true);
        put(Key.VOICE_CONTROL_ENABLED.name(), false);
        put(Key.TTS_VOICE_NAME.name(), "");
        put(Key.TTS_SPEECH_RATE.name(), 100);
    }};

    public static void init(Context context) {
        if(pref==null) {
            pref =  context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }
    //boolean
    protected static Boolean getBoolean(String key){
        if(dictionary.containsKey(key)){
            return (Boolean) dictionary.get(key);
        }
       
        boolean val=pref.getBoolean(key, false);
        dictionary.put(key,val);
        return val;
    }
    protected static Boolean getBoolean(String key, boolean defaultvalue){
        if(dictionary.containsKey(key)){
            return (Boolean) dictionary.get(key);
        }
       
        boolean val= pref.getBoolean(key, defaultvalue);
        dictionary.put(key,val);
        return val;
    }
    protected static void setBoolean(String key, boolean value) {
        dictionary.put(key,value);
       
        editor.putBoolean(key, value);
        editor.commit();
    }

    //string
    protected static String getString(String key){
        if(dictionary.containsKey(key)){
            return (String) dictionary.get(key);
        }
       
        String val = pref.getString(key, null);
        dictionary.put(key,val);
        return val;
    }
    protected static String getString(String key, String defaultvalue){
        if(dictionary.containsKey(key)){
            return (String) dictionary.get(key);
        }
       
        String val = pref.getString(key, defaultvalue);
        dictionary.put(key,val);
        return val;
    }
    protected static void setString(String key, String value) {

        dictionary.put(key,value);
       
        editor.putString(key, value);
        editor.commit();
    }

    //int

    protected static int getInt(String key){
        if(dictionary.containsKey(key)){
            return (int) dictionary.get(key);
        }
       
        int val = pref.getInt(key, 0);
        dictionary.put(key,val);
        return val;
    }

    protected static int getInt(String key, int defaultvalue){
        if(dictionary.containsKey(key)){
            return (int) dictionary.get(key);
        }
       
        int val = pref.getInt(key, defaultvalue);
        dictionary.put(key,val);
        return val;
    }
    public static void ClearAll(){
       
        editor.clear();
        editor.commit();
    }
    protected static void setInt(String key, int i) {
        dictionary.put(key,i);
       
        editor.putInt(key, i);
        editor.commit();
    }
    protected static void setLong(String key, long i) {
        dictionary.put(key,i);
       
        editor.putLong(key, i);
        editor.commit();
    }
    protected static long getLong(String key, long defaultvalue){
        if(dictionary.containsKey(key)){
            return (long) dictionary.get(key);
        }
       
        long val = pref.getLong(key, defaultvalue);
        dictionary.put(key,val);
        return val;
    }
    /////////////////
    public static boolean Bool(Key setting){
        Object res = defaultmap.get(setting.name());
        return getBoolean(setting.name(),res!=null?(Boolean)res:false);
    }
    public static void Bool(Key setting,boolean value){
        setBoolean(setting.name(),value);
    }
    public static boolean ToggleBool(Key setting){
        Bool(setting,!Bool(setting));
        return Bool(setting);
    }


    public static int Int(Key setting){
        Object res = defaultmap.get(setting.name());
        return getInt(setting.name(),res!=null?(int)res:0);
    }
    public static void Int(Key setting,int value){
        setInt(setting.name(),value);
    }
    public static void IntPlus(Key setting,int value){
        setInt(setting.name(),Int(setting)+value);
    }
    public static int IntInc(Key setting){
        setInt(setting.name(),Int(setting)+1);
        return(Int(setting));
    }


    public static long Long(Key setting){
        Object res = defaultmap.get(setting.name());
        return getLong(setting.name(),res!=null?(int)res:0);
    }
    public static void Long(Key setting,long value){
        setLong(setting.name(),value);
    }


    public static String String(Key setting){
        Object res = defaultmap.get(setting.name());
        return getString(setting.name(),res!=null?(String)res:null);
    }
    public static void String(Key setting,String value){
        setString(setting.name(),value);
    }


    public static List<String> StrList(Key setting){
        Object res = defaultmap.get(setting.name());
        if(res==null){
            res = "";
        }
        return Arrays.asList(getString(setting.name(), (String) res).split(SPILLITOR));
    }
    public static void StrList(Key setting,List<String> values){
        setString(setting.name(), TextUtils.join(SPILLITOR,values));
    }
    public static void addToStrList(Key setting,String value){
        List<String> list = StrList(setting);
        list.add(value);
        StrList(setting,list);
    }
    public static void removeFromStrList(Key setting,String value){
        List<String> list = StrList(setting);
        list.remove(value);
        StrList(setting,list);
    }
    public static void clearStrList(Key setting){
        StrList(setting,new ArrayList<String>());
    }


    public static boolean BoolOR(Key setting1,Key setting2) {
        return Bool(setting1)||Bool(setting2);
    }
}
