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
import com.autogratuity.models.Delivery;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DeliveriesFragment extends Fragment {
    private RecyclerView recyclerView;
    // We'll add adapter implementation later

    public static DeliveriesFragment newInstance() {
        return new DeliveriesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliveries, container, false);
        recyclerView = view.findViewById(R.id.recycler_deliveries);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        // Will implement FirestoreRecyclerAdapter here
    }
}