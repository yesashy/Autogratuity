package com.autogratuity.data.repository;

import com.autogratuity.data.repository.address.AddressRepositoryTest;
import com.autogratuity.data.repository.config.ConfigRepositoryTest;
import com.autogratuity.data.repository.core.RepositoryProviderTest;
import com.autogratuity.data.repository.delivery.DeliveryRepositoryTest;
import com.autogratuity.data.repository.preference.PreferenceRepositoryTest;
import com.autogratuity.data.repository.subscription.SubscriptionRepositoryTest;
import com.autogratuity.data.repository.sync.SyncRepositoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all repository tests.
 * This ensures all repository tests run together and can be easily executed.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RepositoryProviderTest.class,
        ConfigRepositoryTest.class,
        PreferenceRepositoryTest.class,
        DeliveryRepositoryTest.class,
        SubscriptionRepositoryTest.class,
        AddressRepositoryTest.class,
        SyncRepositoryTest.class
})
public class RepositoryTestSuite {
    // Test suite definition
}
