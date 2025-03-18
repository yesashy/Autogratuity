# AddressViewModel Implementation Guide

This document details the implementation of the `AddressViewModel` as part of the architectural overhaul to migrate the Autogratuity application to a domain-based repository pattern with proper MVVM architecture.

## Overview of Changes

The implementation includes:

1. Creation of a new `AddressViewModel` class that properly extends `BaseViewModel`
2. Refactoring of `AddressesFragment` to use the ViewModel instead of direct repository access
3. Implementation of LiveData pattern for reactive UI updates

## Implementation Details

### 1. AddressViewModel

Location: `com.autogratuity.ui.address.AddressViewModel`

Key features:
- Extends `BaseViewModel` to leverage common functionality
- Properly injects the `AddressRepository` using constructor dependency injection
- Exposes addresses as LiveData for reactive UI updates
- Manages RxJava subscriptions through the CompositeDisposable
- Implements comprehensive error handling
- Includes methods for all address-related operations:
  - Loading addresses
  - Real-time observation of address changes
  - Address selection
  - CRUD operations (Create, Read, Update, Delete)
  - Filtering and search operations

### 2. Refactored AddressesFragment

Location: `com.autogratuity.ui.address.AddressesFragment`

Key changes:
- Removes direct repository access
- Uses ViewModelProvider with RepositoryViewModelFactory to obtain the ViewModel
- Observes LiveData from the ViewModel for reactive UI updates
- Delegates all data operations to the ViewModel
- Properly handles loading states, errors, and empty states

## Integration Steps

To integrate these changes:

1. **Review the new AddressViewModel:**
   - Ensure it meets all business requirements
   - Verify it handles all use cases covered by the original fragment

2. **Replace the existing AddressesFragment:**
   ```bash
   # Make a backup of the existing file
   cp app/src/main/java/com/autogratuity/ui/address/AddressesFragment.java app/src/main/java/com/autogratuity/ui/address/AddressesFragment.java.bak
   
   # Replace with the new implementation
   mv app/src/main/java/com/autogratuity/ui/address/AddressesFragment.java.new app/src/main/java/com/autogratuity/ui/address/AddressesFragment.java
   ```

3. **Update any related navigation or dependency code:**
   - Check for any imports referencing the old implementation
   - Update fragment instantiation if needed

4. **Test thoroughly:**
   - Verify all functionality works as expected
   - Test loading states, error handling, and empty states
   - Confirm that real-time updates are working

## Benefits of This Implementation

1. **Separation of Concerns:**
   - UI logic is isolated in the Fragment
   - Business logic is contained in the ViewModel
   - Data access is handled by the Repository

2. **Improved Testability:**
   - ViewModel can be tested independently of Android framework
   - LiveData automatically handles lifecycle concerns

3. **Enhanced Maintainability:**
   - Clear, single-responsibility components
   - Consistent pattern across the application
   - Easier to extend and modify

4. **Memory Management:**
   - Proper handling of RxJava disposables
   - Lifecycle-aware components to prevent leaks

## Next Steps

1. **Create unit tests** for the AddressViewModel
2. **Apply similar patterns** to other ViewModels in the application
3. **Update UI components** that interact with addresses to use the ViewModel
4. **Consider implementing Dagger** for more robust dependency injection

## Related Documentation

- [MVVM Architecture Pattern](https://developer.android.com/jetpack/guide#recommended-app-arch)
- [LiveData Overview](https://developer.android.com/topic/libraries/architecture/livedata)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
