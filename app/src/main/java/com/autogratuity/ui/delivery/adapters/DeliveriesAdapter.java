package com.autogratuity.ui.delivery.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.data.model.Delivery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.DiffUtil;

/**
 * Adapter for displaying deliveries in a RecyclerView.
 * Updated to work with the new repository pattern and Delivery model.
 */
public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder> {
    
    private List<Delivery> deliveries;
    private final OnDeliveryClickListener listener;
    private final DateFormat dateFormat;
    
    /**
     * Interface for handling delivery item clicks
     */
    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
    }
    
    /**
     * Constructor with deliveries and click listener
     */
    public DeliveriesAdapter(List<Delivery> deliveries, OnDeliveryClickListener listener) {
        this.deliveries = new ArrayList<>(deliveries);
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Delivery delivery = deliveries.get(position);
        holder.bind(delivery, listener);
    }
    
    @Override
    public int getItemCount() {
        return deliveries.size();
    }
    
    /**
     * Update the list of deliveries and refresh the adapter using DiffUtil
     * for efficient updates with animations
     */
    public void updateDeliveries(List<Delivery> newDeliveries) {
        // Calculate the difference between old and new lists
        DeliveryDiffCallback diffCallback = new DeliveryDiffCallback(this.deliveries, newDeliveries);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        
        // Update the data
        this.deliveries = new ArrayList<>(newDeliveries);
        
        // Dispatch the updates to the adapter
        diffResult.dispatchUpdatesTo(this);
    }
    
    /**
     * ViewHolder for delivery items
     */
    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final TextView orderIdText;
        private final TextView addressText;
        private final TextView dateText;
        private final TextView tipText;
        
        DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.order_id_text);
            addressText = itemView.findViewById(R.id.address_text);
            dateText = itemView.findViewById(R.id.date_text);
            tipText = itemView.findViewById(R.id.tip_text);
        }
        
        void bind(final Delivery delivery, final OnDeliveryClickListener listener) {
            // Set order ID
            String orderId = delivery.getOrderId();
            if (orderId != null && !orderId.isEmpty()) {
                orderIdText.setText(String.format("Order #%s", orderId));
            } else {
                orderIdText.setText("Delivery");
            }
            
            // Set address
            if (delivery.getAddress() != null) {
                addressText.setText(delivery.getAddress().getFullAddress());
            } else {
                addressText.setText("Unknown Address");
            }
            
            // Set delivery date based on which timestamp is available
            Date deliveryDate = null;
            if (delivery.getTimes() != null) {
                if (delivery.getTimes().getCompletedAt() != null) {
                    deliveryDate = delivery.getTimes().getCompletedAt();
                } else if (delivery.getTimes().getPickedUpAt() != null) {
                    deliveryDate = delivery.getTimes().getPickedUpAt();
                } else if (delivery.getTimes().getAcceptedAt() != null) {
                    deliveryDate = delivery.getTimes().getAcceptedAt();
                } else if (delivery.getTimes().getOrderedAt() != null) {
                    deliveryDate = delivery.getTimes().getOrderedAt();
                }
            }
            
            if (deliveryDate != null) {
                dateText.setText(dateFormat.format(deliveryDate));
            } else if (delivery.getMetadata() != null && delivery.getMetadata().getCreatedAt() != null) {
                dateText.setText(dateFormat.format(delivery.getMetadata().getCreatedAt()));
            } else {
                dateText.setText("Unknown Date");
            }
            
            // Set tip amount
            double tipAmount = 0;
            if (delivery.getAmounts() != null) {
                tipAmount = delivery.getAmounts().getTipAmount();
            }
            
            if (tipAmount > 0) {
                tipText.setText(String.format(Locale.US, "$%.2f", tipAmount));
                tipText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else {
                tipText.setText("No Tip");
                tipText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light));
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeliveryClick(delivery);
                }
            });
            
            // Set background color based on delivery status
            if (delivery.getStatus() != null) {
                if (delivery.getStatus().isCompleted() && delivery.getStatus().isTipped()) {
                    // Completed and tipped - normal background
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                } else if (!delivery.getStatus().isCompleted() && "canceled".equals(delivery.getStatus().getState())) {
                    // Canceled delivery - light red background
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light));
                } else if (delivery.getDisputeInfo() != null && delivery.getDisputeInfo().isHasDispute()) {
                    // Disputed delivery - light orange background
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_light));
                }
            }
        }
    }
}