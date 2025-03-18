package com.autogratuity.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.R;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.ui.dialog.DeliveryDialogViewModel;
import com.autogratuity.ui.dialog.DeliveryDialogViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog for viewing and editing delivery details.
 * Implements MVVM architecture using DeliveryDialogViewModel.
 */
public class DeliveryDetailDialog extends DialogFragment {

    private static final String ARG_DELIVERY_ID = "delivery_id";
    
    private DeliveryDialogViewModel viewModel;
    private Observer<Delivery> deliveryObserver;
    private Observer<Boolean> loadingObserver;
    private Observer<Throwable> errorObserver;
    private Observer<String> toastObserver;
    private Observer<Boolean> operationSuccessObserver;
    private String deliveryId;
    private Delivery delivery;
    
    // UI elements
    private TextView orderIdText;
    private TextView addressText;
    private TextView dateText;
    private TextView statusText;
    private TextInputEditText tipAmountInput;
    private TextInputLayout tipAmountLayout;
    private Button updateButton;
    private Button deleteButton;
    private Button closeButton;
    
    // Callback for when a delivery is updated
    public interface OnDeliveryUpdateListener {
        void onDeliveryUpdated(Delivery delivery);
        void onDeliveryDeleted(String deliveryId);
    }
    
    private OnDeliveryUpdateListener listener;
    
    /**
     * Factory method to create a new instance of the dialog
     */
    public static DeliveryDetailDialog newInstance(String deliveryId) {
        DeliveryDetailDialog fragment = new DeliveryDetailDialog();
        Bundle args = new Bundle();
        args.putString(ARG_DELIVERY_ID, deliveryId);
        fragment.setArguments(args);
        return fragment;
    }
    
    /**
     * Set the listener for delivery update events
     */
    public void setOnDeliveryUpdateListener(OnDeliveryUpdateListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel with factory
        viewModel = new ViewModelProvider(this, new DeliveryDialogViewModelFactory())
                .get(DeliveryDialogViewModel.class);
        
        // Get delivery ID from arguments
        if (getArguments() != null) {
            deliveryId = getArguments().getString(ARG_DELIVERY_ID);
        }
        
        if (deliveryId == null) {
            dismiss();
        }
        
        // Set up observers
        setupObservers();
    }
    
    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Delivery observer
        deliveryObserver = newDelivery -> {
            if (newDelivery != null) {
                delivery = newDelivery;
                updateUI(newDelivery);
            }
        };
        
        // Loading state observer
        loadingObserver = isLoading -> {
            if (updateButton != null && deleteButton != null) {
                updateButton.setEnabled(!isLoading);
                deleteButton.setEnabled(!isLoading);
            }
        };
        
        // Error observer
        errorObserver = throwable -> {
            if (throwable != null && getContext() != null) {
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        
        // Toast message observer
        toastObserver = message -> {
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        };
        
        // Operation success observer
        operationSuccessObserver = success -> {
            if (success != null && success) {
                // If deleting was successful, notify listener and dismiss
                if (listener != null && !viewModel.isLoading().getValue()) {
                    // If we're still showing the delivery data, this was an update
                    if (delivery != null) {
                        listener.onDeliveryUpdated(delivery);
                    } else {
                        // Otherwise it was a delete
                        listener.onDeliveryDeleted(deliveryId);
                        dismiss();
                    }
                }
            }
        };
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delivery_detail, null);
        
        // Initialize UI elements
        orderIdText = view.findViewById(R.id.order_id_text);
        addressText = view.findViewById(R.id.address_text);
        dateText = view.findViewById(R.id.date_text);
        statusText = view.findViewById(R.id.status_text);
        tipAmountInput = view.findViewById(R.id.tip_amount_input);
        tipAmountLayout = view.findViewById(R.id.tip_amount_layout);
        updateButton = view.findViewById(R.id.update_button);
        deleteButton = view.findViewById(R.id.delete_button);
        closeButton = view.findViewById(R.id.close_button);
        
        // Set up click listeners
        updateButton.setOnClickListener(v -> validateAndUpdateDelivery());
        deleteButton.setOnClickListener(v -> confirmAndDeleteDelivery());
        closeButton.setOnClickListener(v -> dismiss());
        
        // Load delivery data
        loadDelivery();
        
        // Create and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }
    
    /**
     * Load delivery data from ViewModel
     */
    private void loadDelivery() {
        if (deliveryId != null) {
            viewModel.loadDelivery(deliveryId);
        }
    }
    
    /**
     * Update UI with delivery data
     */
    private void updateUI(Delivery delivery) {
        this.delivery = delivery;
        
        // Set order ID
        orderIdText.setText(String.format("Order #%s", delivery.getOrderId()));
        
        // Set address
        if (delivery.getAddress() != null) {
            addressText.setText(delivery.getAddress().getFullAddress());
        } else {
            addressText.setText("Unknown Address");
        }
        
        // Set date
        Date deliveryDate = getDeliveryDate(delivery);
        if (deliveryDate != null) {
            DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
            dateText.setText(dateFormat.format(deliveryDate));
        } else {
            dateText.setText("Unknown Date");
        }
        
        // Set status
        if (delivery.getStatus() != null) {
            String statusStr = delivery.getStatus().getState();
            statusStr = statusStr.substring(0, 1).toUpperCase() + statusStr.substring(1);
            
            if (delivery.getStatus().isCompleted()) {
                statusStr += " / Completed";
            }
            
            if (delivery.getStatus().isTipped()) {
                statusStr += " / Tipped";
            }
            
            statusText.setText(statusStr);
        } else {
            statusText.setText("Unknown Status");
        }
        
        // Set tip amount
        if (delivery.getAmounts() != null) {
            double tipAmount = delivery.getAmounts().getTipAmount();
            if (tipAmount > 0) {
                tipAmountInput.setText(String.format(Locale.US, "%.2f", tipAmount));
            } else {
                tipAmountInput.setText("");
            }
        }
    }
    
    /**
     * Get the delivery date from various timestamp fields
     */
    private Date getDeliveryDate(Delivery delivery) {
        if (delivery.getTimes() != null) {
            if (delivery.getTimes().getCompletedAt() != null) {
                return delivery.getTimes().getCompletedAt();
            } else if (delivery.getTimes().getPickedUpAt() != null) {
                return delivery.getTimes().getPickedUpAt();
            } else if (delivery.getTimes().getAcceptedAt() != null) {
                return delivery.getTimes().getAcceptedAt();
            } else if (delivery.getTimes().getOrderedAt() != null) {
                return delivery.getTimes().getOrderedAt();
            }
        }
        
        if (delivery.getMetadata() != null && delivery.getMetadata().getCreatedAt() != null) {
            return delivery.getMetadata().getCreatedAt();
        }
        
        return null;
    }
    
    /**
     * Validate and update the delivery tip amount
     */
    private void validateAndUpdateDelivery() {
        // Clear any previous errors
        tipAmountLayout.setError(null);
        
        // Get tip amount
        String tipAmountText = tipAmountInput.getText() != null ? tipAmountInput.getText().toString().trim() : "";
        double tipAmount = 0;
        
        // Validate tip amount
        boolean isValid = true;
        if (!TextUtils.isEmpty(tipAmountText)) {
            try {
                tipAmount = Double.parseDouble(tipAmountText);
                if (tipAmount < 0) {
                    tipAmountLayout.setError("Tip amount cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tipAmountLayout.setError("Invalid tip amount");
                isValid = false;
            }
        }
        
        // Update delivery if valid
        if (isValid) {
            viewModel.updateTipAmount(deliveryId, tipAmount);
        }
    }
    
    /**
     * Confirm and delete the delivery
     */
    private void confirmAndDeleteDelivery() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Delivery")
            .setMessage("Are you sure you want to delete this delivery? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteDelivery())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Delete the delivery
     */
    private void deleteDelivery() {
        // Set delivery to null to indicate we're deleting
        delivery = null;
        viewModel.deleteDelivery(deliveryId);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Start observing ViewModel LiveData
        viewModel.getDelivery().observe(this, deliveryObserver);
        viewModel.isLoading().observe(this, loadingObserver);
        viewModel.getError().observe(this, errorObserver);
        viewModel.getToastMessage().observe(this, toastObserver);
        viewModel.getOperationSuccess().observe(this, operationSuccessObserver);
        
        // Load delivery data
        loadDelivery();
    }
    
    @Override
    public void onPause() {
        // Stop observing LiveData
        viewModel.getDelivery().removeObserver(deliveryObserver);
        viewModel.isLoading().removeObserver(loadingObserver);
        viewModel.getError().removeObserver(errorObserver);
        viewModel.getToastMessage().removeObserver(toastObserver);
        viewModel.getOperationSuccess().removeObserver(operationSuccessObserver);
        
        super.onPause();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
