package com.autogratuity.ui.faq;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.ui.common.BaseViewModel;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for FaqActivity, implementing the repository pattern.
 * Handles retrieval of FAQ content from repositories.
 */
public class FaqViewModel extends BaseViewModel {
    private static final String TAG = "FaqViewModel";
    
    private final ConfigRepository configRepository;
    
    // LiveData fields for UI state
    private final MutableLiveData<String> faqContentLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> faqTitleLiveData = new MutableLiveData<>("Knowledge Base");
    
    /**
     * Constructor with repository injection
     * 
     * @param configRepository Repository for app configuration
     */
    public FaqViewModel(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
    
    /**
     * Get FAQ content as LiveData
     * 
     * @return LiveData containing FAQ content
     */
    public LiveData<String> getFaqContent() {
        return faqContentLiveData;
    }
    
    /**
     * Get FAQ title as LiveData
     * 
     * @return LiveData containing FAQ title
     */
    public LiveData<String> getFaqTitle() {
        return faqTitleLiveData;
    }
    
    /**
     * Load FAQ content from configuration
     */
    public void loadFaqContent() {
        setLoading(true);
        
        // Try to load custom FAQ content from config repository
        disposables.add(
            Single.fromCallable(() -> configRepository.getConfigValue("faq_content", ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    content -> {
                        if (content != null && !content.isEmpty()) {
                            faqContentLiveData.setValue(content);
                        } else {
                            // If no custom content, return null and let the activity load from assets
                            faqContentLiveData.setValue(null);
                        }
                        setLoading(false);
                    },
                    error -> {
                        Log.e(TAG, "Error loading FAQ content", error);
                        setError(error);
                        // On error, return null and let the activity load from assets
                        faqContentLiveData.setValue(null);
                        setLoading(false);
                    }
                )
        );
    }
    
    /**
     * Load FAQ title from configuration
     */
    public void loadFaqTitle() {
        disposables.add(
            Single.fromCallable(() -> configRepository.getConfigValue("faq_title", "Knowledge Base"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    title -> {
                        if (title != null && !title.isEmpty()) {
                            faqTitleLiveData.setValue(title);
                        }
                    },
                    error -> Log.e(TAG, "Error loading FAQ title", error)
                )
        );
    }
    
    /**
     * Track FAQ view event
     */
    public void trackFaqView() {
        // Track that user viewed the FAQ
        disposables.add(
            Single.fromCallable(() -> configRepository.getConfigBoolean("track_faq_views", true))
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(shouldTrack -> {
                    if (shouldTrack) {
                        // Here we could integrate with analytics if needed
                        return configRepository.incrementCounter("faq_view_count");
                    }
                    return configRepository.noOpCompletable();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d(TAG, "FAQ view tracked"),
                    error -> Log.e(TAG, "Error tracking FAQ view", error)
                )
        );
    }
}
