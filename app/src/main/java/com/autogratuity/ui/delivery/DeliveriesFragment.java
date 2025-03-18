package com.autogratuity.ui.delivery;

import android.os.Bundle;
import android.util.Log;
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
import com.autogratuity.data.model.Delivery;
import com.autogratuity.ui.common.RepositoryViewModelFactory;
import com.autogratuity.ui.delivery.adapters.DeliveriesAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the list of deliveries.
 * Implements the MVVM pattern with DeliveryViewModel for efficient data handling.
 */
public class DeliveriesFragment extends Fragment {
    private static final String TAG = "DeliveriesFragment";
    
    // UI elements
    private RecyclerView recyclerView;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private TextView errorView;
    private FloatingActionButton fabAddDelivery;
    
    // Data
    private DeliveryViewModel viewModel;
    private DeliveriesAdapter adapter;
    private List<Delivery> deliveries = new ArrayList<>();
    
    /**
     * Factory method to create a new instance of the fragment
     */
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
        
        // Initialize ViewModel using factory with repository
        viewModel = new ViewModelProvider(this,
                RepositoryViewModelFactory.fromRepositoryProvider())
                .get(DeliveryViewModel.class);
        
        // Initialize UI components
        initializeViews(view);
        
        // Set up RecyclerView and adapter
        setupRecyclerView();
        
        // Observe ViewModel LiveData
        observeViewModel();
        
        // Load initial data
        viewModel.loadDeliveries();
    }
    
    /**
     * Initialize UI elements
     */
    private void initializeViews(View view) {
        // Find views
        recyclerView = view.findViewById(R.id.deliveries_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyView = view.findViewById(R.id.empty_view);
        errorView = view.findViewById(R.id.error_view);
        fabAddDelivery = view.findViewById(R.id.fab_add_delivery);
        
        // Add loading, empty, and error views if they don't exist
        if (loadingIndicator == null) {
            addMissingViews(view);
        }
        
        // Set up FAB click listener
        if (fabAddDelivery != null) {
            fabAddDelivery.setOnClickListener(v -> {
                // Navigate to add delivery screen
                // TODO: Implement add delivery navigation
                Toast.makeText(getContext(), "Add delivery functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * Add missing views if they don't exist in the layout
     * This ensures compatibility with the existing layout
     */
    private void addMissingViews(View rootView) {
        ViewGroup parent = (ViewGroup) rootView;
        
        // Add loading indicator if it doesn't exist
        if (loadingIndicator == null) {
            loadingIndicator = new ProgressBar(getContext());
            loadingIndicator.setId(View.generateViewId());
            loadingIndicator.setVisibility(View.GONE);
            
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            loadingIndicator.setLayoutParams(params);
            parent.addView(loadingIndicator);
        }
        
        // Add error view if it doesn't exist
        if (errorView == null) {
            errorView = new TextView(getContext());
            errorView.setId(View.generateViewId());
            errorView.setVisibility(View.GONE);
            errorView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            errorView.setLayoutParams(params);
            parent.addView(errorView);
        }
    }
    
    /**
     * Set up RecyclerView and adapter
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Create delivery click listener
        DeliveriesAdapter.OnDeliveryClickListener clickListener = delivery -> {
            // Set selected delivery in ViewModel
            viewModel.selectDelivery(delivery);
            
            // Handle delivery click
            // TODO: Navigate to delivery details
            Toast.makeText(getContext(), "Delivery clicked: " + delivery.getOrderId(), Toast.LENGTH_SHORT).show();
        };
        
        // Create and set adapter
        adapter = new DeliveriesAdapter(deliveries, clickListener);
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Observe changes from ViewModel
     */
    private void observeViewModel() {
        // Observe deliveries data
        viewModel.getDeliveries().observe(getViewLifecycleOwner(), this::onDeliveriesLoaded);
        
        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::showLoading);
        
        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                onError(error);
            } else {
                if (errorView != null) {
                    errorView.setVisibility(View.GONE);
                }
            }
        });
        
        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Handle loaded deliveries
     */
    private void onDeliveriesLoaded(List<Delivery> loadedDeliveries) {
        // Update adapter with new data if not null
        if (loadedDeliveries != null) {
            deliveries.clear();
            deliveries.addAll(loadedDeliveries);
            adapter.updateDeliveries(loadedDeliveries);
            
            // Show/hide empty view
            if (deliveries.isEmpty()) {
                showEmptyState();
            } else {
                showContent();
            }
        }
    }
    
    /**
     * Handle error loading deliveries
     */
    private void onError(Throwable error) {
        Log.e(TAG, "Error loading deliveries", error);
        
        // Show error message
        showError(error.getMessage());
    }
    
    /**
     * Show loading state
     */
    private void showLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
            
            if (errorView != null) {
                errorView.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Show content (recycler view)
     */
    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show empty state
     */
    private void showEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // If no empty view exists, show recycler view with no items
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show error state
     */
    private void showError(String message) {
        if (errorView != null) {
            errorView.setText(message != null ? message : "Unknown error occurred");
            errorView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        } else {
            // If no error view exists, show toast instead
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            }
            
            // Show empty recycler view
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Start observing deliveries when the fragment is visible
        viewModel.observeDeliveries();
    }
}
