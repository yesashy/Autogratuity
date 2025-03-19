package com.autogratuity.ui.delivery.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.autogratuity.data.model.Delivery;

import java.util.List;
import java.util.Objects;

/**
 * DiffUtil callback implementation for Delivery objects.
 * Used to efficiently update the RecyclerView when the list of deliveries changes.
 */
public class DeliveryDiffCallback extends DiffUtil.Callback {
    
    private final List<Delivery> oldDeliveries;
    private final List<Delivery> newDeliveries;
    
    /**
     * Constructor with old and new delivery lists
     */
    public DeliveryDiffCallback(List<Delivery> oldDeliveries, List<Delivery> newDeliveries) {
        this.oldDeliveries = oldDeliveries;
        this.newDeliveries = newDeliveries;
    }
    
    @Override
    public int getOldListSize() {
        return oldDeliveries != null ? oldDeliveries.size() : 0;
    }
    
    @Override
    public int getNewListSize() {
        return newDeliveries != null ? newDeliveries.size() : 0;
    }
    
    /**
     * Called to check whether two objects represent the same item.
     * For deliveries, we compare the deliveryId which is the unique identifier.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Delivery oldDelivery = oldDeliveries.get(oldItemPosition);
        Delivery newDelivery = newDeliveries.get(newItemPosition);
        
        if (oldDelivery == null || newDelivery == null) {
            return false;
        }
        
        // Compare by delivery ID
        String oldId = oldDelivery.getId();
        String newId = newDelivery.getId();
        
        return oldId != null && oldId.equals(newId);
    }
    
    /**
     * Called to check whether two items have the same data.
     * This method is only called if areItemsTheSame() returns true.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Delivery oldDelivery = oldDeliveries.get(oldItemPosition);
        Delivery newDelivery = newDeliveries.get(newItemPosition);
        
        if (oldDelivery == null || newDelivery == null) {
            return false;
        }
        
        // Compare basic properties
        if (!Objects.equals(oldDelivery.getOrderId(), newDelivery.getOrderId())) {
            return false;
        }
        
        if (!Objects.equals(oldDelivery.getNotes(), newDelivery.getNotes())) {
            return false;
        }
        
        // Compare address
        if (oldDelivery.getAddress() != null && newDelivery.getAddress() != null) {
            if (!Objects.equals(oldDelivery.getAddress().getFullAddress(), 
                    newDelivery.getAddress().getFullAddress())) {
                return false;
            }
        } else if (oldDelivery.getAddress() != null || newDelivery.getAddress() != null) {
            return false;
        }
        
        // Compare amounts (particularly tip amount)
        if (oldDelivery.getAmounts() != null && newDelivery.getAmounts() != null) {
            if (oldDelivery.getAmounts().getTipAmount() != newDelivery.getAmounts().getTipAmount()) {
                return false;
            }
        } else if (oldDelivery.getAmounts() != null || newDelivery.getAmounts() != null) {
            return false;
        }
        
        // Compare status
        if (oldDelivery.getStatus() != null && newDelivery.getStatus() != null) {
            if (!Objects.equals(oldDelivery.getStatus().getState(), newDelivery.getStatus().getState()) ||
                    oldDelivery.getStatus().isCompleted() != newDelivery.getStatus().isCompleted() ||
                    oldDelivery.getStatus().isTipped() != newDelivery.getStatus().isTipped()) {
                return false;
            }
        } else if (oldDelivery.getStatus() != null || newDelivery.getStatus() != null) {
            return false;
        }
        
        // Compare dispute info
        if (oldDelivery.getDisputeInfo() != null && newDelivery.getDisputeInfo() != null) {
            if (oldDelivery.getDisputeInfo().isHasDispute() != newDelivery.getDisputeInfo().isHasDispute()) {
                return false;
            }
        } else if (oldDelivery.getDisputeInfo() != null || newDelivery.getDisputeInfo() != null) {
            return false;
        }
        
        // Compare times (we'll just check if completedAt is the same, as that's most relevant for UI)
        if (oldDelivery.getTimes() != null && newDelivery.getTimes() != null) {
            if (!Objects.equals(
                    oldDelivery.getTimes().getCompletedAt(), 
                    newDelivery.getTimes().getCompletedAt())) {
                return false;
            }
        } else if (oldDelivery.getTimes() != null || newDelivery.getTimes() != null) {
            return false;
        }
        
        // If we've passed all checks, the contents are the same
        return true;
    }
    
    /**
     * Called when an item changes but we want to animate specific changes.
     * This is optional but improves the animation precision.
     */
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // We're not implementing fine-grained change tracking for now
        // A potential enhancement would be to return specific fields that changed
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}