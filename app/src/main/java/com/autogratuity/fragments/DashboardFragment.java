package com.autogratuity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.autogratuity.R;
import com.autogratuity.views.StatCard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class DashboardFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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

        // Load data
        refreshData();
    }

    public void refreshData() {
        if (mAuth.getCurrentUser() == null || !isAdded()) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Get today's date
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        // Get 7 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date weekStart = calendar.getTime();

        // Get 30 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -23); // -7 - 23 = -30
        Date monthStart = calendar.getTime();

        // Format for currency
        DecimalFormat currencyFormat = new DecimalFormat("$0.00");

        // Query for today's data
        db.collection("deliveries")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("deliveryDate", com.google.firebase.Timestamp.now().toDate())
                .orderBy("deliveryDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    int totalDeliveries = queryDocumentSnapshots.size();
                    double totalTips = 0;
                    int tippedDeliveries = 0;
                    int pendingTips = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains("tipAmount")) {
                            double tipAmount = document.getDouble("tipAmount");
                            totalTips += tipAmount;
                            tippedDeliveries++;
                        } else {
                            pendingTips++;
                        }
                    }

                    double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

                    // Update UI for Today
                    todayTipsReceived.setStatValue(currencyFormat.format(totalTips));
                    todayPendingTips.setStatValue(String.valueOf(pendingTips));
                    todayAverageTip.setStatValue(currencyFormat.format(averageTip));
                    todayDeliveries.setStatValue(String.valueOf(totalDeliveries));
                });

        // Query for 7-day data
        db.collection("deliveries")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("deliveryDate", weekStart)
                .orderBy("deliveryDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    int totalDeliveries = queryDocumentSnapshots.size();
                    double totalTips = 0;
                    int tippedDeliveries = 0;
                    int pendingTips = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains("tipAmount")) {
                            double tipAmount = document.getDouble("tipAmount");
                            totalTips += tipAmount;
                            tippedDeliveries++;
                        } else {
                            pendingTips++;
                        }
                    }

                    double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

                    // Update UI for 7 Days
                    weekTipsReceived.setStatValue(currencyFormat.format(totalTips));
                    weekPendingTips.setStatValue(String.valueOf(pendingTips));
                    weekAverageTip.setStatValue(currencyFormat.format(averageTip));
                    weekDeliveries.setStatValue(String.valueOf(totalDeliveries));
                });

        // Query for 30-day data
        db.collection("deliveries")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("deliveryDate", monthStart)
                .orderBy("deliveryDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    int totalDeliveries = queryDocumentSnapshots.size();
                    double totalTips = 0;
                    int tippedDeliveries = 0;
                    int pendingTips = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains("tipAmount")) {
                            double tipAmount = document.getDouble("tipAmount");
                            totalTips += tipAmount;
                            tippedDeliveries++;
                        } else {
                            pendingTips++;
                        }
                    }

                    double averageTip = tippedDeliveries > 0 ? totalTips / tippedDeliveries : 0;

                    // Update UI for 30 Days
                    monthTipsReceived.setStatValue(currencyFormat.format(totalTips));
                    monthPendingTips.setStatValue(String.valueOf(pendingTips));
                    monthAverageTip.setStatValue(currencyFormat.format(averageTip));
                    monthDeliveries.setStatValue(String.valueOf(totalDeliveries));
                });

        // Query for best tipping areas
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("averageTip", 0)
                .orderBy("averageTip", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    // Update best tipping areas
                    // This would require additional UI elements that need to be added to the layout
                });
    }
}