package com.autogratuity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autogratuity.utils.ShiptCaptureProcessor;

/**
 * Receiver for processing Shipt captures on schedule
 */
public class CaptureProcessReceiver extends BroadcastReceiver {
    private static final String TAG = "CaptureProcessReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received capture processing alarm");

        // Process captures in a background thread
        new Thread(() -> {
            ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context);
            processor.processCaptures(new ShiptCaptureProcessor.ProcessCallback() {
                @Override
                public void onComplete(int count) {
                    Log.d(TAG, "Alarm-triggered processing completed: " + count + " captures processed");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error during alarm-triggered processing", e);
                }
            });
        }).start();
    }
}