package com.autogratuity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.models.Delivery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying deliveries in a RecyclerView
 */
public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder> {
    
    private final List<Delivery> deliveries;
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
        this.deliveries = deliveries;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
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
     * Update the list of deliveries and refresh the adapter
     */
    public void updateDeliveries(List<Delivery> newDeliveries) {
        deliveries.clear();
        deliveries.addAll(newDeliveries);
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for delivery items
     */
    class DeliveryViewHolder extends RecyclerView.ViewHolder {
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
            orderIdText.setText(String.format("Order #%s", delivery.getOrderId()));
            
            // Set address
            addressText.setText(delivery.getAddress());
            
            // Set delivery date
            if (delivery.getDeliveryDate() != null) {
                dateText.setText(dateFormat.format(delivery.getDeliveryDate().toDate()));
            } else {
                dateText.setText("N/A");
            }
            
            // Set tip amount
            if (delivery.getTipAmount() > 0) {
                tipText.setText(String.format(Locale.US, "$%.2f", delivery.getTipAmount()));
                tipText.setTextColor(itemView.getContext().getResources().getColor(R.color.green_700));
            } else {
                tipText.setText("No Tip");
                tipText.setTextColor(itemView.getContext().getResources().getColor(R.color.red_500));
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeliveryClick(delivery);
                }
            });
            
            // Set background color for "Do Not Deliver" addresses
            if (delivery.isDoNotDeliver()) {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.red_100));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent));
            }
        }
    }
}
