package com.autogratuity.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.R;
import com.autogratuity.data.model.Address;
import com.autogratuity.data.model.Delivery;
import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.utils.MapManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Fragment for displaying the map with markers for deliveries and tips
 * Updated to use ViewModel architecture pattern
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";

    // ViewModel
    private MapViewModel viewModel;
    
    // Google Maps related
    private GoogleMap googleMap;
    private MapManager mapManager;
    
    // For MapManager initialization
    private AddressRepository addressRepository;
    private DeliveryRepository deliveryRepository;
    
    // UI elements
    private Button btnShowRecent;
    private Button btnShowAll;
    private FloatingActionButton fabRefresh;

    public static MapFragment newInstance() {
        return new MapFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        
        // Initialize repositories (needed for MapManager)
        addressRepository = RepositoryProvider.getAddressRepository();
        deliveryRepository = RepositoryProvider.getDeliveryRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize UI components
        initializeUI(view);
        
        // Observe ViewModel
        observeViewModel();
    }
    
    /**
     * Initialize UI components and set up event handlers
     */
    private void initializeUI(View view) {
        btnShowRecent = view.findViewById(R.id.btnShowRecent);
        btnShowAll = view.findViewById(R.id.btnShowAll);
        fabRefresh = view.findViewById(R.id.fabRefresh);
        
        btnShowRecent.setOnClickListener(v -> {
            viewModel.loadRecentDeliveries(50);
            btnShowRecent.setActivated(true);
            btnShowAll.setActivated(false);
        });
        
        btnShowAll.setOnClickListener(v -> {
            viewModel.loadAddresses(100);
            btnShowRecent.setActivated(false);
            btnShowAll.setActivated(true);
        });
        
        fabRefresh.setOnClickListener(v -> {
            viewModel.refreshMapData();
        });
    }
    
    /**
     * Observe LiveData from ViewModel
     */
    private void observeViewModel() {
        // Observe addresses
        viewModel.getAddresses().observe(getViewLifecycleOwner(), this::displayAddressMarkers);
        
        // Observe deliveries
        viewModel.getDeliveries().observe(getViewLifecycleOwner(), this::displayDeliveryMarkers);
        
        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Update UI to show loading state if needed
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
     * Display markers for addresses
     */
    private void displayAddressMarkers(List<Address> addresses) {
        if (mapManager != null && addresses != null) {
            // Clear existing markers
            mapManager.clearMarkers();
            
            // Load address markers
            for (Address address : addresses) {
                if (address.getLocation() != null) {
                    // MapManager will handle adding the markers
                }
            }
            
            // Let MapManager handle it directly
            mapManager.loadAddressMarkers(addresses.size());
        }
    }
    
    /**
     * Display markers for deliveries
     */
    private void displayDeliveryMarkers(List<Delivery> deliveries) {
        if (mapManager != null && deliveries != null) {
            // Clear existing markers
            mapManager.clearMarkers();
            
            // Let MapManager handle loading the delivery markers
            mapManager.loadRecentImportMarkers();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        
        try {
            // Initialize map manager with domain repositories
            mapManager = new MapManager(requireContext(), googleMap, addressRepository, deliveryRepository);
            
            // Load initial data from ViewModel
            viewModel.loadRecentDeliveries(50);
            btnShowRecent.setActivated(true);
            btnShowAll.setActivated(false);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing map manager", e);
            Toast.makeText(requireContext(), "Error initializing map", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Refresh the map data
     * Called when new data is imported
     */
    public void refreshMap() {
        viewModel.refreshMapData();
        Toast.makeText(requireContext(), "Map updated with new data", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh map when returning to this fragment
        if (mapManager != null) {
            viewModel.refreshMapData();
        }
    }
    
    @Override
    public void onDestroyView() {
        // Clean up map manager resources
        if (mapManager != null) {
            mapManager.dispose();
        }
        
        super.onDestroyView();
    }
}