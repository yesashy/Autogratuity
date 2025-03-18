package com.autogratuity.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.R;
import com.autogratuity.data.model.Delivery;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.autogratuity.ui.dialog.DeliveryDialogViewModel;
import com.autogratuity.ui.dialog.DeliveryDialogViewModelFactory;

/**
 * Dialog for adding a new delivery.
 * Implements MVVM architecture using DeliveryDialogViewModel.
 */
public class AddDeliveryDialog extends DialogFragment {

    private DeliveryDialogViewModel viewModel;
    private Observer<Boolean> loadingObserver;
    private Observer<Throwable> errorObserver;
    private Observer<String> toastObserver;
    private Observer<Boolean> operationSuccessObserver;
    
    // UI elements
    private TextInputEditText orderIdInput;
    private TextInputEditText addressInput;
    private TextInputEditText tipAmountInput;
    private TextInputLayout orderIdLayout;
    private TextInputLayout addressLayout;
    private TextInputLayout tipAmountLayout;
    private Button saveButton;
    private Button cancelButton;
    
    // Callback for when a delivery is added
    public interface OnDeliveryAddedListener {
        void onDeliveryAdded(Delivery delivery);
    }
    
    private OnDeliveryAddedListener listener;
    
    /**
     * Set the listener for delivery added events
     */
    public void setOnDeliveryAddedListener(OnDeliveryAddedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel with factory
        viewModel = new ViewModelProvider(this, new DeliveryDialogViewModelFactory())
                .get(DeliveryDialogViewModel.class);
        
        // Set up observers
        setupObservers();
    }
    
    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Loading state observer
        loadingObserver = isLoading -> {
            if (saveButton != null) {
                saveButton.setEnabled(!isLoading);
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
                // Notify listener if delivery was added successfully
                Delivery delivery = viewModel.getDelivery().getValue();
                if (delivery != null && listener != null) {
                    listener.onDeliveryAdded(delivery);
                }
                // Dismiss the dialog
                dismiss();
            }
        };
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_delivery, null);
        
        // Initialize UI elements
        orderIdInput = view.findViewById(R.id.order_id_input);
        addressInput = view.findViewById(R.id.address_input);
        tipAmountInput = view.findViewById(R.id.tip_amount_input);
        orderIdLayout = view.findViewById(R.id.order_id_layout);
        addressLayout = view.findViewById(R.id.address_layout);
        tipAmountLayout = view.findViewById(R.id.tip_amount_layout);
        saveButton = view.findViewById(R.id.save_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        
        // Set up click listeners
        cancelButton.setOnClickListener(v -> dismiss());
        saveButton.setOnClickListener(v -> validateAndSaveDelivery());
        
        // Create and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();
    }
    
    /**
     * Validate inputs and save the delivery if valid
     */
    private void validateAndSaveDelivery() {
        // Clear any previous errors
        orderIdLayout.setError(null);
        addressLayout.setError(null);
        tipAmountLayout.setError(null);
        
        // Get input values
        String orderId = orderIdInput.getText() != null ? orderIdInput.getText().toString().trim() : "";
        String addressText = addressInput.getText() != null ? addressInput.getText().toString().trim() : "";
        String tipAmountText = tipAmountInput.getText() != null ? tipAmountInput.getText().toString().trim() : "";
        
        // Validate inputs
        boolean isValid = true;
        
        if (TextUtils.isEmpty(addressText)) {
            addressLayout.setError("Address is required");
            isValid = false;
        }
        
        double tipAmount = 0;
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
        
        // If inputs are valid, create and save the delivery using ViewModel
        if (isValid) {
            saveButton.setEnabled(false);
            viewModel.createDeliveryWithAddress(orderId, addressText, tipAmount);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Start observing ViewModel LiveData
        viewModel.isLoading().observe(this, loadingObserver);
        viewModel.getError().observe(this, errorObserver);
        viewModel.getToastMessage().observe(this, toastObserver);
        viewModel.getOperationSuccess().observe(this, operationSuccessObserver);
    }
    
    @Override
    public void onPause() {
        // Stop observing LiveData to prevent memory leaks
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
