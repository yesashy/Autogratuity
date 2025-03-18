package com.autogratuity.ui.common;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.views.StatCard;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

/**
 * Extension methods for integrating StatCard with ViewModels and LiveData.
 * This provides utility methods for binding StatCard components to repositories
 * through ViewModels.
 */
public class StatCardExtensions {
    
    /**
     * Bind a StatCard to DeliveryStats LiveData
     * 
     * @param statCard The StatCard to bind
     * @param liveData The LiveData to observe
     * @param lifecycleOwner The LifecycleOwner to manage observation
     * @param statType The type of stat to display from DeliveryStats
     * @return The observer that was created for cleanup purposes
     */
    public static Observer<DeliveryStats> bindStatCardToDeliveryStats(
            StatCard statCard,
            LiveData<DeliveryStats> liveData,
            LifecycleOwner lifecycleOwner,
            StatCard.StatType statType) {
        
        Observer<DeliveryStats> observer = stats -> {
            if (stats != null) {
                statCard.setStatFromDeliveryStats(stats, statType);
            } else {
                statCard.setStatValue("--");
            }
        };
        
        liveData.observe(lifecycleOwner, observer);
        return observer;
    }
    
    /**
     * Bind a StatCard to any LiveData with a custom formatter
     * 
     * @param statCard The StatCard to bind
     * @param liveData The LiveData to observe
     * @param lifecycleOwner The LifecycleOwner to manage observation
     * @param formatter Function to convert the value to a string
     * @param <T> The type of data
     * @return The observer that was created for cleanup purposes
     */
    public static <T> Observer<T> bindStatCard(
            StatCard statCard,
            LiveData<T> liveData,
            LifecycleOwner lifecycleOwner,
            Function<T, String> formatter) {
        
        Observer<T> observer = value -> {
            if (value != null) {
                statCard.setStatValue(formatter.apply(value));
            } else {
                statCard.setStatValue("--");
            }
        };
        
        liveData.observe(lifecycleOwner, observer);
        return observer;
    }
    
    /**
     * Bind a StatCard to a currency value LiveData
     * 
     * @param statCard The StatCard to bind
     * @param liveData The LiveData to observe
     * @param lifecycleOwner The LifecycleOwner to manage observation
     * @return The observer that was created for cleanup purposes
     */
    public static Observer<Double> bindStatCardToCurrency(
            StatCard statCard, 
            LiveData<Double> liveData,
            LifecycleOwner lifecycleOwner) {
        
        return bindStatCard(
            statCard,
            liveData,
            lifecycleOwner,
            value -> {
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                return format.format(value);
            }
        );
    }
    
    /**
     * Bind a StatCard to a percentage value LiveData (0-100)
     * 
     * @param statCard The StatCard to bind
     * @param liveData The LiveData to observe
     * @param lifecycleOwner The LifecycleOwner to manage observation
     * @return The observer that was created for cleanup purposes
     */
    public static Observer<Double> bindStatCardToPercentage(
            StatCard statCard,
            LiveData<Double> liveData,
            LifecycleOwner lifecycleOwner) {
        
        return bindStatCard(
            statCard,
            liveData,
            lifecycleOwner,
            value -> statCard.formatPercentage(value)
        );
    }
    
    /**
     * Bind a StatCard to a count value LiveData
     * 
     * @param statCard The StatCard to bind
     * @param liveData The LiveData to observe
     * @param lifecycleOwner The LifecycleOwner to manage observation
     * @return The observer that was created for cleanup purposes
     */
    public static Observer<Integer> bindStatCardToCount(
            StatCard statCard,
            LiveData<Integer> liveData,
            LifecycleOwner lifecycleOwner) {
        
        return bindStatCard(
            statCard,
            liveData,
            lifecycleOwner,
            String::valueOf
        );
    }
}
