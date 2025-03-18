package com.autogratuity.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.data.model.Address;
import com.autogratuity.ui.address.adapters.AddressesAdapter;
import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment for displaying and managing addresses
 * Fully implements MVVM pattern with AddressViewModel
 */
public class AddressesFragment extends Fragment implements AddressesAdapter.OnAddressClickListener {
    private static final String TAG = "AddressesFragment";

    // UI elements
    private RecyclerView recyclerView;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private TextView errorView;
    private FloatingActionButton fabAddAddress;

    // Data
    private AddressViewModel viewModel;
    private AddressesAdapter adapter;

    /**
     * Factory method to create a new instance of the fragment
     */
    public static AddressesFragment newInstance() {
        return new AddressesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_addresses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel using factory with repository
        viewModel = new ViewModelProvider(this, 
                RepositoryViewModelFactory.fromRepositoryProvider())
                .get(AddressViewModel.class);

        // Initialize UI elements
        initializeViews(view);
        
        // Set up RecyclerView with the updated adapter
        setupRecyclerView();
        
        // Observe ViewModel data changes
        observeViewModel();
        
        // Load data
        viewModel.loadAddresses();
    }

    /**
     * Initialize UI elements
     */
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_addresses);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyView = view.findViewById(R.id.empty_view);
        errorView = view.findViewById(R.id.error_view);
        fabAddAddress = view.findViewById(R.id.fab_add_address);
        
        // Set up FAB click listener
        fabAddAddress.setOnClickListener(v -> {
            // Launch add address activity
            // TODO: Implement add address functionality
            Toast.makeText(getContext(), "Add address functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Set up RecyclerView with adapter
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AddressesAdapter(getContext(), this);
        
        // Connect adapter with ViewModel
        adapter.setViewModel(viewModel);
        
        recyclerView.setAdapter(adapter);
    }

    /**
     * Observe changes from ViewModel
     */
    private void observeViewModel() {
        // Observe addresses data
        viewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                // Update adapter with new data
                adapter.submitList(addresses);
                
                // Show/hide empty view
                emptyView.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(addresses.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        
        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::showLoading);
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message
                errorView.setText("Error loading addresses: " + error.getMessage());
                errorView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                errorView.setVisibility(View.GONE);
            }
        });
        
        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        
        if (isLoading) {
            errorView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up real-time address updates
        viewModel.observeAddresses();
    }

    // AddressesAdapter.OnAddressClickListener Implementation

    @Override
    public void onAddressClick(Address address) {
        // The ViewModel.selectAddress() is now called from the adapter
        // Just handle the navigation here
        
        // TODO: Navigate to address detail screen
        Toast.makeText(getContext(), "Address clicked: " + address.getFullAddress(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDeliveriesClick(Address address) {
        // The ViewModel.selectAddress() is now called from the adapter
        // Just handle the navigation here
        
        // TODO: Navigate to deliveries for this address
        Toast.makeText(getContext(), "View deliveries for: " + address.getFullAddress(), Toast.LENGTH_SHORT).show();
    }
}