Executing tasks: [:app:assembleDebug] in project C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity

Configuration on demand is an incubating feature.
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:dataBindingMergeDependencyArtifactsDebug UP-TO-DATE
> Task :app:generateDebugResValues UP-TO-DATE
> Task :app:generateDebugResources UP-TO-DATE
> Task :app:processDebugGoogleServices UP-TO-DATE
> Task :app:mergeDebugResources UP-TO-DATE
> Task :app:packageDebugResources UP-TO-DATE
> Task :app:parseDebugLocalResources UP-TO-DATE
> Task :app:dataBindingGenBaseClassesDebug UP-TO-DATE
> Task :app:generateDebugBuildConfig UP-TO-DATE
> Task :app:javaPreCompileDebug UP-TO-DATE
> Task :app:checkDebugAarMetadata UP-TO-DATE
> Task :app:mapDebugSourceSetPaths UP-TO-DATE
> Task :app:createDebugCompatibleScreenManifests UP-TO-DATE
> Task :app:extractDeepLinksDebug UP-TO-DATE
> Task :app:processDebugMainManifest UP-TO-DATE
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE

> Task :app:compileDebugJavaWithJavac
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:314: error: local variables referenced from a lambda expression must be final or effectively final
            final Map<String, Object> finalDeviceInfo = new HashMap<>(deviceInfo);
                                                                      ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:292: error: incompatible types: AggregateSource cannot be converted to Source
                    .get(com.google.firebase.firestore.AggregateSource.SERVER)
                                                                      ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:159: error: cannot find symbol
                .subscribeOn(Schedulers.io())
                ^
  symbol:   method subscribeOn(Scheduler)
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:164: error: cannot find symbol
                        preferenceRepository.setLongPreference("user_registered_at", System.currentTimeMillis())
                                            ^
  symbol:   method setLongPreference(String,long)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:169: error: cannot find symbol
                        preferenceRepository.setStringPreference("user_email", user.getEmail())
                                            ^
  symbol:   method setStringPreference(String,String)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:120: error: cannot find symbol
                syncIcon.setImageResource(R.drawable.ic_sync);
                                                    ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:168: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_sync);
                                                ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:174: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_sync);
                                                ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:176: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_error);
                                                ^
  symbol:   variable ic_error
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:190: error: cannot find symbol
                fragment = new DashboardFragment();
                               ^
  symbol:   class DashboardFragment
  location: class MainActivity
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:199: error: cannot find symbol
                fragment = new DashboardFragment();
                               ^
  symbol:   class DashboardFragment
  location: class MainActivity
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:70: error: method getInstance in class SubscriptionManager cannot be applied to given types;
        subscriptionManager = SubscriptionManager.getInstance(this);
                                                 ^
  required: Context,SubscriptionRepository,PreferenceRepository
  found:    ProSubscribeActivity
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:71: error: cannot find symbol
        subscriptionManager.setListener(this);
                           ^
  symbol:   method setListener(ProSubscribeActivity)
  location: variable subscriptionManager of type SubscriptionManager
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\subscription\SubscriptionViewModel.java:109: error: cannot find symbol
            subscriptionRepository.isTrialAvailable()
                                  ^
  symbol:   method isTrialAvailable()
  location: variable subscriptionRepository of type SubscriptionRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:287: error: cannot find symbol
            status.setPurchaseToken(purchase.getPurchaseToken());
                  ^
  symbol:   method setPurchaseToken(String)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:288: error: cannot find symbol
            status.setLastUpdated(new Date());
                  ^
  symbol:   method setLastUpdated(Date)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:384: error: cannot find symbol
            status.setLastUpdated(new Date());
                  ^
  symbol:   method setLastUpdated(Date)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\BootCompletedReceiver.java:39: error: cannot find symbol
                    subscriptionRepository.getCurrentSubscriptionStatus()
                                          ^
  symbol:   method getCurrentSubscriptionStatus()
  location: variable subscriptionRepository of type SubscriptionRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\CaptureProcessReceiver.java:37: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
        ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context);
                                          ^
  required: Context,DeliveryRepository,AddressRepository
  found:    Context
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:47: error: method getInstance in class AuthenticationManager cannot be applied to given types;
            authManager = AuthenticationManager.getInstance();
                                               ^
  required: Context
  found:    no arguments
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:148: error: cannot find symbol
        delivery.getStatus().setDoNotDeliver(true);
                            ^
  symbol:   method setDoNotDeliver(boolean)
  location: class Status
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\ShiptCaptureBackgroundService.java:147: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
            ShiptCaptureProcessor processor = new ShiptCaptureProcessor(this);
                                              ^
  required: Context,DeliveryRepository,AddressRepository
  found:    ShiptCaptureBackgroundService
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ShiptNotificationListenerService.java:406: error: cannot find symbol
                        orderId.equals(delivery.getReference().getOrderId())) {
                                                              ^
  symbol:   method getOrderId()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:102: error: cannot find symbol
        if (address.getMetadata() != null && address.getMetadata().getCustomData() != null) {
                                                                  ^
  symbol:   method getCustomData()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:103: error: cannot find symbol
            Object dnpObj = address.getMetadata().getCustomData().get("doNotDeliver");
                                                 ^
  symbol:   method getCustomData()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:178: error: cannot find symbol
            favoriteToggle = itemView.findViewById(R.id.button_favorite);
                                                       ^
  symbol:   variable button_favorite
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:179: error: cannot find symbol
            favoriteIndicator = itemView.findViewById(R.id.favorite_indicator);
                                                          ^
  symbol:   variable favorite_indicator
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:143: error: cannot find symbol
        View tryNewUiCard = view.findViewById(R.id.try_new_ui_card);
                                                  ^
  symbol:   variable try_new_ui_card
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:273: error: cannot find symbol
                    R.layout.item_recent_activity, recentActivityContainer, false);
                            ^
  symbol:   variable item_recent_activity
  location: class layout
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:277: error: cannot find symbol
            TextView amountText = activityItem.findViewById(R.id.amount_text);
                                                                ^
  symbol:   variable amount_text
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:282: error: cannot find symbol
            } else if (delivery.getReference() != null && delivery.getReference().getAddressText() != null) {
                                                                                 ^
  symbol:   method getAddressText()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:283: error: cannot find symbol
                addressText.setText(delivery.getReference().getAddressText());
                                                           ^
  symbol:   method getAddressText()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:330: error: cannot find symbol
            TextView statsText = areaItem.findViewById(R.id.stats_text);
                                                           ^
  symbol:   variable stats_text
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardViewModel.java:145: error: cannot find symbol
            addressRepository.getBestTippingAddresses(5)
                             ^
  symbol:   method getBestTippingAddresses(int)
  location: variable addressRepository of type AddressRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:125: error: non-static variable dateFormat cannot be referenced from a static context
                dateText.setText(dateFormat.format(deliveryDate));
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:127: error: non-static variable dateFormat cannot be referenced from a static context
                dateText.setText(dateFormat.format(delivery.getMetadata().getCreatedAt()));
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:443: error: cannot find symbol
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                               ^
  symbol:   method getId()
  location: class Delivery
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:513: error: cannot find symbol
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                               ^
  symbol:   method getId()
  location: class Delivery
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\import\BulkUploadViewModel.java:63: error: incompatible types: Object cannot be converted to MapFragment
        importManager = new ImportManager(context, addressRepository, deliveryRepository, syncRepository, mapFragment);
                                                                                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:245: error: cannot find symbol
                    if (delivery.getAddress().getLocation() == null ||
                                             ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:246: error: cannot find symbol
                            (delivery.getAddress().getLocation().getLatitude() == 0 &&
                                                  ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:247: error: cannot find symbol
                             delivery.getAddress().getLocation().getLongitude() == 0)) {
                                                  ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:250: error: incompatible types: SimpleAddress cannot be converted to Address
                            Address geocodedAddress = addressRepository.geocodeAddress(delivery.getAddress())
                                                                                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:252: error: incompatible types: Address cannot be converted to SimpleAddress
                            delivery.setAddress(geocodedAddress);
                                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:294: error: incompatible types: SimpleAddress cannot be converted to Address
                            addressRepository.addAddress(delivery.getAddress())
                                                                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:298: error: incompatible types: Address cannot be converted to SimpleAddress
                            delivery.setAddress(existingAddress);
                                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:135: error: cannot find symbol
                                        if (profile != null && profile.getUsageStats() != null) {
                                                                      ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:136: error: cannot find symbol
                                            mappingCount = profile.getUsageStats().getMappingCount();
                                                                  ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                            UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                            UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                                                               ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:170: error: incompatible types: Throwable cannot be converted to Exception
                                                                callback.onError(error);
                                                                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:180: error: incompatible types: Throwable cannot be converted to Exception
                                            callback.onError(error);
                                                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:189: error: incompatible types: Throwable cannot be converted to Exception
                            callback.onError(error);
                                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                       ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:222: error: cannot find symbol
                                usageStats = new UserProfile.UsageStats();
                                                            ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                       ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:267: error: cannot find symbol
                                usageStats = new UserProfile.UsageStats();
                                                            ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:96: error: cannot find symbol
                .subscribeOn(Schedulers.io())
                ^
  symbol:   method subscribeOn(Scheduler)
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:157: error: cannot find symbol
            preferenceRepository.setStringPreference(key, value)
                                ^
  symbol:   method setStringPreference(String,String)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\DataValidationSystem.java:54: error: cannot find symbol
                    delivery.getAddress().getLocation() == null || 
                                         ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:128: error: cannot find symbol
            String orderId = delivery.getMetadata() != null ? delivery.getMetadata().getOrderId() : "";
                                                                                    ^
  symbol:   method getOrderId()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:158: error: cannot find symbol
                    delivery.getAddress().getFlags() != null && 
                                         ^
  symbol:   method getFlags()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:159: error: cannot find symbol
                    delivery.getAddress().getFlags().isDoNotDeliver();
                                         ^
  symbol:   method getFlags()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:216: error: incompatible types: Coordinates cannot be converted to Location
            address.setLocation(coordinates);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:234: error: cannot find symbol
            address.setSearchTerms(searchTerms);
                   ^
  symbol:   method setSearchTerms(List<String>)
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:264: error: cannot find symbol
            reference.setAddressText(address.getFullAddress());
                     ^
  symbol:   method setAddressText(String)
  location: variable reference of type Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:265: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
            delivery.setReference(reference);
                                  ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:266: error: incompatible types: Address cannot be converted to SimpleAddress
            delivery.setAddress(address);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:271: error: cannot find symbol
            amounts.setDeliveryAmount(0.0); // Unknown from GeoJSON
                   ^
  symbol:   method setDeliveryAmount(double)
  location: variable amounts of type Amounts
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:272: error: cannot find symbol
            amounts.setOrderAmount(0.0); // Unknown from GeoJSON
                   ^
  symbol:   method setOrderAmount(double)
  location: variable amounts of type Amounts
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:273: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
            delivery.setAmounts(amounts);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:279: error: cannot find symbol
            metadata.setImportSource("GeoJSON");
                    ^
  symbol:   method setImportSource(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:281: error: cannot find symbol
                metadata.setOrderId(orderId);
                        ^
  symbol:   method setOrderId(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:283: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
            delivery.setMetadata(metadata);
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:287: error: cannot find symbol
            times.setCreatedAt(new Date());
                 ^
  symbol:   method setCreatedAt(Date)
  location: variable times of type Times
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:288: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
            delivery.setTimes(times);
                              ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:294: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
            delivery.setStatus(status);
                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:348: error: cannot find symbol
            if (delivery.getAddress().getLocation() == null || 
                                     ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:349: error: cannot find symbol
                    (delivery.getAddress().getLocation().getLatitude() == 0 && 
                                          ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:350: error: cannot find symbol
                     delivery.getAddress().getLocation().getLongitude() == 0)) {
                                          ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:416: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                    Reference reference = delivery.getReference();
                                                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:418: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                    delivery.setReference(reference);
                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:422: error: incompatible types: SimpleAddress cannot be converted to Address
                        addressRepository.updateAddress(delivery.getAddress())
                                                                           ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:427: error: incompatible types: Address cannot be converted to SimpleAddress
                        delivery.setAddress(existingAddress);
                                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:431: error: incompatible types: SimpleAddress cannot be converted to Address
                    DocumentReference addressRef = addressRepository.addAddress(delivery.getAddress())
                                                                                                   ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:437: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                    Reference reference = delivery.getReference();
                                                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:439: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                    delivery.setReference(reference);
                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:274: error: incompatible types: Coordinates cannot be converted to Location
            address.setLocation(location);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:281: error: incompatible types: Address cannot be converted to SimpleAddress
        delivery.setAddress(address);
                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:285: error: cannot find symbol
        reference.setAddressText(addressText);
                 ^
  symbol:   method setAddressText(String)
  location: variable reference of type Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:286: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
        delivery.setReference(reference);
                              ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:290: error: cannot find symbol
        metadata.setOrderId(orderId);
                ^
  symbol:   method setOrderId(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:293: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
        delivery.setMetadata(metadata);
                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:297: error: cannot find symbol
        times.setCreatedAt(new Date());
             ^
  symbol:   method setCreatedAt(Date)
  location: variable times of type Times
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:299: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
        delivery.setTimes(times);
                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:305: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
        delivery.setStatus(status);
                           ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:311: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
            delivery.setAmounts(amounts);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ShiptCaptureProcessor.java:65: error: cannot find symbol
                            (delivery.getStatus() == null || !delivery.getStatus().isProcessed())) {
                                                                                  ^
  symbol:   method isProcessed()
  location: class Status
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
100 errors

> Task :app:compileDebugJavaWithJavac FAILED
> Task :app:mergeDebugShaders UP-TO-DATE
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets UP-TO-DATE
> Task :app:compressDebugAssets UP-TO-DATE
> Task :app:desugarDebugFileDependencies UP-TO-DATE
> Task :app:checkDebugDuplicateClasses UP-TO-DATE
> Task :app:mergeExtDexDebug UP-TO-DATE
> Task :app:mergeLibDexDebug UP-TO-DATE
> Task :app:mergeDebugJniLibFolders UP-TO-DATE
> Task :app:mergeDebugNativeLibs NO-SOURCE
> Task :app:stripDebugDebugSymbols NO-SOURCE
> Task :app:validateSigningDebug UP-TO-DATE
> Task :app:writeDebugAppMetadata UP-TO-DATE
> Task :app:writeDebugSigningConfigVersions UP-TO-DATE
[Incubating] Problems report is available at: file:///C:/Users/ReifiedAsh/AndroidStudioProjects/Autogratuity/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugJavaWithJavac'.
> Compilation failed; see the compiler output below.
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:292: error: incompatible types: AggregateSource cannot be converted to Source
                      .get(com.google.firebase.firestore.AggregateSource.SERVER)
                                                                        ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\import\BulkUploadViewModel.java:63: error: incompatible types: Object cannot be converted to MapFragment
          importManager = new ImportManager(context, addressRepository, deliveryRepository, syncRepository, mapFragment);
                                                                                                            ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:250: error: incompatible types: SimpleAddress cannot be converted to Address
                              Address geocodedAddress = addressRepository.geocodeAddress(delivery.getAddress())
                                                                                                            ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:252: error: incompatible types: Address cannot be converted to SimpleAddress
                              delivery.setAddress(geocodedAddress);
                                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:294: error: incompatible types: SimpleAddress cannot be converted to Address
                              addressRepository.addAddress(delivery.getAddress())
                                                                              ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:298: error: incompatible types: Address cannot be converted to SimpleAddress
                              delivery.setAddress(existingAddress);
                                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:170: error: incompatible types: Throwable cannot be converted to Exception
                                                                  callback.onError(error);
                                                                                   ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:180: error: incompatible types: Throwable cannot be converted to Exception
                                              callback.onError(error);
                                                               ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:189: error: incompatible types: Throwable cannot be converted to Exception
                              callback.onError(error);
                                               ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:216: error: incompatible types: Coordinates cannot be converted to Location
              address.setLocation(coordinates);
                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:265: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
              delivery.setReference(reference);
                                    ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:266: error: incompatible types: Address cannot be converted to SimpleAddress
              delivery.setAddress(address);
                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:273: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
              delivery.setAmounts(amounts);
                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:283: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
              delivery.setMetadata(metadata);
                                   ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:288: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
              delivery.setTimes(times);
                                ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:294: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
              delivery.setStatus(status);
                                 ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:416: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                      Reference reference = delivery.getReference();
                                                                 ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:418: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                      delivery.setReference(reference);
                                            ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:422: error: incompatible types: SimpleAddress cannot be converted to Address
                          addressRepository.updateAddress(delivery.getAddress())
                                                                             ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:427: error: incompatible types: Address cannot be converted to SimpleAddress
                          delivery.setAddress(existingAddress);
                                              ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:431: error: incompatible types: SimpleAddress cannot be converted to Address
                      DocumentReference addressRef = addressRepository.addAddress(delivery.getAddress())
                                                                                                     ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:437: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                      Reference reference = delivery.getReference();
                                                                 ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:439: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                      delivery.setReference(reference);
                                            ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:274: error: incompatible types: Coordinates cannot be converted to Location
              address.setLocation(location);
                                  ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:281: error: incompatible types: Address cannot be converted to SimpleAddress
          delivery.setAddress(address);
                              ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:286: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
          delivery.setReference(reference);
                                ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:293: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
          delivery.setMetadata(metadata);
                               ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:299: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
          delivery.setTimes(times);
                            ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:305: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
          delivery.setStatus(status);
                             ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:311: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
              delivery.setAmounts(amounts);
                                  ^
  Note: Some input files use unchecked or unsafe operations.
  Note: Recompile with -Xlint:unchecked for details.
  Note: Recompile with -Xlint:deprecation for details.
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:314: error: local variables referenced from a lambda expression must be final or effectively final
              final Map<String, Object> finalDeviceInfo = new HashMap<>(deviceInfo);
                                                                        ^
  Note: Some input files use or override a deprecated API.
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:125: error: non-static variable dateFormat cannot be referenced from a static context
                  dateText.setText(dateFormat.format(deliveryDate));
                                   ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:127: error: non-static variable dateFormat cannot be referenced from a static context
                  dateText.setText(dateFormat.format(delivery.getMetadata().getCreatedAt()));
                                   ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:70: error: method getInstance in class SubscriptionManager cannot be applied to given types;
          subscriptionManager = SubscriptionManager.getInstance(this);
                                                   ^
    required: Context,SubscriptionRepository,PreferenceRepository
    found:    ProSubscribeActivity
    reason: actual and formal argument lists differ in length
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\CaptureProcessReceiver.java:37: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
          ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context);
                                            ^
    required: Context,DeliveryRepository,AddressRepository
    found:    Context
    reason: actual and formal argument lists differ in length
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:47: error: method getInstance in class AuthenticationManager cannot be applied to given types;
              authManager = AuthenticationManager.getInstance();
                                                 ^
    required: Context
    found:    no arguments
    reason: actual and formal argument lists differ in length
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\ShiptCaptureBackgroundService.java:147: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
              ShiptCaptureProcessor processor = new ShiptCaptureProcessor(this);
                                                ^
    required: Context,DeliveryRepository,AddressRepository
    found:    ShiptCaptureBackgroundService
    reason: actual and formal argument lists differ in length
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:159: error: cannot find symbol
                  .subscribeOn(Schedulers.io())
                  ^
    symbol:   method subscribeOn(Scheduler)
    location: class String
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:164: error: cannot find symbol
                          preferenceRepository.setLongPreference("user_registered_at", System.currentTimeMillis())
                                              ^
    symbol:   method setLongPreference(String,long)
    location: variable preferenceRepository of type PreferenceRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:169: error: cannot find symbol
                          preferenceRepository.setStringPreference("user_email", user.getEmail())
                                              ^
    symbol:   method setStringPreference(String,String)
    location: variable preferenceRepository of type PreferenceRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:71: error: cannot find symbol
          subscriptionManager.setListener(this);
                             ^
    symbol:   method setListener(ProSubscribeActivity)
    location: variable subscriptionManager of type SubscriptionManager
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\subscription\SubscriptionViewModel.java:109: error: cannot find symbol
              subscriptionRepository.isTrialAvailable()
                                    ^
    symbol:   method isTrialAvailable()
    location: variable subscriptionRepository of type SubscriptionRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:287: error: cannot find symbol
              status.setPurchaseToken(purchase.getPurchaseToken());
                    ^
    symbol:   method setPurchaseToken(String)
    location: variable status of type SubscriptionStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:288: error: cannot find symbol
              status.setLastUpdated(new Date());
                    ^
    symbol:   method setLastUpdated(Date)
    location: variable status of type SubscriptionStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:384: error: cannot find symbol
              status.setLastUpdated(new Date());
                    ^
    symbol:   method setLastUpdated(Date)
    location: variable status of type SubscriptionStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\BootCompletedReceiver.java:39: error: cannot find symbol
                      subscriptionRepository.getCurrentSubscriptionStatus()
                                            ^
    symbol:   method getCurrentSubscriptionStatus()
    location: variable subscriptionRepository of type SubscriptionRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:148: error: cannot find symbol
          delivery.getStatus().setDoNotDeliver(true);
                              ^
    symbol:   method setDoNotDeliver(boolean)
    location: class Status
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ShiptNotificationListenerService.java:406: error: cannot find symbol
                          orderId.equals(delivery.getReference().getOrderId())) {
                                                                ^
    symbol:   method getOrderId()
    location: class Reference
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:102: error: cannot find symbol
          if (address.getMetadata() != null && address.getMetadata().getCustomData() != null) {
                                                                    ^
    symbol:   method getCustomData()
    location: class Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:103: error: cannot find symbol
              Object dnpObj = address.getMetadata().getCustomData().get("doNotDeliver");
                                                   ^
    symbol:   method getCustomData()
    location: class Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:282: error: cannot find symbol
              } else if (delivery.getReference() != null && delivery.getReference().getAddressText() != null) {
                                                                                   ^
    symbol:   method getAddressText()
    location: class Reference
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:283: error: cannot find symbol
                  addressText.setText(delivery.getReference().getAddressText());
                                                             ^
    symbol:   method getAddressText()
    location: class Reference
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardViewModel.java:145: error: cannot find symbol
              addressRepository.getBestTippingAddresses(5)
                               ^
    symbol:   method getBestTippingAddresses(int)
    location: variable addressRepository of type AddressRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:443: error: cannot find symbol
                              selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                                 ^
    symbol:   method getId()
    location: class Delivery
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:513: error: cannot find symbol
                              selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                                 ^
    symbol:   method getId()
    location: class Delivery
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:245: error: cannot find symbol
                      if (delivery.getAddress().getLocation() == null ||
                                               ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:246: error: cannot find symbol
                              (delivery.getAddress().getLocation().getLatitude() == 0 &&
                                                    ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:247: error: cannot find symbol
                               delivery.getAddress().getLocation().getLongitude() == 0)) {
                                                    ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:135: error: cannot find symbol
                                          if (profile != null && profile.getUsageStats() != null) {
                                                                        ^
    symbol:   method getUsageStats()
    location: variable profile of type UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:136: error: cannot find symbol
                                              mappingCount = profile.getUsageStats().getMappingCount();
                                                                    ^
    symbol:   method getUsageStats()
    location: variable profile of type UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                              UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                         ^
    symbol:   method getUsageStats()
    location: variable profile of type UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                              UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                         ^
    symbol:   method getUsageStats()
    location: variable profile of type UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:96: error: cannot find symbol
                  .subscribeOn(Schedulers.io())
                  ^
    symbol:   method subscribeOn(Scheduler)
    location: class String
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:157: error: cannot find symbol
              preferenceRepository.setStringPreference(key, value)
                                  ^
    symbol:   method setStringPreference(String,String)
    location: variable preferenceRepository of type PreferenceRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\DataValidationSystem.java:54: error: cannot find symbol
                      delivery.getAddress().getLocation() == null || 
                                           ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:128: error: cannot find symbol
              String orderId = delivery.getMetadata() != null ? delivery.getMetadata().getOrderId() : "";
                                                                                      ^
    symbol:   method getOrderId()
    location: class Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:158: error: cannot find symbol
                      delivery.getAddress().getFlags() != null && 
                                           ^
    symbol:   method getFlags()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:159: error: cannot find symbol
                      delivery.getAddress().getFlags().isDoNotDeliver();
                                           ^
    symbol:   method getFlags()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:234: error: cannot find symbol
              address.setSearchTerms(searchTerms);
                     ^
    symbol:   method setSearchTerms(List<String>)
    location: variable address of type Address
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:264: error: cannot find symbol
              reference.setAddressText(address.getFullAddress());
                       ^
    symbol:   method setAddressText(String)
    location: variable reference of type Reference
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:271: error: cannot find symbol
              amounts.setDeliveryAmount(0.0); // Unknown from GeoJSON
                     ^
    symbol:   method setDeliveryAmount(double)
    location: variable amounts of type Amounts
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:272: error: cannot find symbol
              amounts.setOrderAmount(0.0); // Unknown from GeoJSON
                     ^
    symbol:   method setOrderAmount(double)
    location: variable amounts of type Amounts
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:279: error: cannot find symbol
              metadata.setImportSource("GeoJSON");
                      ^
    symbol:   method setImportSource(String)
    location: variable metadata of type Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:281: error: cannot find symbol
                  metadata.setOrderId(orderId);
                          ^
    symbol:   method setOrderId(String)
    location: variable metadata of type Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:287: error: cannot find symbol
              times.setCreatedAt(new Date());
                   ^
    symbol:   method setCreatedAt(Date)
    location: variable times of type Times
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:348: error: cannot find symbol
              if (delivery.getAddress().getLocation() == null || 
                                       ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:349: error: cannot find symbol
                      (delivery.getAddress().getLocation().getLatitude() == 0 && 
                                            ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:350: error: cannot find symbol
                       delivery.getAddress().getLocation().getLongitude() == 0)) {
                                            ^
    symbol:   method getLocation()
    location: class SimpleAddress
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:285: error: cannot find symbol
          reference.setAddressText(addressText);
                   ^
    symbol:   method setAddressText(String)
    location: variable reference of type Reference
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:290: error: cannot find symbol
          metadata.setOrderId(orderId);
                  ^
    symbol:   method setOrderId(String)
    location: variable metadata of type Metadata
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:297: error: cannot find symbol
          times.setCreatedAt(new Date());
               ^
    symbol:   method setCreatedAt(Date)
    location: variable times of type Times
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ShiptCaptureProcessor.java:65: error: cannot find symbol
                              (delivery.getStatus() == null || !delivery.getStatus().isProcessed())) {
                                                                                    ^
    symbol:   method isProcessed()
    location: class Status
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:120: error: cannot find symbol
                  syncIcon.setImageResource(R.drawable.ic_sync);
                                                      ^
    symbol:   variable ic_sync
    location: class drawable
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:168: error: cannot find symbol
              syncIcon.setImageResource(R.drawable.ic_sync);
                                                  ^
    symbol:   variable ic_sync
    location: class drawable
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:174: error: cannot find symbol
              syncIcon.setImageResource(R.drawable.ic_sync);
                                                  ^
    symbol:   variable ic_sync
    location: class drawable
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:176: error: cannot find symbol
              syncIcon.setImageResource(R.drawable.ic_error);
                                                  ^
    symbol:   variable ic_error
    location: class drawable
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:190: error: cannot find symbol
                  fragment = new DashboardFragment();
                                 ^
    symbol:   class DashboardFragment
    location: class MainActivity
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:199: error: cannot find symbol
                  fragment = new DashboardFragment();
                                 ^
    symbol:   class DashboardFragment
    location: class MainActivity
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:178: error: cannot find symbol
              favoriteToggle = itemView.findViewById(R.id.button_favorite);
                                                         ^
    symbol:   variable button_favorite
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:179: error: cannot find symbol
              favoriteIndicator = itemView.findViewById(R.id.favorite_indicator);
                                                            ^
    symbol:   variable favorite_indicator
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:143: error: cannot find symbol
          View tryNewUiCard = view.findViewById(R.id.try_new_ui_card);
                                                    ^
    symbol:   variable try_new_ui_card
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:273: error: cannot find symbol
                      R.layout.item_recent_activity, recentActivityContainer, false);
                              ^
    symbol:   variable item_recent_activity
    location: class layout
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:277: error: cannot find symbol
              TextView amountText = activityItem.findViewById(R.id.amount_text);
                                                                  ^
    symbol:   variable amount_text
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:330: error: cannot find symbol
              TextView statsText = areaItem.findViewById(R.id.stats_text);
                                                             ^
    symbol:   variable stats_text
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                              UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                         ^
    symbol:   class UsageStats
    location: class UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                              UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                                                                 ^
    symbol:   class UsageStats
    location: class UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                              UserProfile.UsageStats usageStats = profile.getUsageStats();
                                         ^
    symbol:   class UsageStats
    location: class UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:222: error: cannot find symbol
                                  usageStats = new UserProfile.UsageStats();
                                                              ^
    symbol:   class UsageStats
    location: class UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                              UserProfile.UsageStats usageStats = profile.getUsageStats();
                                         ^
    symbol:   class UsageStats
    location: class UserProfile
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:267: error: cannot find symbol
                                  usageStats = new UserProfile.UsageStats();
                                                              ^
    symbol:   class UsageStats
    location: class UserProfile
  Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
  100 errors

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to get full insights.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:compileDebugJavaWithJavac'.
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:130)
	at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:293)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:128)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:116)
	at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
	at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:51)
	at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
	at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:74)
	at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
	at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:42)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:331)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:318)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.lambda$execute$0(DefaultTaskExecutionGraph.java:314)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:314)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:303)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:459)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:376)
	at org.gradle.execution.plan.DefaultPlanExecutor.process(DefaultPlanExecutor.java:111)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.executeWithServices(DefaultTaskExecutionGraph.java:138)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.execute(DefaultTaskExecutionGraph.java:123)
	at org.gradle.execution.SelectedTaskExecutionAction.execute(SelectedTaskExecutionAction.java:35)
	at org.gradle.execution.DryRunBuildExecutionAction.execute(DryRunBuildExecutionAction.java:51)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:54)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:43)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor.execute(BuildOperationFiringBuildWorkerExecutor.java:40)
	at org.gradle.internal.build.DefaultBuildLifecycleController.lambda$executeTasks$10(DefaultBuildLifecycleController.java:313)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266)
	at org.gradle.internal.model.StateTransitionController.lambda$tryTransition$8(StateTransitionController.java:177)
	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:46)
	at org.gradle.internal.model.StateTransitionController.tryTransition(StateTransitionController.java:177)
	at org.gradle.internal.build.DefaultBuildLifecycleController.executeTasks(DefaultBuildLifecycleController.java:304)
	at org.gradle.internal.build.DefaultBuildWorkGraphController$DefaultBuildWorkGraph.runWork(DefaultBuildWorkGraphController.java:220)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:263)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)
	at org.gradle.composite.internal.DefaultBuildController.doRun(DefaultBuildController.java:181)
	at org.gradle.composite.internal.DefaultBuildController.access$000(DefaultBuildController.java:50)
	at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.lambda$run$0(DefaultBuildController.java:198)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)
	at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.run(DefaultBuildController.java:198)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:48)
Caused by: org.gradle.api.internal.tasks.compile.CompilationFailedException: Compilation failed; see the compiler output below.
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:292: error: incompatible types: AggregateSource cannot be converted to Source
                    .get(com.google.firebase.firestore.AggregateSource.SERVER)
                                                                      ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\import\BulkUploadViewModel.java:63: error: incompatible types: Object cannot be converted to MapFragment
        importManager = new ImportManager(context, addressRepository, deliveryRepository, syncRepository, mapFragment);
                                                                                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:250: error: incompatible types: SimpleAddress cannot be converted to Address
                            Address geocodedAddress = addressRepository.geocodeAddress(delivery.getAddress())
                                                                                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:252: error: incompatible types: Address cannot be converted to SimpleAddress
                            delivery.setAddress(geocodedAddress);
                                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:294: error: incompatible types: SimpleAddress cannot be converted to Address
                            addressRepository.addAddress(delivery.getAddress())
                                                                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:298: error: incompatible types: Address cannot be converted to SimpleAddress
                            delivery.setAddress(existingAddress);
                                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:170: error: incompatible types: Throwable cannot be converted to Exception
                                                                callback.onError(error);
                                                                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:180: error: incompatible types: Throwable cannot be converted to Exception
                                            callback.onError(error);
                                                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:189: error: incompatible types: Throwable cannot be converted to Exception
                            callback.onError(error);
                                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:216: error: incompatible types: Coordinates cannot be converted to Location
            address.setLocation(coordinates);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:265: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
            delivery.setReference(reference);
                                  ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:266: error: incompatible types: Address cannot be converted to SimpleAddress
            delivery.setAddress(address);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:273: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
            delivery.setAmounts(amounts);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:283: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
            delivery.setMetadata(metadata);
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:288: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
            delivery.setTimes(times);
                              ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:294: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
            delivery.setStatus(status);
                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:416: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                    Reference reference = delivery.getReference();
                                                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:418: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                    delivery.setReference(reference);
                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:422: error: incompatible types: SimpleAddress cannot be converted to Address
                        addressRepository.updateAddress(delivery.getAddress())
                                                                           ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:427: error: incompatible types: Address cannot be converted to SimpleAddress
                        delivery.setAddress(existingAddress);
                                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:431: error: incompatible types: SimpleAddress cannot be converted to Address
                    DocumentReference addressRef = addressRepository.addAddress(delivery.getAddress())
                                                                                                   ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:437: error: incompatible types: com.autogratuity.data.model.Delivery.Reference cannot be converted to com.autogratuity.data.model.Reference
                    Reference reference = delivery.getReference();
                                                               ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:439: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
                    delivery.setReference(reference);
                                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:274: error: incompatible types: Coordinates cannot be converted to Location
            address.setLocation(location);
                                ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:281: error: incompatible types: Address cannot be converted to SimpleAddress
        delivery.setAddress(address);
                            ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:286: error: incompatible types: com.autogratuity.data.model.Reference cannot be converted to com.autogratuity.data.model.Delivery.Reference
        delivery.setReference(reference);
                              ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:293: error: incompatible types: com.autogratuity.data.model.Metadata cannot be converted to com.autogratuity.data.model.Delivery.Metadata
        delivery.setMetadata(metadata);
                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:299: error: incompatible types: com.autogratuity.data.model.Times cannot be converted to com.autogratuity.data.model.Delivery.Times
        delivery.setTimes(times);
                          ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:305: error: incompatible types: com.autogratuity.data.model.Status cannot be converted to com.autogratuity.data.model.Delivery.Status
        delivery.setStatus(status);
                           ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:311: error: incompatible types: com.autogratuity.data.model.Amounts cannot be converted to com.autogratuity.data.model.Delivery.Amounts
            delivery.setAmounts(amounts);
                                ^
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Note: Recompile with -Xlint:deprecation for details.
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:314: error: local variables referenced from a lambda expression must be final or effectively final
            final Map<String, Object> finalDeviceInfo = new HashMap<>(deviceInfo);
                                                                      ^
Note: Some input files use or override a deprecated API.
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:125: error: non-static variable dateFormat cannot be referenced from a static context
                dateText.setText(dateFormat.format(deliveryDate));
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\adapters\DeliveriesAdapter.java:127: error: non-static variable dateFormat cannot be referenced from a static context
                dateText.setText(dateFormat.format(delivery.getMetadata().getCreatedAt()));
                                 ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:70: error: method getInstance in class SubscriptionManager cannot be applied to given types;
        subscriptionManager = SubscriptionManager.getInstance(this);
                                                 ^
  required: Context,SubscriptionRepository,PreferenceRepository
  found:    ProSubscribeActivity
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\CaptureProcessReceiver.java:37: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
        ShiptCaptureProcessor processor = new ShiptCaptureProcessor(context);
                                          ^
  required: Context,DeliveryRepository,AddressRepository
  found:    Context
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:47: error: method getInstance in class AuthenticationManager cannot be applied to given types;
            authManager = AuthenticationManager.getInstance();
                                               ^
  required: Context
  found:    no arguments
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\ShiptCaptureBackgroundService.java:147: error: constructor ShiptCaptureProcessor in class ShiptCaptureProcessor cannot be applied to given types;
            ShiptCaptureProcessor processor = new ShiptCaptureProcessor(this);
                                              ^
  required: Context,DeliveryRepository,AddressRepository
  found:    ShiptCaptureBackgroundService
  reason: actual and formal argument lists differ in length
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:159: error: cannot find symbol
                .subscribeOn(Schedulers.io())
                ^
  symbol:   method subscribeOn(Scheduler)
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:164: error: cannot find symbol
                        preferenceRepository.setLongPreference("user_registered_at", System.currentTimeMillis())
                                            ^
  symbol:   method setLongPreference(String,long)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:169: error: cannot find symbol
                        preferenceRepository.setStringPreference("user_email", user.getEmail())
                                            ^
  symbol:   method setStringPreference(String,String)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ProSubscribeActivity.java:71: error: cannot find symbol
        subscriptionManager.setListener(this);
                           ^
  symbol:   method setListener(ProSubscribeActivity)
  location: variable subscriptionManager of type SubscriptionManager
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\subscription\SubscriptionViewModel.java:109: error: cannot find symbol
            subscriptionRepository.isTrialAvailable()
                                  ^
  symbol:   method isTrialAvailable()
  location: variable subscriptionRepository of type SubscriptionRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:287: error: cannot find symbol
            status.setPurchaseToken(purchase.getPurchaseToken());
                  ^
  symbol:   method setPurchaseToken(String)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:288: error: cannot find symbol
            status.setLastUpdated(new Date());
                  ^
  symbol:   method setLastUpdated(Date)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\SubscriptionManager.java:384: error: cannot find symbol
            status.setLastUpdated(new Date());
                  ^
  symbol:   method setLastUpdated(Date)
  location: variable status of type SubscriptionStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\receivers\BootCompletedReceiver.java:39: error: cannot find symbol
                    subscriptionRepository.getCurrentSubscriptionStatus()
                                          ^
  symbol:   method getCurrentSubscriptionStatus()
  location: variable subscriptionRepository of type SubscriptionRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\services\DoNotDeliverService.java:148: error: cannot find symbol
        delivery.getStatus().setDoNotDeliver(true);
                            ^
  symbol:   method setDoNotDeliver(boolean)
  location: class Status
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ShiptNotificationListenerService.java:406: error: cannot find symbol
                        orderId.equals(delivery.getReference().getOrderId())) {
                                                              ^
  symbol:   method getOrderId()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:102: error: cannot find symbol
        if (address.getMetadata() != null && address.getMetadata().getCustomData() != null) {
                                                                  ^
  symbol:   method getCustomData()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:103: error: cannot find symbol
            Object dnpObj = address.getMetadata().getCustomData().get("doNotDeliver");
                                                 ^
  symbol:   method getCustomData()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:282: error: cannot find symbol
            } else if (delivery.getReference() != null && delivery.getReference().getAddressText() != null) {
                                                                                 ^
  symbol:   method getAddressText()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:283: error: cannot find symbol
                addressText.setText(delivery.getReference().getAddressText());
                                                           ^
  symbol:   method getAddressText()
  location: class Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardViewModel.java:145: error: cannot find symbol
            addressRepository.getBestTippingAddresses(5)
                             ^
  symbol:   method getBestTippingAddresses(int)
  location: variable addressRepository of type AddressRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:443: error: cannot find symbol
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                               ^
  symbol:   method getId()
  location: class Delivery
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\delivery\DeliveryViewModel.java:513: error: cannot find symbol
                            selectedDeliveryLiveData.getValue().getId().equals(deliveryId)) {
                                                               ^
  symbol:   method getId()
  location: class Delivery
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:245: error: cannot find symbol
                    if (delivery.getAddress().getLocation() == null ||
                                             ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:246: error: cannot find symbol
                            (delivery.getAddress().getLocation().getLatitude() == 0 &&
                                                  ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ImportManager.java:247: error: cannot find symbol
                             delivery.getAddress().getLocation().getLongitude() == 0)) {
                                                  ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:135: error: cannot find symbol
                                        if (profile != null && profile.getUsageStats() != null) {
                                                                      ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:136: error: cannot find symbol
                                            mappingCount = profile.getUsageStats().getMappingCount();
                                                                  ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                       ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                                                       ^
  symbol:   method getUsageStats()
  location: variable profile of type UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:96: error: cannot find symbol
                .subscribeOn(Schedulers.io())
                ^
  symbol:   method subscribeOn(Scheduler)
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\webapp\WebAppViewModel.java:157: error: cannot find symbol
            preferenceRepository.setStringPreference(key, value)
                                ^
  symbol:   method setStringPreference(String,String)
  location: variable preferenceRepository of type PreferenceRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\DataValidationSystem.java:54: error: cannot find symbol
                    delivery.getAddress().getLocation() == null || 
                                         ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:128: error: cannot find symbol
            String orderId = delivery.getMetadata() != null ? delivery.getMetadata().getOrderId() : "";
                                                                                    ^
  symbol:   method getOrderId()
  location: class Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:158: error: cannot find symbol
                    delivery.getAddress().getFlags() != null && 
                                         ^
  symbol:   method getFlags()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ExportManager.java:159: error: cannot find symbol
                    delivery.getAddress().getFlags().isDoNotDeliver();
                                         ^
  symbol:   method getFlags()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:234: error: cannot find symbol
            address.setSearchTerms(searchTerms);
                   ^
  symbol:   method setSearchTerms(List<String>)
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:264: error: cannot find symbol
            reference.setAddressText(address.getFullAddress());
                     ^
  symbol:   method setAddressText(String)
  location: variable reference of type Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:271: error: cannot find symbol
            amounts.setDeliveryAmount(0.0); // Unknown from GeoJSON
                   ^
  symbol:   method setDeliveryAmount(double)
  location: variable amounts of type Amounts
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:272: error: cannot find symbol
            amounts.setOrderAmount(0.0); // Unknown from GeoJSON
                   ^
  symbol:   method setOrderAmount(double)
  location: variable amounts of type Amounts
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:279: error: cannot find symbol
            metadata.setImportSource("GeoJSON");
                    ^
  symbol:   method setImportSource(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:281: error: cannot find symbol
                metadata.setOrderId(orderId);
                        ^
  symbol:   method setOrderId(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:287: error: cannot find symbol
            times.setCreatedAt(new Date());
                 ^
  symbol:   method setCreatedAt(Date)
  location: variable times of type Times
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:348: error: cannot find symbol
            if (delivery.getAddress().getLocation() == null || 
                                     ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:349: error: cannot find symbol
                    (delivery.getAddress().getLocation().getLatitude() == 0 && 
                                          ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\GeoJsonImportUtil.java:350: error: cannot find symbol
                     delivery.getAddress().getLocation().getLongitude() == 0)) {
                                          ^
  symbol:   method getLocation()
  location: class SimpleAddress
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:285: error: cannot find symbol
        reference.setAddressText(addressText);
                 ^
  symbol:   method setAddressText(String)
  location: variable reference of type Reference
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:290: error: cannot find symbol
        metadata.setOrderId(orderId);
                ^
  symbol:   method setOrderId(String)
  location: variable metadata of type Metadata
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\KmlImportUtil.java:297: error: cannot find symbol
        times.setCreatedAt(new Date());
             ^
  symbol:   method setCreatedAt(Date)
  location: variable times of type Times
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\ShiptCaptureProcessor.java:65: error: cannot find symbol
                            (delivery.getStatus() == null || !delivery.getStatus().isProcessed())) {
                                                                                  ^
  symbol:   method isProcessed()
  location: class Status
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:120: error: cannot find symbol
                syncIcon.setImageResource(R.drawable.ic_sync);
                                                    ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:168: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_sync);
                                                ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:174: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_sync);
                                                ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:176: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_error);
                                                ^
  symbol:   variable ic_error
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:190: error: cannot find symbol
                fragment = new DashboardFragment();
                               ^
  symbol:   class DashboardFragment
  location: class MainActivity
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:199: error: cannot find symbol
                fragment = new DashboardFragment();
                               ^
  symbol:   class DashboardFragment
  location: class MainActivity
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:178: error: cannot find symbol
            favoriteToggle = itemView.findViewById(R.id.button_favorite);
                                                       ^
  symbol:   variable button_favorite
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:179: error: cannot find symbol
            favoriteIndicator = itemView.findViewById(R.id.favorite_indicator);
                                                          ^
  symbol:   variable favorite_indicator
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:143: error: cannot find symbol
        View tryNewUiCard = view.findViewById(R.id.try_new_ui_card);
                                                  ^
  symbol:   variable try_new_ui_card
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:273: error: cannot find symbol
                    R.layout.item_recent_activity, recentActivityContainer, false);
                            ^
  symbol:   variable item_recent_activity
  location: class layout
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:277: error: cannot find symbol
            TextView amountText = activityItem.findViewById(R.id.amount_text);
                                                                ^
  symbol:   variable amount_text
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\dashboard\DashboardFragment.java:330: error: cannot find symbol
            TextView statsText = areaItem.findViewById(R.id.stats_text);
                                                           ^
  symbol:   variable stats_text
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                            UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:152: error: cannot find symbol
                                            UserProfile.UsageStats usageStats = new UserProfile.UsageStats();
                                                                                               ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:220: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:222: error: cannot find symbol
                                usageStats = new UserProfile.UsageStats();
                                                            ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:265: error: cannot find symbol
                            UserProfile.UsageStats usageStats = profile.getUsageStats();
                                       ^
  symbol:   class UsageStats
  location: class UserProfile
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\utils\UsageTracker.java:267: error: cannot find symbol
                                usageStats = new UserProfile.UsageStats();
                                                            ^
  symbol:   class UsageStats
  location: class UserProfile
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
100 errors
	at org.gradle.api.internal.tasks.compile.JdkJavaCompiler.execute(JdkJavaCompiler.java:84)
	at org.gradle.api.internal.tasks.compile.JdkJavaCompiler.execute(JdkJavaCompiler.java:46)
	at org.gradle.api.internal.tasks.compile.NormalizingJavaCompiler.delegateAndHandleErrors(NormalizingJavaCompiler.java:98)
	at org.gradle.api.internal.tasks.compile.NormalizingJavaCompiler.execute(NormalizingJavaCompiler.java:52)
	at org.gradle.api.internal.tasks.compile.NormalizingJavaCompiler.execute(NormalizingJavaCompiler.java:38)
	at org.gradle.api.internal.tasks.compile.AnnotationProcessorDiscoveringCompiler.execute(AnnotationProcessorDiscoveringCompiler.java:52)
	at org.gradle.api.internal.tasks.compile.AnnotationProcessorDiscoveringCompiler.execute(AnnotationProcessorDiscoveringCompiler.java:38)
	at org.gradle.api.internal.tasks.compile.ModuleApplicationNameWritingCompiler.execute(ModuleApplicationNameWritingCompiler.java:46)
	at org.gradle.api.internal.tasks.compile.ModuleApplicationNameWritingCompiler.execute(ModuleApplicationNameWritingCompiler.java:36)
	at org.gradle.jvm.toolchain.internal.DefaultToolchainJavaCompiler.execute(DefaultToolchainJavaCompiler.java:57)
	at org.gradle.api.tasks.compile.JavaCompile.lambda$createToolchainCompiler$3(JavaCompile.java:205)
	at org.gradle.api.internal.tasks.compile.CleaningJavaCompiler.execute(CleaningJavaCompiler.java:53)
	at org.gradle.api.internal.tasks.compile.incremental.IncrementalCompilerFactory.lambda$createRebuildAllCompiler$0(IncrementalCompilerFactory.java:52)
	at org.gradle.api.internal.tasks.compile.incremental.SelectiveCompiler.execute(SelectiveCompiler.java:70)
	at org.gradle.api.internal.tasks.compile.incremental.SelectiveCompiler.execute(SelectiveCompiler.java:44)
	at org.gradle.api.internal.tasks.compile.incremental.IncrementalResultStoringCompiler.execute(IncrementalResultStoringCompiler.java:66)
	at org.gradle.api.internal.tasks.compile.incremental.IncrementalResultStoringCompiler.execute(IncrementalResultStoringCompiler.java:52)
	at org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationReportingCompiler$1.call(CompileJavaBuildOperationReportingCompiler.java:64)
	at org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationReportingCompiler$1.call(CompileJavaBuildOperationReportingCompiler.java:48)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.api.internal.tasks.compile.CompileJavaBuildOperationReportingCompiler.execute(CompileJavaBuildOperationReportingCompiler.java:48)
	at org.gradle.api.tasks.compile.JavaCompile.performCompilation(JavaCompile.java:223)
	at org.gradle.api.tasks.compile.JavaCompile.performIncrementalCompilation(JavaCompile.java:164)
	at org.gradle.api.tasks.compile.JavaCompile.compile(JavaCompile.java:149)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)
	at org.gradle.internal.reflect.JavaMethod.invoke(JavaMethod.java:125)
	at org.gradle.api.internal.project.taskfactory.IncrementalTaskAction.doExecute(IncrementalTaskAction.java:45)
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:51)
	at org.gradle.api.internal.project.taskfactory.IncrementalTaskAction.execute(IncrementalTaskAction.java:26)
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:29)
	at org.gradle.api.internal.tasks.execution.TaskExecution$3.run(TaskExecution.java:244)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:29)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:26)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:47)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeAction(TaskExecution.java:229)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeActions(TaskExecution.java:212)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeWithPreviousOutputFiles(TaskExecution.java:195)
	at org.gradle.api.internal.tasks.execution.TaskExecution.execute(TaskExecution.java:162)
	at org.gradle.internal.execution.steps.ExecuteStep.executeInternal(ExecuteStep.java:105)
	at org.gradle.internal.execution.steps.ExecuteStep.access$000(ExecuteStep.java:44)
	at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:59)
	at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:56)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:56)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:44)
	at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:42)
	at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:75)
	at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:55)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:50)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:28)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:67)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:37)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:61)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:26)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:69)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:46)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:40)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:29)
	at org.gradle.internal.execution.steps.BuildCacheStep.executeWithoutCache(BuildCacheStep.java:189)
	at org.gradle.internal.execution.steps.BuildCacheStep.lambda$execute$1(BuildCacheStep.java:75)
	at org.gradle.internal.Either$Right.fold(Either.java:175)
	at org.gradle.internal.execution.caching.CachingState.fold(CachingState.java:62)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:73)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:48)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:46)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:35)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:75)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$2(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:35)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:37)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:27)
	at org.gradle.internal.execution.steps.ResolveIncrementalCachingStateStep.executeDelegate(ResolveIncrementalCachingStateStep.java:49)
	at org.gradle.internal.execution.steps.ResolveIncrementalCachingStateStep.executeDelegate(ResolveIncrementalCachingStateStep.java:27)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:71)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:39)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:65)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:36)
	at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:107)
	at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:56)
	at org.gradle.internal.execution.steps.AbstractCaptureStateBeforeExecutionStep.execute(AbstractCaptureStateBeforeExecutionStep.java:64)
	at org.gradle.internal.execution.steps.AbstractCaptureStateBeforeExecutionStep.execute(AbstractCaptureStateBeforeExecutionStep.java:43)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.executeWithNonEmptySources(AbstractSkipEmptyWorkStep.java:125)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.execute(AbstractSkipEmptyWorkStep.java:61)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.execute(AbstractSkipEmptyWorkStep.java:36)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:38)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:36)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:23)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.execute(HandleStaleOutputsStep.java:75)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.execute(HandleStaleOutputsStep.java:41)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.lambda$execute$0(AssignMutableWorkspaceStep.java:35)
	at org.gradle.api.internal.tasks.execution.TaskExecution$4.withWorkspace(TaskExecution.java:289)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.execute(AssignMutableWorkspaceStep.java:31)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.execute(AssignMutableWorkspaceStep.java:22)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:40)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:23)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.lambda$execute$2(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:39)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:46)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:34)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:48)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:35)
	at org.gradle.internal.execution.impl.DefaultExecutionEngine$1.execute(DefaultExecutionEngine.java:61)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:127)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:116)
	at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
	at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:51)
	at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
	at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:74)
	at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
	at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:42)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:331)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:318)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.lambda$execute$0(DefaultTaskExecutionGraph.java:314)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:314)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:303)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:459)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:376)
	at org.gradle.execution.plan.DefaultPlanExecutor.process(DefaultPlanExecutor.java:111)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.executeWithServices(DefaultTaskExecutionGraph.java:138)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.execute(DefaultTaskExecutionGraph.java:123)
	at org.gradle.execution.SelectedTaskExecutionAction.execute(SelectedTaskExecutionAction.java:35)
	at org.gradle.execution.DryRunBuildExecutionAction.execute(DryRunBuildExecutionAction.java:51)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:54)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:43)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)
	at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor.execute(BuildOperationFiringBuildWorkerExecutor.java:40)
	at org.gradle.internal.build.DefaultBuildLifecycleController.lambda$executeTasks$10(DefaultBuildLifecycleController.java:313)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266)
	at org.gradle.internal.model.StateTransitionController.lambda$tryTransition$8(StateTransitionController.java:177)
	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:46)
	at org.gradle.internal.model.StateTransitionController.tryTransition(StateTransitionController.java:177)
	at org.gradle.internal.build.DefaultBuildLifecycleController.executeTasks(DefaultBuildLifecycleController.java:304)
	at org.gradle.internal.build.DefaultBuildWorkGraphController$DefaultBuildWorkGraph.runWork(DefaultBuildWorkGraphController.java:220)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:263)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)
	at org.gradle.composite.internal.DefaultBuildController.doRun(DefaultBuildController.java:181)
	at org.gradle.composite.internal.DefaultBuildController.access$000(DefaultBuildController.java:50)
	at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.lambda$run$0(DefaultBuildController.java:198)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)
	at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.run(DefaultBuildController.java:198)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:48)


BUILD FAILED in 3s
30 actionable tasks: 1 executed, 29 up-to-date
