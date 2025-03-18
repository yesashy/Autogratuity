package com.autogratuity.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.R;
import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.model.DeliveryStats;
import com.autogratuity.views.StatCard;
import com.autogratuity.ui.common.StatCardExtensions;
import com.autogratuity.ui.common.LiveDataStatCard;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Dashboard fragment that displays summary statistics
 * Updated to use ViewModel architecture pattern
 */
public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";
    
    // ViewModel
    private DashboardViewModel viewModel;
    
    // For cleanup of observers
    private final List<Observer<?>> observers = new ArrayList<>();
    
    // UI elements - Today's stats
    private StatCard todayTipsReceived;
    private StatCard todayPendingTips;
    private StatCard todayAverageTip;
    private StatCard todayDeliveries;
    
    // UI elements - 7 day stats
    private StatCard weekTipsReceived;
    private StatCard weekPendingTips;
    private StatCard weekAverageTip;
    private StatCard weekDeliveries;
    
    // UI elements - 30 day stats
    private StatCard monthTipsReceived;
    private StatCard monthPendingTips;
    private StatCard monthAverageTip;
    private StatCard monthDeliveries;
    
    // UI elements - Recent activity and best tipping areas
    private LinearLayout recentActivityContainer;
    private LinearLayout bestTippingAreasContainer;
    
    /**
     * Factory method to create a new instance of the fragment
     */
    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up UI components
        setupUI(view);
        
        // Observe ViewModel
        observeViewModel();
    }
    
    /**
     * Set up UI components and references
     */
    private void setupUI(View view) {
        // Initialize today stats cards
        todayTipsReceived = view.findViewById(R.id.today_tips_received);
        todayPendingTips = view.findViewById(R.id.today_pending_tips);
        todayAverageTip = view.findViewById(R.id.today_average_tip);
        todayDeliveries = view.findViewById(R.id.today_deliveries);
        
        // Initialize week stats cards
        weekTipsReceived = view.findViewById(R.id.week_tips_received);
        weekPendingTips = view.findViewById(R.id.week_pending_tips);
        weekAverageTip = view.findViewById(R.id.week_average_tip);
        weekDeliveries = view.findViewById(R.id.week_deliveries);
        
        // Initialize month stats cards
        monthTipsReceived = view.findViewById(R.id.month_tips_received);
        monthPendingTips = view.findViewById(R.id.month_pending_tips);
        monthAverageTip = view.findViewById(R.id.month_average_tip);
        monthDeliveries = view.findViewById(R.id.month_deliveries);
        
        // Initialize container views
        recentActivityContainer = view.findViewById(R.id.recent_activity_container);
        bestTippingAreasContainer = view.findViewById(R.id.best_tipping_areas_container);
        
        // Hide or remove the "Try New React Native UI" button as we're moving to native UI
        Button tryReactUiButton = view.findViewById(R.id.try_react_ui_button);
        if (tryReactUiButton != null) {
            // Either hide the button
            tryReactUiButton.setVisibility(View.GONE);
            
            // Or update its text and functionality to align with the new native UI direction
            tryReactUiButton.setText("Help Improve UI");
            tryReactUiButton.setOnClickListener(v -> {
                // Launch feedback form or settings screen instead
                Toast.makeText(getContext(), "UI feedback functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Hide the parent card if needed
        View tryNewUiCard = view.findViewById(R.id.try_new_ui_card);
        if (tryNewUiCard != null) {
            tryNewUiCard.setVisibility(View.GONE);
        }
    }
    
    /**
     * Observe LiveData from ViewModel
     */
    private void observeViewModel() {
        // Observe today's stats using the new StatCardExtensions methods
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                todayTipsReceived, 
                viewModel.getTodayStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.TOTAL_TIPS));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                todayPendingTips, 
                viewModel.getTodayStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.PENDING_COUNT));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                todayAverageTip, 
                viewModel.getTodayStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.AVERAGE_TIP));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                todayDeliveries, 
                viewModel.getTodayStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.DELIVERY_COUNT));
        
        // Observe week stats
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                weekTipsReceived, 
                viewModel.getWeekStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.TOTAL_TIPS));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                weekPendingTips, 
                viewModel.getWeekStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.PENDING_COUNT));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                weekAverageTip, 
                viewModel.getWeekStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.AVERAGE_TIP));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                weekDeliveries, 
                viewModel.getWeekStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.DELIVERY_COUNT));
        
        // Observe month stats
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                monthTipsReceived, 
                viewModel.getMonthStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.TOTAL_TIPS));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                monthPendingTips, 
                viewModel.getMonthStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.PENDING_COUNT));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                monthAverageTip, 
                viewModel.getMonthStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.AVERAGE_TIP));
        
        observers.add(StatCardExtensions.bindStatCardToDeliveryStats(
                monthDeliveries, 
                viewModel.getMonthStats(), 
                getViewLifecycleOwner(), 
                StatCard.StatType.DELIVERY_COUNT));
        
        // Observe recent deliveries
        viewModel.getRecentDeliveries().observe(getViewLifecycleOwner(), this::updateRecentActivity);
        
        // Observe best tipping areas
        viewModel.getBestTippingAreas().observe(getViewLifecycleOwner(), this::updateBestTippingAreas);
        
        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Update UI with loading state if needed
        });
        
        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error in ViewModel", error);
                Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Update the recent activity UI with the provided data
     */
    private void updateRecentActivity(List<Delivery> deliveries) {
        // Clear existing views
        recentActivityContainer.removeAllViews();
        
        if (deliveries == null || deliveries.isEmpty()) {
            // Show empty state
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No recent activity");
            recentActivityContainer.addView(emptyView);
            return;
        }
        
        // Create views for each delivery
        for (Delivery delivery : deliveries) {
            View activityItem = getLayoutInflater().inflate(
                    R.layout.item_recent_activity, recentActivityContainer, false);
            
            TextView addressText = activityItem.findViewById(R.id.address_text);
            TextView dateText = activityItem.findViewById(R.id.date_text);
            TextView amountText = activityItem.findViewById(R.id.amount_text);
            
            // Set values
            if (delivery.getAddress() != null && delivery.getAddress().getFullAddress() != null) {
                addressText.setText(delivery.getAddress().getFullAddress());
            } else if (delivery.getReference() != null && delivery.getReference().getAddressText() != null) {
                addressText.setText(delivery.getReference().getAddressText());
            } else {
                addressText.setText("Unknown Address");
            }
            
            // Format date
            String dateStr = "Unknown date";
            if (delivery.getTimes() != null && delivery.getTimes().getCompletedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                dateStr = sdf.format(delivery.getTimes().getCompletedAt());
            }
            dateText.setText(dateStr);
            
            // Format amount
            String amountStr = "$0.00";
            if (delivery.getAmounts() != null && delivery.getAmounts().getTipAmount() > 0) {
                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
                amountStr = nf.format(delivery.getAmounts().getTipAmount());
            }
            amountText.setText(amountStr);
            
            // Add to container
            recentActivityContainer.addView(activityItem);
        }
    }
    
    /**
     * Update the best tipping areas UI with the provided data
     */
    private void updateBestTippingAreas(List<Address> addresses) {
        // Clear existing views
        bestTippingAreasContainer.removeAllViews();
        
        if (addresses == null || addresses.isEmpty()) {
            // Show empty state
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No tipping data available");
            bestTippingAreasContainer.addView(emptyView);
            return;
        }
        
        // Create views for each address
        for (Address address : addresses) {
            View areaItem = getLayoutInflater().inflate(
                    R.layout.item_tipping_area, bestTippingAreasContainer, false);
            
            TextView addressText = areaItem.findViewById(R.id.address_text);
            TextView statsText = areaItem.findViewById(R.id.stats_text);
            
            // Set address text
            addressText.setText(address.getFullAddress());
            
            // Set stats text
            String statsStr = "No statistics available";
            if (address.getDeliveryStats() != null) {
                Address.DeliveryStats stats = address.getDeliveryStats();
                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
                statsStr = String.format("Avg Tip: %s (%d deliveries)",
                        nf.format(stats.getAverageTip()),
                        stats.getDeliveryCount());
            }
            statsText.setText(statsStr);
            
            // Add to container
            bestTippingAreasContainer.addView(areaItem);
        }
    }
    
    /**
     * Clean up observers when fragment is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clean up all observers
        for (Observer<?> observer : observers) {
            // We don't have direct access to remove observers from LiveData,
            // but the LiveData will clean up observers that are associated with
            // the destroyed LifecycleOwner
        }
        observers.clear();
    }
    
    /**
     * Refresh data when requested
     */
    public void refreshData() {
        viewModel.refreshData();
    }
}