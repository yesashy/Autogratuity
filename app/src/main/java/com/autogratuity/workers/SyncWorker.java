package com.autogratuity.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import androidx.work.ListenableWorker.Result;

import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.sync.SyncRepository;

import io.reactivex.Single;

/**
 * Worker class for handling background synchronization tasks.
 * Uses RxWorker for reactive programming integration.
 */
public class SyncWorker extends RxWorker {
    
    private static final String TAG = "SyncWorker";
    
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Single<Result> createWork() {
        Log.d(TAG, "Starting background sync work");
        
        // Get the sync repository
        SyncRepository syncRepository = RepositoryProvider.getSyncRepository();
        
        // Perform sync operation
        return syncRepository.syncData()
            .toSingleDefault(Result.success())
            .onErrorReturn(error -> {
                Log.e(TAG, "Error during background sync", error);
                return Result.failure();
            });
    }
}
