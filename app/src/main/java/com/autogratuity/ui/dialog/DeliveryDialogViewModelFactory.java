package com.autogratuity.ui.dialog;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.ui.common.RepositoryViewModelFactory;

/**
 * Factory for creating DeliveryDialogViewModel instances.
 * This factory uses the standardized RepositoryViewModelFactory pattern.
 */
public class DeliveryDialogViewModelFactory implements ViewModelProvider.Factory {

    private final RepositoryViewModelFactory delegateFactory;

    /**
     * Constructor that creates a specialized factory for DeliveryDialogViewModel
     */
    public DeliveryDialogViewModelFactory() {
        // Use the builder pattern from RepositoryViewModelFactory
        this.delegateFactory = RepositoryViewModelFactory.builder()
                .withDeliveryRepository(RepositoryProvider.getDeliveryRepository())
                .withAddressRepository(RepositoryProvider.getAddressRepository())
                .build();
    }

    /**
     * Delegate creation to the standard RepositoryViewModelFactory
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return delegateFactory.create(modelClass);
    }

    /**
     * Static factory method to create a new instance
     * 
     * @return New factory instance
     */
    public static DeliveryDialogViewModelFactory create() {
        return new DeliveryDialogViewModelFactory();
    }
}