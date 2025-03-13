// app/src/main/java/com/autogratuity/adapters/DeliveriesAdapter.java
package com.autogratuity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.models.Delivery;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder> {
    private List<Delivery> deliveries;
    private Context context;
    private OnDeliveryClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
        void onEditDelivery(Delivery delivery);
    }

    public DeliveriesAdapter(Context context, List<Delivery> deliveries, OnDeliveryClickListener listener) {
        this.context = context;
        this.deliveries = deliveries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Delivery delivery = deliveries.get(position);

        holder.orderIdText.setText("#" + delivery.getOrderId());
        holder.addressText.setText(delivery.getAddress());

        // Handle delivery date
        Timestamp deliveryDate = delivery.getDeliveryDate();
        if (deliveryDate != null) {
            holder.dateText.setText(dateFormat.format(deliveryDate.toDate()));
        } else {
            holder.dateText.setText("Unknown date");
        }

        // Handle completion date if available
        Timestamp completionDate = delivery.getDeliveryCompletedDate();
        if (completionDate != null) {
            holder.completionDateText.setText("Completed: " + dateFormat.format(completionDate.toDate()));
            holder.completionDateText.setVisibility(View.VISIBLE);
        } else {
            holder.completionDateText.setVisibility(View.GONE);
        }

        // Handle tip status
        if (delivery.isTipped()) {
            holder.tipText.setText(String.format("$%.2f", delivery.getTipAmount()));
            holder.tipText.setTextColor(context.getResources().getColor(R.color.green_700));
        } else {
            holder.tipText.setText("Pending");
            holder.tipText.setTextColor(context.getResources().getColor(R.color.gray_400));
        }

        // Handle Do Not Deliver flag
        if (delivery.isDoNotDeliver()) {
            holder.doNotDeliverText.setVisibility(View.VISIBLE);
        } else {
            holder.doNotDeliverText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeliveryClick(delivery);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditDelivery(delivery);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deliveries != null ? deliveries.size() : 0;
    }

    public void updateDeliveries(List<Delivery> newDeliveries) {
        this.deliveries = newDeliveries;
        notifyDataSetChanged();
    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, addressText, dateText, tipText, completionDateText, doNotDeliverText;
        View editButton;

        DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.text_order_id);
            addressText = itemView.findViewById(R.id.text_address);
            dateText = itemView.findViewById(R.id.text_date);
            tipText = itemView.findViewById(R.id.text_tip);
            completionDateText = itemView.findViewById(R.id.text_completion_date);
            doNotDeliverText = itemView.findViewById(R.id.text_do_not_deliver);
            editButton = itemView.findViewById(R.id.button_edit);
        }
    }
}