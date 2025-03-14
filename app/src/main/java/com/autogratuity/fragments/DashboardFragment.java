package com.autogratuity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.autogratuity.R;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;

/**
 * Dashboard fragment that displays summary statistics
 */
public class DashboardFragment extends Fragment {
    
    private IFirestoreRepository repository;
    
    /**
     * Factory method to create a new instance of the fragment
     */
    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize repository
        repository = FirestoreRepository.getInstance();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up UI components
        setupUI(view);
        
        // Load data
        loadData();
    }
    
    /**
     * Set up UI components
     */
    private void setupUI(View view) {
        // Set up UI elements here
    }
    
    /**
     * Load data from repository
     */
    private void loadData() {
        // Implement data loading
    }
    
    /**
     * Refresh data when requested
     */
    public void refreshData() {
        loadData();
    }
}
