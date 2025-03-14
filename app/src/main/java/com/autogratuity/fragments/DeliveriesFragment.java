package com.autogratuity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.adapters.DeliveriesAdapter;
import com.autogratuity.models.Delivery;
import com.autogratuity.repositories.FirestoreRepository;
import com.autogratuity.repositories.IFirestoreRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the list of deliveries
 */
public class DeliveriesFragment extends Fragment {
    
    private IFirestoreRepository repository;
    private RecyclerView deliveriesRecyclerView;
    private DeliveriesAdapter adapter;
    
    /**
     * Factory method to create a new instance of the fragment
     */
    public static DeliveriesFragment newInstance() {
        return new DeliveriesFragment();
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
        return inflater.inflate(R.layout.fragment_deliveries, container, false);
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
        deliveriesRecyclerView = view.findViewById(R.id.deliveries_recycler_view);
        deliveriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Create a click listener for deliveries
        DeliveriesAdapter.OnDeliveryClickListener clickListener = delivery -> {
            // Handle delivery click (show details, etc.)
        };
        
        // Set up adapter with empty list initially
        adapter = new DeliveriesAdapter(new ArrayList<>(), clickListener);
        deliveriesRecyclerView.setAdapter(adapter);
    }
    
    /**
     * Load data from repository
     */
    private void loadData() {
        if (getContext() == null) return;
        
        // Get recent deliveries (limit to 50)
        repository.getRecentDeliveries(50)
                .addOnSuccessListener(querySnapshot -> {
                    List<Delivery> deliveries = new ArrayList<>();
                    
                    // Convert query results to Delivery objects
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Delivery delivery = Delivery.fromDocument(document);
                        if (delivery != null) {
                            deliveries.add(delivery);
                        }
                    }
                    
                    // Update adapter with new data
                    adapter.updateDeliveries(deliveries);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }
}
