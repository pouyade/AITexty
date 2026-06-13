package com.dpouya.aitexty;

import android.util.SparseArray;

import androidx.annotation.UiThread;

import java.util.ArrayList;

public class NotificationCenter {

    private static NotificationCenter instance;

    public static NotificationCenter getInstance(){
        if(instance==null){
            instance=new NotificationCenter();
        }
        return instance;
    }

    private SparseArray<ArrayList<NotificationCenterDelegate>> observers = new SparseArray<>();

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }
    private static int totalEvents = 1;

    public static final int didDataChanged = totalEvents++;
    public static final int didChangedTheme = totalEvents++;
    public static final int didUserLogedin = totalEvents++;
    public static final int didUserLogedOut = totalEvents++;
    public static final int didInformationUpdated = totalEvents++;
    public static final int didLoadedStories = totalEvents++;
    public static final int didSwitchAccount = totalEvents++;
    public static final int didAddDownloadItem = totalEvents++;
    public static final int didDownloadStatusChanged = totalEvents++;
    public static final int didChangedLanguage = totalEvents++;
    public static final int didStartedRefresh = totalEvents++;
    public static final int didEndedRefresh = totalEvents++;
    public static final int didDeletedDownloadItem = totalEvents++;
    public static final int didFontChange = totalEvents++;
    public static final int didReceiveSms = totalEvents++;
    public static final int didConversationsChanged = totalEvents++;
    public static final int hiddenVaultUnlocked = totalEvents++;
    public static final int hiddenVaultLocked = totalEvents++;
    public static final int didBlocklistChanged = totalEvents++;
    public static final int didSearchResultsChanged = totalEvents++;
    public static final int didAccessibilitySettingsChanged = totalEvents++;

    public void removeObserver(NotificationCenterDelegate observer, int id) {
        ArrayList<NotificationCenterDelegate> objects = observers.get(id);
        if (objects != null) {
            objects.remove(observer);
        }
    }
    public void addObserver(NotificationCenterDelegate observer, int id) {
        ArrayList<NotificationCenterDelegate> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }
    @UiThread
    public void postNotification(int id, Object... args) {
        ArrayList<NotificationCenterDelegate> objects = observers.get(id);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                NotificationCenterDelegate obj = objects.get(a);
                ApplicationLoader.runOnUiThread(()->obj.didReceivedNotification(id, args));

            }
        }
    }
}
