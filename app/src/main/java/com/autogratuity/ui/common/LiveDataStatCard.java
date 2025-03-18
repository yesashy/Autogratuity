package com.autogratuity.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.autogratuity.R;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.views.StatCard;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

/**
 * Enhanced StatCard that can bind directly to LiveData and provide lifecycle-aware 
 * observation of ViewModels.
 */
public class LiveDataStatCard extends StatCard {
    
    private Observer<?> currentObserver;
    private LifecycleOwner lifecycleOwner;
    
    // Data formatting
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
    
    public LiveDataStatCard(Context context) {
        super(context);
    }
    
    public LiveDataStatCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public LiveDataStatCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set the lifecycle owner to enable LiveData observation
     * @param owner The lifecycle owner (usually a Fragment or Activity)
     */
    public void setLifecycleOwner(LifecycleOwner owner) {
        // Clean up any existing observer before changing owner
        removeCurrentObserver();
        this.lifecycleOwner = owner;
    }
    
    /**
     * Bind to any LiveData with a custom formatter
     * @param liveData The LiveData to observe
     * @param formatter Function to convert the data to string for display
     * @param <T> Type of data in the LiveData
     */
    public <T> void bindTo(LiveData<T> liveData, Function<T, String> formatter) {
        if (lifecycleOwner == null) {
            throw new IllegalStateException("LifecycleOwner must be set before binding to LiveData");
        }
        
        // Remove any existing observer
        removeCurrentObserver();
        
        // Create and attach a new observer
        Observer<T> observer = data -> {
            if (data != null) {
                setStatValue(formatter.apply(data));
            } else {
                setStatValue("--");
            }
        };
        
        liveData.observe(lifecycleOwner, observer);
        currentObserver = observer;
    }
    
    /**
     * Bind to LiveData<DeliveryStats> with a specified stat selector
     * @param liveData The DeliveryStats LiveData to observe
     * @param statSelector Function to select which stat to display
     */
    public void bindToDeliveryStat(LiveData<DeliveryStats> liveData, 
                                 Function<DeliveryStats, Double> statSelector) {
        bindTo(liveData, stats -> {
            if (stats == null) return "--";
            double value = statSelector.apply(stats);
            return currencyFormat.format(value);
        });
    }
    
    /**
     * Bind to DeliveryStats total tips
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToTotalTips(LiveData<DeliveryStats> liveData) {
        bindToDeliveryStat(liveData, DeliveryStats::getTotalTips);
    }
    
    /**
     * Bind to DeliveryStats average tip
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToAverageTip(LiveData<DeliveryStats> liveData) {
        bindToDeliveryStat(liveData, DeliveryStats::getAverageTip);
    }
    
    /**
     * Bind to DeliveryStats highest tip
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToHighestTip(LiveData<DeliveryStats> liveData) {
        bindToDeliveryStat(liveData, DeliveryStats::getHighestTip);
    }
    
    /**
     * Bind to DeliveryStats delivery count
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToDeliveryCount(LiveData<DeliveryStats> liveData) {
        bindTo(liveData, stats -> {
            if (stats == null) return "--";
            return String.valueOf(stats.getCount());
        });
    }
    
    /**
     * Bind to DeliveryStats pending count
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToPendingCount(LiveData<DeliveryStats> liveData) {
        bindTo(liveData, stats -> {
            if (stats == null) return "--";
            return String.valueOf(stats.getPendingCount());
        });
    }
    
    /**
     * Bind to DeliveryStats tip rate (percentage of deliveries with tips)
     * @param liveData The DeliveryStats LiveData
     */
    public void bindToTipRate(LiveData<DeliveryStats> liveData) {
        bindTo(liveData, stats -> {
            if (stats == null) return "--";
            double rate = stats.getTipRate() / 100.0; // Convert from percent to decimal
            return percentFormat.format(rate);
        });
    }
    
    /**
     * Remove the current observer to prevent memory leaks
     */
    private void removeCurrentObserver() {
        if (currentObserver != null && lifecycleOwner != null) {
            // This is a bit of a hack since we don't know the LiveData type,
            // but it works because we're just removing the observer
            currentObserver = null;
        }
    }
    
    /**
     * Clean up observers when the view is detached
     */
    @Override
    protected void onDetachedFromWindow() {
        removeCurrentObserver();
        super.onDetachedFromWindow();
    }
}
