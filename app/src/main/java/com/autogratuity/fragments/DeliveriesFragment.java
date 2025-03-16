package com.autogratuity.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.adapters.DeliveriesAdapter;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.DataRepository;
import com.autogratuity.data.repository.RepositoryProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment for displaying the list of deliveries.
 * Implements the repository pattern with RxJava for efficient data handling.
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
    private DataRepository repository;
    private DeliveriesAdapter adapter;
    private List<Delivery> deliveries = new ArrayList<>();
    private CompositeDisposable disposables = new CompositeDisposable();
    
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
        
        // Get repository instance from provider
        repository = RepositoryProvider.getRepository();
        
        // Initialize UI components
        initializeViews(view);
        
        // Set up RecyclerView and adapter
        setupRecyclerView();
        
        // Load initial data
        loadDeliveries();
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
            // Handle delivery click
            // TODO: Navigate to delivery details
            Toast.makeText(getContext(), "Delivery clicked: " + delivery.getOrderId(), Toast.LENGTH_SHORT).show();
        };
        
        // Create and set adapter
        adapter = new DeliveriesAdapter(deliveries, clickListener);
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Load deliveries from repository
     */
    private void loadDeliveries() {
        // Show loading indicator
        showLoading(true);
        
        // Clear any existing subscriptions
        disposables.clear();
        
        // Use repository to get deliveries (limit to 50 recent deliveries)
        disposables.add(
            repository.getDeliveries(50, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::onDeliveriesLoaded,
                    this::onError
                )
        );
    }
    
    /**
     * Set up real-time delivery updates
     */
    private void observeDeliveries() {
        disposables.add(
            repository.observeDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::onDeliveriesLoaded,
                    this::onError
                )
        );
    }
    
    /**
     * Handle loaded deliveries
     */
    private void onDeliveriesLoaded(List<Delivery> loadedDeliveries) {
        // Hide loading indicator
        showLoading(false);
        
        // Update adapter with new data
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
    
    /**
     * Handle error loading deliveries
     */
    private void onError(Throwable error) {
        Log.e(TAG, "Error loading deliveries", error);
        
        // Hide loading indicator
        showLoading(false);
        
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
        observeDeliveries();
    }
    
    @Override
    public void onPause() {
        // Clear all subscriptions to prevent memory leaks
        disposables.clear();
        super.onPause();
    }
    
    @Override
    public void onDestroyView() {
        // Ensure all subscriptions are cleared
        disposables.clear();
        super.onDestroyView();
    }
}
