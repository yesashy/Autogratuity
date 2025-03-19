package com.autogratuity.ui.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.autogratuity.data.repository.address.AddressRepository;
import com.autogratuity.data.repository.config.ConfigRepository;
import com.autogratuity.data.repository.core.RepositoryProvider;
import com.autogratuity.data.repository.delivery.DeliveryRepository;
import com.autogratuity.data.repository.preference.PreferenceRepository;
import com.autogratuity.data.repository.subscription.SubscriptionRepository;
import com.autogratuity.data.repository.sync.SyncRepository;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized factory for creating ViewModels that require repository dependencies.
 * This factory ensures proper dependency injection for ViewModels with a builder pattern
 * for flexible configuration and better error handling.
 */
public class RepositoryViewModelFactory implements ViewModelProvider.Factory {
    
    private final ConfigRepository configRepository;
    private final PreferenceRepository preferenceRepository;
    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AddressRepository addressRepository;
    private final SyncRepository syncRepository;
    
    private final Map<Class<?>, ViewModel> cache = new HashMap<>();
    
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
    protected RepositoryViewModelFactory(
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
     * Create a new instance of the given ViewModel class or retrieve from cache if available.
     * This method handles dependency injection for repositories based on constructor parameters.
     * 
     * @param modelClass Class to instantiate
     * @param <T> Type of ViewModel
     * @return New instance of the ViewModel
     * @throws RuntimeException if the ViewModel cannot be created
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Check cache first to avoid recreating the same ViewModel
        if (cache.containsKey(modelClass)) {
            return (T) cache.get(modelClass);
        }
        
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
                        // Create the ViewModel instance
                        T viewModel = (T) constructor.newInstance(params);
                        
                        // Cache the instance
                        cache.put(modelClass, viewModel);
                        
                        return viewModel;
                    }
                }
            }
            
            // If no matching constructor found, try no-args constructor
            T viewModel = modelClass.newInstance();
            cache.put(modelClass, viewModel);
            return viewModel;
            
        } catch (Exception e) {
            String errorMessage = "Cannot create an instance of " + modelClass.getName() + ": " + e.getMessage();
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    /**
     * Builder class for creating RepositoryViewModelFactory instances.
     * This allows flexible configuration of which repositories to include.
     */
    public static class Builder {
        private ConfigRepository configRepository;
        private PreferenceRepository preferenceRepository;
        private DeliveryRepository deliveryRepository;
        private SubscriptionRepository subscriptionRepository;
        private AddressRepository addressRepository;
        private SyncRepository syncRepository;
        
        /**
         * Default constructor initializes with null repositories
         */
        public Builder() {
            // All repositories start as null
        }
        
        /**
         * Set the config repository
         * 
         * @param configRepository Config repository
         * @return Builder instance for chaining
         */
        public Builder withConfigRepository(ConfigRepository configRepository) {
            this.configRepository = configRepository;
            return this;
        }
        
        /**
         * Set the preference repository
         * 
         * @param preferenceRepository Preference repository
         * @return Builder instance for chaining
         */
        public Builder withPreferenceRepository(PreferenceRepository preferenceRepository) {
            this.preferenceRepository = preferenceRepository;
            return this;
        }
        
        /**
         * Set the delivery repository
         * 
         * @param deliveryRepository Delivery repository
         * @return Builder instance for chaining
         */
        public Builder withDeliveryRepository(DeliveryRepository deliveryRepository) {
            this.deliveryRepository = deliveryRepository;
            return this;
        }
        
        /**
         * Set the subscription repository
         * 
         * @param subscriptionRepository Subscription repository
         * @return Builder instance for chaining
         */
        public Builder withSubscriptionRepository(SubscriptionRepository subscriptionRepository) {
            this.subscriptionRepository = subscriptionRepository;
            return this;
        }
        
        /**
         * Set the address repository
         * 
         * @param addressRepository Address repository
         * @return Builder instance for chaining
         */
        public Builder withAddressRepository(AddressRepository addressRepository) {
            this.addressRepository = addressRepository;
            return this;
        }
        
        /**
         * Set the sync repository
         * 
         * @param syncRepository Sync repository
         * @return Builder instance for chaining
         */
        public Builder withSyncRepository(SyncRepository syncRepository) {
            this.syncRepository = syncRepository;
            return this;
        }
        
        /**
         * Build a new RepositoryViewModelFactory with the configured repositories
         * 
         * @return New RepositoryViewModelFactory instance
         */
        public RepositoryViewModelFactory build() {
            return new RepositoryViewModelFactory(
                    configRepository,
                    preferenceRepository,
                    deliveryRepository,
                    subscriptionRepository,
                    addressRepository,
                    syncRepository
            );
        }
        
        /**
         * Build a factory with all repositories from the Repository Provider
         * 
         * @return New RepositoryViewModelFactory instance with all repositories
         */
        public RepositoryViewModelFactory buildWithAllRepositories() {
            return new RepositoryViewModelFactory(
                    RepositoryProvider.getConfigRepository(),
                    RepositoryProvider.getPreferenceRepository(),
                    RepositoryProvider.getDeliveryRepository(),
                    RepositoryProvider.getSubscriptionRepository(),
                    RepositoryProvider.getAddressRepository(),
                    RepositoryProvider.getSyncRepository()
            );
        }
    }
    
    /**
     * Factory method to create a RepositoryViewModelFactory with all repositories from provider.
     * This is a shorthand for creating a Builder and calling buildWithAllRepositories.
     * 
     * @return New factory instance with all repositories
     */
    public static RepositoryViewModelFactory fromRepositoryProvider() {
        return new Builder().buildWithAllRepositories();
    }
    
    /**
     * Factory method to create a new Builder
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}