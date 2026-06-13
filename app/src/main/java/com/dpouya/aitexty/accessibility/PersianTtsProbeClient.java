package com.dpouya.aitexty.accessibility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class PersianTtsProbeClient {
    private static final String TAG = "PersianTtsProbeClient";
    static final String EXTRA_VOICE_ID = "voice_id";
    static final String EXTRA_ERROR = "error";
    private static final long PROBE_TIMEOUT_MS = 20_000L;

    interface ProbeResult {
        void onComplete(boolean success, String error);
    }

    private PersianTtsProbeClient() {
    }

    static boolean probeBlocking(Context context, PersianTtsVoice voice) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();
        probeAsync(context.getApplicationContext(), voice, (ok, err) -> {
            success.set(ok);
            error.set(err);
            latch.countDown();
        });
        try {
            if (!latch.await(PROBE_TIMEOUT_MS + 5_000L, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "Probe timed out for " + voice.id);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        if (!success.get() && error.get() != null) {
            Log.w(TAG, "Probe failed for " + voice.id + ": " + error.get());
        }
        return success.get();
    }

    private static void probeAsync(Context context, PersianTtsVoice voice, ProbeResult result) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent(context, PersianTtsProbeService.class);
        AtomicBoolean finished = new AtomicBoolean(false);
        ServiceConnection[] connectionHolder = new ServiceConnection[1];

        ServiceConnection connection = new ServiceConnection() {
            Messenger serviceMessenger;
            final Messenger replyMessenger = new Messenger(new Handler(mainHandler.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what != PersianTtsProbeService.MSG_RESULT || finished.getAndSet(true)) {
                        return;
                    }
                    boolean ok = msg.arg1 == 1;
                    String err = msg.getData().getString(EXTRA_ERROR);
                    safeUnbind(context, connectionHolder[0]);
                    result.onComplete(ok, err);
                }
            });

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceMessenger = new Messenger(service);
                Message probe = Message.obtain(null, PersianTtsProbeService.MSG_PROBE);
                probe.replyTo = replyMessenger;
                probe.getData().putString(EXTRA_VOICE_ID, voice.id);
                try {
                    serviceMessenger.send(probe);
                } catch (Exception e) {
                    if (finished.compareAndSet(false, true)) {
                        safeUnbind(context, connectionHolder[0]);
                        result.onComplete(false, e.getMessage());
                    }
                }
                mainHandler.postDelayed(() -> {
                    if (finished.compareAndSet(false, true)) {
                        safeUnbind(context, connectionHolder[0]);
                        result.onComplete(false, "Probe timed out");
                    }
                }, PROBE_TIMEOUT_MS);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (finished.compareAndSet(false, true)) {
                    result.onComplete(false, "Probe process crashed");
                }
            }
        };
        connectionHolder[0] = connection;

        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            result.onComplete(false, "Could not start probe service");
        }
    }

    private static void safeUnbind(Context context, ServiceConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            context.unbindService(connection);
        } catch (Exception ignored) {
        }
    }
}
