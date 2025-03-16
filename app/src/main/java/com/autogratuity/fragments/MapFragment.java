package com.autogratuity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.autogratuity.R;
import com.autogratuity.repositories.CachedFirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;
import com.autogratuity.utils.MapManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment for displaying the map with markers for deliveries and tips
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapManager mapManager;
    private IFirestoreRepository repository;
    private Button btnShowRecent;
    private Button btnShowAll;
    private FloatingActionButton fabRefresh;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize repository
        repository = new CachedFirestoreRepository(requireContext());
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize UI components
        initializeUI(view);
    }
    
    /**
     * Initialize UI components and set up event handlers
     */
    private void initializeUI(View view) {
        btnShowRecent = view.findViewById(R.id.btnShowRecent);
        btnShowAll = view.findViewById(R.id.btnShowAll);
        fabRefresh = view.findViewById(R.id.fabRefresh);
        
        btnShowRecent.setOnClickListener(v -> {
            if (mapManager != null) {
                mapManager.loadRecentImportMarkers();
                Toast.makeText(requireContext(), "Showing recent imports", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnShowAll.setOnClickListener(v -> {
            if (mapManager != null) {
                mapManager.loadAddressMarkers(100);
                Toast.makeText(requireContext(), "Showing all locations", Toast.LENGTH_SHORT).show();
            }
        });
        
        fabRefresh.setOnClickListener(v -> {
            if (mapManager != null) {
                // Refresh the current view
                if (btnShowRecent.isActivated()) {
                    mapManager.loadRecentImportMarkers();
                } else {
                    mapManager.loadAddressMarkers(100);
                }
                Toast.makeText(requireContext(), "Refreshing map", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        
        // Initialize map manager
        mapManager = new MapManager(requireContext(), googleMap, repository);
        
        // Load initial data - show recent imports by default
        mapManager.loadRecentImportMarkers();
        btnShowRecent.setActivated(true);
        btnShowAll.setActivated(false);
    }
    
    /**
     * Refresh the map data
     * Called when new data is imported
     */
    public void refreshMap() {
        if (mapManager != null) {
            mapManager.loadRecentImportMarkers();
            Toast.makeText(requireContext(), "Map updated with new data", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh map when returning to this fragment
        if (mapManager != null) {
            mapManager.loadRecentImportMarkers();
        }
    }
}