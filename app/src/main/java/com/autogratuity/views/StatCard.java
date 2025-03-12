package com.autogratuity.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.autogratuity.R;

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
}