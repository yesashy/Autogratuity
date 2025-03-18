package com.autogratuity.ui.dialog;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;

/**
 * Factory for creating DeliveryDialogViewModel instances.
 * This factory ensures that the ViewModel is created with the correct repositories.
 */
public class DeliveryDialogViewModelFactory implements ViewModelProvider.Factory {

    /**
     * Create a new instance of DeliveryDialogViewModel with the required repositories
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DeliveryDialogViewModel.class)) {
            DeliveryRepository deliveryRepository = RepositoryProvider.getDeliveryRepository();
            AddressRepository addressRepository = RepositoryProvider.getAddressRepository();
            return (T) new DeliveryDialogViewModel(deliveryRepository, addressRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
