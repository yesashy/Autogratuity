package com.autogratuity.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.autogratuity.R;
import com.autogratuity.data.model.DeliveryStats;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Custom view for displaying statistic cards in the dashboard.
 * Updated to work with the new architecture pattern.
 */
public class StatCard extends LinearLayout {
    private TextView labelTextView;
    private TextView valueTextView;

    public StatCard(Context context) {
        super(context);
        init(context, null);
    }

    public StatCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StatCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.item_stat_card, this, true);

        labelTextView = findViewById(R.id.stat_label);
        valueTextView = findViewById(R.id.stat_value);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatCard);

            String statLabel = a.getString(R.styleable.StatCard_statLabel);
            if (statLabel != null) {
                labelTextView.setText(statLabel);
            }

            String statValue = a.getString(R.styleable.StatCard_statValue);
            if (statValue != null) {
                valueTextView.setText(statValue);
            }

            a.recycle();
        }
    }

    public void setStatLabel(String label) {
        if (labelTextView != null) {
            labelTextView.setText(label);
        }
    }

    public void setStatValue(String value) {
        if (valueTextView != null) {
            valueTextView.setText(value);
        }
    }
    
    /**
     * Format a currency value for display
     * 
     * @param value The value to format
     * @return Formatted string
     */
    public String formatCurrency(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(value);
    }
    
    /**
     * Format a percentage value for display
     * 
     * @param value The value to format (0-100)
     * @return Formatted string
     */
    public String formatPercentage(double value) {
        NumberFormat format = NumberFormat.getPercentInstance(Locale.US);
        return format.format(value / 100.0);
    }
    
    /**
     * Set the stat value from a DeliveryStats object and a specific stat type
     * 
     * @param stats The delivery stats object
     * @param statType The type of statistic to display
     */
    public void setStatFromDeliveryStats(DeliveryStats stats, StatType statType) {
        if (stats == null) {
            setStatValue("--");
            return;
        }
        
        switch (statType) {
            case TOTAL_TIPS:
                setStatValue(formatCurrency(stats.getTotalTips()));
                break;
            case AVERAGE_TIP:
                setStatValue(formatCurrency(stats.getAverageTip()));
                break;
            case HIGHEST_TIP:
                setStatValue(formatCurrency(stats.getHighestTip()));
                break;
            case DELIVERY_COUNT:
                setStatValue(String.valueOf(stats.getCount()));
                break;
            case PENDING_COUNT:
                setStatValue(String.valueOf(stats.getPendingCount()));
                break;
            case TIP_RATE:
                setStatValue(formatPercentage(stats.getTipRate()));
                break;
            default:
                setStatValue("--");
        }
    }
    
    /**
     * Enum for different types of stats from DeliveryStats
     */
    public enum StatType {
        TOTAL_TIPS,
        AVERAGE_TIP,
        HIGHEST_TIP,
        DELIVERY_COUNT,
        PENDING_COUNT,
        TIP_RATE
    }
}