package com.autogratuity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.autogratuity.R;
import com.autogratuity.models.Address;
import com.autogratuity.models.Delivery;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.views.StatCard;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";
    private FirestoreRepository repository;

    // UI components for Today
    private StatCard todayTipsReceived;
    private StatCard todayPendingTips;
    private StatCard todayAverageTip;
    private StatCard todayDeliveries;

    // UI components for 7 Days
    private StatCard weekTipsReceived;
    private StatCard weekPendingTips;
    private StatCard weekAverageTip;
    private StatCard weekDeliveries;

    // UI components for 30 Days
    private StatCard monthTipsReceived;
    private StatCard monthPendingTips;
    private StatCard monthAverageTip;
    private StatCard monthDeliveries;

    // Additional UI components
    private LinearLayout recentActivityContainer;
    private LinearLayout bestTippingAreasContainer;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        repository = FirestoreRepository.getInstance();

        // Initialize UI components for Today
        todayTipsReceived = view.findViewById(R.id.today_tips_received);
        todayPendingTips = view.findViewById(R.id.today_pending_tips);
        todayAverageTip = view.findViewById(R.id.today_average_tip);
        todayDeliveries = view.findViewById(R.id.today_deliveries);

        // Initialize UI components for 7 Days
        weekTipsReceived = view.findViewById(R.id.week_tips_received);
        weekPendingTips = view.findViewById(R.id.week_pending_tips);
        weekAverageTip = view.findViewById(R.id.week_average_tip);
        weekDeliveries = view.findViewById(R.id.week_deliveries);

        // Initialize UI components for 30 Days
        monthTipsReceived = view.findViewById(R.id.month_tips_received);
        monthPendingTips = view.findViewById(R.id.month_pending_tips);
        monthAverageTip = view.findViewById(R.id.month_average_tip);
        monthDeliveries = view.findViewById(R.id.month_deliveries);

        // Additional UI components
        recentActivityContainer = view.findViewById(R.id.recent_activity_container);
        bestTippingAreasContainer = view.findViewById(R.id.best_tipping_areas_container);

        // Load data
        refreshData();
    }

    public void refreshData() {
        if (!isAdded()) return;

        // Format for currency
        DecimalFormat currencyFormat = new DecimalFormat("$0.00");

        // Load today's statistics (1 day)
        loadPeriodStatistics(1, (totalDeliveries, totalTips, tippedDeliveries, pendingTips) -> {
            double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

            // Update UI for Today
            todayTipsReceived.setStatValue(currencyFormat.format(totalTips));
            todayPendingTips.setStatValue(String.valueOf(pendingTips));
            todayAverageTip.setStatValue(currencyFormat.format(averageTip));
            todayDeliveries.setStatValue(String.valueOf(totalDeliveries));
        });

        // Load week statistics (7 days)
        loadPeriodStatistics(7, (totalDeliveries, totalTips, tippedDeliveries, pendingTips) -> {
            double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

            // Update UI for Week
            weekTipsReceived.setStatValue(currencyFormat.format(totalTips));
            weekPendingTips.setStatValue(String.valueOf(pendingTips));
            weekAverageTip.setStatValue(currencyFormat.format(averageTip));
            weekDeliveries.setStatValue(String.valueOf(totalDeliveries));
        });

        // Load month statistics (30 days)
        loadPeriodStatistics(30, (totalDeliveries, totalTips, tippedDeliveries, pendingTips) -> {
            double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

            // Update UI for Month
            monthTipsReceived.setStatValue(currencyFormat.format(totalTips));
            monthPendingTips.setStatValue(String.valueOf(pendingTips));
            monthAverageTip.setStatValue(currencyFormat.format(averageTip));
            monthDeliveries.setStatValue(String.valueOf(totalDeliveries));
        });

        // Load recent activity
        loadRecentActivity();

        // Load best tipping areas
        loadBestTippingAreas();
    }

    /**
     * Load statistics for a specific time period
     */
    private void loadPeriodStatistics(int daysAgo, StatisticsCallback callback) {
        repository.getDeliveryStatistics(daysAgo)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    int totalDeliveries = queryDocumentSnapshots.size();
                    double totalTips = 0;
                    int tippedDeliveries = 0;
                    int pendingTips = 0;

                    List<Delivery> periodDeliveries = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Delivery delivery = Delivery.fromDocument(document);
                        periodDeliveries.add(delivery);

                        if (delivery.isTipped()) {
                            totalTips += delivery.getTipAmount();
                            tippedDeliveries++;
                        } else {
                            pendingTips++;
                        }
                    }

                    callback.onStatisticsLoaded(totalDeliveries, totalTips, tippedDeliveries, pendingTips);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    if (isAdded()) {
                        callback.onStatisticsLoaded(0, 0, 0, 0);
                    }
                });
    }

    /**
     * Load recent activity for display
     */
    private void loadRecentActivity() {
        if (recentActivityContainer == null || !isAdded()) return;

        // Clear existing views
        recentActivityContainer.removeAllViews();

        // Load recent deliveries
        repository.getRecentDeliveries(5)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyStateView(recentActivityContainer, "No recent activity");
                        return;
                    }

                    // Process and display recent deliveries
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Delivery delivery = Delivery.fromDocument(document);
                        addActivityItem(recentActivityContainer, delivery);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        addEmptyStateView(recentActivityContainer, "Error loading recent activity");
                    }
                });
    }

    /**
     * Load best tipping areas for display
     */
    private void loadBestTippingAreas() {
        if (bestTippingAreasContainer == null || !isAdded()) return;

        // Clear existing views
        bestTippingAreasContainer.removeAllViews();

        // Load best tipping addresses
        repository.getBestTippingAddresses(3)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    if (queryDocumentSnapshots.isEmpty()) {
                        addEmptyStateView(bestTippingAreasContainer, "No tipping data yet");
                        return;
                    }

                    // Process and display best tipping areas
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Address address = Address.fromDocument(document);
                        addBestTippingArea(bestTippingAreasContainer, address);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        addEmptyStateView(bestTippingAreasContainer, "Error loading best tipping areas");
                    }
                });
    }

    /**
     * Add an activity item to the container
     */
    private void addActivityItem(LinearLayout container, Delivery delivery) {
        if (container == null || !isAdded()) return;

        View activityItem = LayoutInflater.from(getContext()).inflate(R.layout.item_activity, container, false);

        TextView titleText = activityItem.findViewById(R.id.activity_title);
        TextView subtitleText = activityItem.findViewById(R.id.activity_subtitle);
        TextView valueText = activityItem.findViewById(R.id.activity_value);

        String orderId = delivery.getOrderId();
        String address = delivery.getAddress() != null ? delivery.getAddress() : "Unknown";
        String shortAddress = address.length() > 30 ? address.substring(0, 27) + "..." : address;

        titleText.setText("Order #" + orderId);
        subtitleText.setText(shortAddress);

        if (delivery.isTipped()) {
            valueText.setText(String.format("$%.2f", delivery.getTipAmount()));
            valueText.setTextColor(getResources().getColor(R.color.green_700));
        } else {
            valueText.setText("Pending");
            valueText.setTextColor(getResources().getColor(R.color.gray_400));
        }

        container.addView(activityItem);
    }

    /**
     * Add a best tipping area item to the container
     */
    private void addBestTippingArea(LinearLayout container, Address address) {
        if (container == null || !isAdded()) return;

        View areaItem = LayoutInflater.from(getContext()).inflate(R.layout.item_tipping_area, container, false);

        TextView addressText = areaItem.findViewById(R.id.area_address);
        TextView statsText = areaItem.findViewById(R.id.area_stats);
        TextView tipText = areaItem.findViewById(R.id.area_tip);

        String fullAddress = address.getFullAddress();
        String shortAddress = fullAddress.length() > 30 ? fullAddress.substring(0, 27) + "..." : fullAddress;

        addressText.setText(shortAddress);
        statsText.setText(address.getDeliveryCount() + " deliveries");
        tipText.setText(String.format("$%.2f", address.getAverageTip()));

        // Set color based on tip amount
        if (address.getAverageTip() >= 8.0) {
            tipText.setTextColor(getResources().getColor(R.color.green_700));
        } else if (address.getAverageTip() >= 5.0) {
            tipText.setTextColor(getResources().getColor(R.color.yellow_500));
        } else {
            tipText.setTextColor(getResources().getColor(R.color.red_500));
        }

        container.addView(areaItem);
    }

    /**
     * Add an empty state view to a container
     */
    private void addEmptyStateView(LinearLayout container, String message) {
        if (container == null || !isAdded()) return;

        TextView emptyView = new TextView(getContext());
        emptyView.setText(message);
        emptyView.setTextColor(getResources().getColor(R.color.gray_400));
        emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyView.setPadding(0, 16, 0, 16);

        container.addView(emptyView);
    }

    /**
     * Callback interface for period statistics
     */
    interface StatisticsCallback {
        void onStatisticsLoaded(int totalDeliveries, double totalTips, int tippedDeliveries, int pendingTips);
    }
}