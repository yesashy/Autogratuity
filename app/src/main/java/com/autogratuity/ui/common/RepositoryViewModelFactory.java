package com.autogratuity.ui.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.data.repository.sync.SyncRepository;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Factory for creating ViewModels that require repository dependencies.
 * This ensures proper dependency injection for ViewModels.
 */
public class RepositoryViewModelFactory implements ViewModelProvider.Factory {
    
    private final ConfigRepository configRepository;
    private final PreferenceRepository preferenceRepository;
    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AddressRepository addressRepository;
    private final SyncRepository syncRepository;
    
    /**
     * Constructor that accepts all repositories
     * 
     * @param configRepository Config repository
     * @param preferenceRepository Preference repository
     * @param deliveryRepository Delivery repository
     * @param subscriptionRepository Subscription repository
     * @param addressRepository Address repository
     * @param syncRepository Sync repository
     */
    public RepositoryViewModelFactory(
            ConfigRepository configRepository,
            PreferenceRepository preferenceRepository,
            DeliveryRepository deliveryRepository,
            SubscriptionRepository subscriptionRepository,
            AddressRepository addressRepository,
            SyncRepository syncRepository) {
        this.configRepository = configRepository;
        this.preferenceRepository = preferenceRepository;
        this.deliveryRepository = deliveryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.addressRepository = addressRepository;
        this.syncRepository = syncRepository;
    }
    
    /**
     * Create a new instance of the given ViewModel class
     * 
     * @param modelClass Class to instantiate
     * @param <T> Type of ViewModel
     * @return New instance of the ViewModel
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            // Find the constructor that matches our repositories
            for (Constructor<?> constructor : modelClass.getConstructors()) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                
                // Try to match constructor with available repositories
                if (paramTypes.length > 0) {
                    Object[] params = new Object[paramTypes.length];
                    boolean canProvide = true;
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramType = paramTypes[i];
                        
                        if (ConfigRepository.class.isAssignableFrom(paramType)) {
                            params[i] = configRepository;
                        } else if (PreferenceRepository.class.isAssignableFrom(paramType)) {
                            params[i] = preferenceRepository;
                        } else if (DeliveryRepository.class.isAssignableFrom(paramType)) {
                            params[i] = deliveryRepository;
                        } else if (SubscriptionRepository.class.isAssignableFrom(paramType)) {
                            params[i] = subscriptionRepository;
                        } else if (AddressRepository.class.isAssignableFrom(paramType)) {
                            params[i] = addressRepository;
                        } else if (SyncRepository.class.isAssignableFrom(paramType)) {
                            params[i] = syncRepository;
                        } else {
                            canProvide = false;
                            break;
                        }
                    }
                    
                    if (canProvide) {
                        return (T) constructor.newInstance(params);
                    }
                }
            }
            
            // If no matching constructor found, try no-args constructor
            return modelClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create an instance of " + modelClass.getName()
                    + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Factory method to create a RepositoryViewModelFactory with all repositories from provider
     * 
     * @return New factory instance
     */
    public static RepositoryViewModelFactory fromRepositoryProvider() {
        // Get all repositories from RepositoryProvider
        return new RepositoryViewModelFactory(
                com.autogratuity.data.repository.core.RepositoryProvider.getConfigRepository(),
                com.autogratuity.data.repository.core.RepositoryProvider.getPreferenceRepository(),
                com.autogratuity.data.repository.core.RepositoryProvider.getDeliveryRepository(),
                com.autogratuity.data.repository.core.RepositoryProvider.getSubscriptionRepository(),
                com.autogratuity.data.repository.core.RepositoryProvider.getAddressRepository(),
                com.autogratuity.data.repository.core.RepositoryProvider.getSyncRepository()
        );
    }
}
