package com.autogratuity.fragments;

import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.autogratuity.R;
import com.autogratuity.adapters.AddressesAdapter;
import com.autogratuity.data.model.Address;
import com.autogratuity.data.repository.DataRepository;
import com.autogratuity.data.repository.RepositoryProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment for displaying and managing addresses
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
    private DataRepository repository;
    private AddressesAdapter adapter;
    private List<Address> addresses = new ArrayList<>();
    private CompositeDisposable disposables = new CompositeDisposable();

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
        
        // Get repository instance
        repository = RepositoryProvider.getRepository();

        // Initialize UI elements
        initializeViews(view);
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Load data
        loadAddresses();
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
        adapter = new AddressesAdapter(getContext(), addresses, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Load addresses from repository
     */
    private void loadAddresses() {
        // Show loading indicator
        showLoading(true);
        
        // Clear any existing subscriptions
        disposables.clear();
        
        // Use repository to get addresses
        disposables.add(
            repository.getAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::onAddressesLoaded,
                    this::onError
                )
        );
    }

    /**
     * Set up real-time address updates
     */
    private void observeAddresses() {
        disposables.add(
            repository.observeAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::onAddressesLoaded,
                    this::onError
                )
        );
    }

    /**
     * Handle loaded addresses
     */
    private void onAddressesLoaded(List<Address> loadedAddresses) {
        // Hide loading indicator
        showLoading(false);
        
        // Update adapter with new data
        this.addresses.clear();
        this.addresses.addAll(loadedAddresses);
        adapter.notifyDataSetChanged();
        
        // Show/hide empty view
        emptyView.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(addresses.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Handle error loading addresses
     */
    private void onError(Throwable error) {
        Log.e(TAG, "Error loading addresses", error);
        
        // Hide loading indicator
        showLoading(false);
        
        // Show error message
        errorView.setText("Error loading addresses: " + error.getMessage());
        errorView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        errorView.setVisibility(View.GONE);
        
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up real-time address updates
        observeAddresses();
    }

    @Override
    public void onDestroyView() {
        // Clean up subscriptions to prevent memory leaks
        disposables.clear();
        super.onDestroyView();
    }

    // AddressesAdapter.OnAddressClickListener Implementation

    @Override
    public void onAddressClick(Address address) {
        // Navigate to address detail screen
        // TODO: Implement address detail screen navigation
        Toast.makeText(getContext(), "Address clicked: " + address.getFullAddress(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDeliveriesClick(Address address) {
        // Navigate to deliveries for this address
        // TODO: Implement deliveries for address screen navigation
        Toast.makeText(getContext(), "View deliveries for: " + address.getFullAddress(), Toast.LENGTH_SHORT).show();
    }
}