package com.autogratuity.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.adapters.DeliveriesAdapter;
import com.autogratuity.models.Delivery;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.RepositoryCallback;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveriesFragment extends Fragment implements DeliveriesAdapter.OnDeliveryClickListener {
    private static final String TAG = "DeliveriesFragment";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private DeliveriesAdapter adapter;
    private List<Delivery> deliveries = new ArrayList<>();

    private FirestoreRepository repository;

    public static DeliveriesFragment newInstance() {
        return new DeliveriesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deliveries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        repository = FirestoreRepository.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_deliveries);
        emptyView = view.findViewById(R.id.empty_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeliveriesAdapter(getContext(), deliveries, this);
        recyclerView.setAdapter(adapter);

        // Load deliveries
        loadDeliveries();
    }

    private void loadDeliveries() {
        // Show loading state
        if (deliveries.isEmpty()) {
            showEmptyView();
        }

        // Clear existing deliveries
        deliveries.clear();

        // Load deliveries using repository
        repository.getAllDeliveries()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyView();
                        return;
                    }

                    // Process results
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Delivery delivery = Delivery.fromDocument(document);
                        deliveries.add(delivery);
                    }

                    // Update adapter
                    adapter.notifyDataSetChanged();

                    // Show or hide empty view
                    if (deliveries.isEmpty()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                        Log.d(TAG, "Loaded " + deliveries.size() + " deliveries");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading deliveries", e);
                    if (isAdded()) {
                        showEmptyView();
                        Toast.makeText(getContext(), "Error loading deliveries: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmptyView() {
        if (emptyView != null && recyclerView != null) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyView() {
        if (emptyView != null && recyclerView != null) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeliveryClick(Delivery delivery) {
        if (delivery == null || !isAdded()) return;

        // Show delivery details dialog
        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(delivery.getOrderId()).append("\n");
        details.append("Address: ").append(delivery.getAddress()).append("\n");

        if (delivery.getDeliveryDate() != null) {
            details.append("Delivery Date: ").append(delivery.getDeliveryDate().toDate()).append("\n");
        }

        if (delivery.isTipped()) {
            details.append("Tip Amount: $").append(String.format("%.2f", delivery.getTipAmount())).append("\n");
        } else {
            details.append("Tip Status: Pending\n");
        }

        if (delivery.isDoNotDeliver()) {
            details.append("\nFlagged as DO NOT DELIVER");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delivery Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onEditDelivery(Delivery delivery) {
        if (delivery == null || !isAdded()) return;

        // Show edit dialog to update tip amount
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_delivery, null);

        // Set up dialog fields
        com.google.android.material.textfield.TextInputEditText orderIdInput = dialogView.findViewById(R.id.order_id_input);
        orderIdInput.setText(delivery.getOrderId());
        orderIdInput.setEnabled(false);

        com.google.android.material.textfield.TextInputEditText addressInput = dialogView.findViewById(R.id.address_input);
        addressInput.setText(delivery.getAddress());

        com.google.android.material.textfield.TextInputEditText tipAmountInput = dialogView.findViewById(R.id.tip_amount_input);
        if (delivery.isTipped()) {
            tipAmountInput.setText(String.format("%.2f", delivery.getTipAmount()));
        }

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setTitle("Update Delivery")
                .setPositiveButton("Update", (dialog, which) -> {
                    // Get updated values
                    String newAddress = addressInput.getText().toString().trim();
                    String tipStr = tipAmountInput.getText().toString().trim();

                    // Check if there are changes
                    boolean hasChanges = false;

                    // Handle address update
                    if (!newAddress.isEmpty() && !newAddress.equals(delivery.getAddress())) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("address", newAddress);

                        repository.updateDelivery(delivery.getId(), updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Address updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating address", e);
                                    Toast.makeText(getContext(), "Error updating address", Toast.LENGTH_SHORT).show();
                                });

                        hasChanges = true;
                    }

                    // Handle tip update
                    if (!tipStr.isEmpty()) {
                        try {
                            double tipAmount = Double.parseDouble(tipStr);

                            if (tipAmount != delivery.getTipAmount()) {
                                repository.updateDeliveryWithTip(delivery.getId(), tipAmount)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Tip updated successfully");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error updating tip", e);
                                            Toast.makeText(getContext(), "Error updating tip", Toast.LENGTH_SHORT).show();
                                        });

                                hasChanges = true;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Invalid tip amount", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (hasChanges) {
                        Toast.makeText(getContext(), "Updating delivery...", Toast.LENGTH_SHORT).show();

                        // Refresh after a short delay to allow updates to complete
                        new android.os.Handler().postDelayed(this::loadDeliveries, 1000);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDeliveries();  // Refresh when returning to this fragment
    }
}