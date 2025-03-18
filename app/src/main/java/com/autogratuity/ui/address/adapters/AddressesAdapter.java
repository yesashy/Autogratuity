package com.autogratuity.ui.address.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.autogratuity.R;
import com.autogratuity.data.model.Address;
import com.autogratuity.ui.address.AddressViewModel;

import java.text.DecimalFormat;

/**
 * Adapter for displaying addresses in a RecyclerView
 * Updated to follow MVVM pattern and properly integrate with AddressViewModel
 */
public class AddressesAdapter extends ListAdapter<Address, AddressesAdapter.AddressViewHolder> {
    private final Context context;
    private final OnAddressClickListener listener;
    private final DecimalFormat currencyFormat = new DecimalFormat("$0.00");
    private AddressViewModel viewModel;

    /**
     * Interface for address click events
     */
    public interface OnAddressClickListener {
        void onAddressClick(Address address);
        void onViewDeliveriesClick(Address address);
    }

    /**
     * DiffUtil implementation for efficient RecyclerView updates
     */
    private static final DiffUtil.ItemCallback<Address> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Address>() {
                @Override
                public boolean areItemsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Constructor for AddressesAdapter
     *
     * @param context Context
     * @param addresses Initial list of addresses (can be empty)
     * @param listener Click listener
     */
    public AddressesAdapter(Context context, @NonNull OnAddressClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    /**
     * Set the ViewModel reference to enable proper data handling
     * 
     * @param viewModel AddressViewModel instance
     */
    public void setViewModel(AddressViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = getItem(position);

        // Set full address
        holder.addressText.setText(address.getFullAddress());

        // Get stats from appropriate fields
        double avgTip = 0;
        int deliveryCount = 0;
        boolean doNotDeliver = false;
        
        // Check if we have delivery stats
        if (address.getDeliveryStats() != null) {
            avgTip = address.getDeliveryStats().getAverageTip();
            deliveryCount = address.getDeliveryStats().getDeliveryCount();
        }

        // Check if we have metadata with doNotDeliver flag
        if (address.getMetadata() != null && address.getMetadata().getCustomData() != null) {
            Object dnpObj = address.getMetadata().getCustomData().get("doNotDeliver");
            if (dnpObj instanceof Boolean) {
                doNotDeliver = (Boolean) dnpObj;
            }
        }

        // Show average tip with color based on amount
        holder.averageTipText.setText(currencyFormat.format(avgTip));

        // Set color based on average tip amount
        if (avgTip >= 8.0) {
            holder.averageTipText.setTextColor(context.getResources().getColor(R.color.green_700));
        } else if (avgTip >= 5.0) {
            holder.averageTipText.setTextColor(context.getResources().getColor(R.color.yellow_500));
        } else if (avgTip > 0) {
            holder.averageTipText.setTextColor(context.getResources().getColor(R.color.red_500));
        } else {
            holder.averageTipText.setTextColor(context.getResources().getColor(R.color.gray_400));
        }

        // Show delivery count
        holder.deliveryCountText.setText(deliveryCount + " " +
                (deliveryCount == 1 ? "delivery" : "deliveries"));

        // Handle Do Not Deliver flag
        holder.doNotDeliverText.setVisibility(doNotDeliver ? View.VISIBLE : View.GONE);

        // Set favorite indicator if address is a favorite
        holder.favoriteIndicator.setVisibility(
                address.isFavorite() ? View.VISIBLE : View.GONE);

        // Handle click events with proper ViewModel integration
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                if (viewModel != null) {
                    // Use ViewModel to select the address
                    viewModel.selectAddress(address);
                }
                listener.onAddressClick(address);
            }
        });

        holder.viewDeliveriesButton.setOnClickListener(v -> {
            if (listener != null) {
                if (viewModel != null) {
                    // Use ViewModel to select the address
                    viewModel.selectAddress(address);
                }
                listener.onViewDeliveriesClick(address);
            }
        });

        // Handle favorite toggle
        holder.favoriteToggle.setOnClickListener(v -> {
            if (viewModel != null) {
                // Use ViewModel to toggle favorite status
                viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
            }
        });
    }

    /**
     * ViewHolder for address items
     */
    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView addressText, averageTipText, deliveryCountText, doNotDeliverText;
        View viewDeliveriesButton, favoriteToggle, favoriteIndicator;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressText = itemView.findViewById(R.id.text_address);
            averageTipText = itemView.findViewById(R.id.text_average_tip);
            deliveryCountText = itemView.findViewById(R.id.text_delivery_count);
            doNotDeliverText = itemView.findViewById(R.id.text_do_not_deliver);
            viewDeliveriesButton = itemView.findViewById(R.id.button_view_deliveries);
            favoriteToggle = itemView.findViewById(R.id.button_favorite);
            favoriteIndicator = itemView.findViewById(R.id.favorite_indicator);
        }
    }
}