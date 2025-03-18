package com.autogratuity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.utils.ShiptCaptureProcessor;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Receiver for processing Shipt captures on schedule
 */
public class CaptureProcessReceiver extends BroadcastReceiver {
    private static final String TAG = "CaptureProcessReceiver";
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received capture processing alarm");

        // Ensure repositories are initialized
        if (!RepositoryProvider.isInitialized()) {
            try {
                RepositoryProvider.initialize(context);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize repositories", e);
                return;
            }
        }

        // Process captures using ShiptCaptureProcessor but with better error handling
        ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context);
        
        // Using the existing callback pattern until ShiptCaptureProcessor is updated
        // to return RxJava Single in a future task
        new Thread(() -> {
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