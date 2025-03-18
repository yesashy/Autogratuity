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

> Task :app:compileDebugJavaWithJavac FAILED
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:312: error: local variables referenced from a lambda expression must be final or effectively final
                            docRef.set(deviceInfo)
                                       ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:212: error: cannot find symbol
            operation.setUserId(getUserId());
                                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:276: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:360: error: cannot find symbol
                                operation.markAsFailed(
                                         ^
  symbol:   method markAsFailed(String,String)
  location: variable operation of type SyncOperation
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:579: error: cannot find symbol
                errorMap.put("code", operation.getError().getCode());
                                                         ^
  symbol:   method getCode()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:580: error: cannot find symbol
                errorMap.put("message", operation.getError().getMessage());
                                                            ^
  symbol:   method getMessage()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:581: error: cannot find symbol
                errorMap.put("timestamp", operation.getError().getTimestamp());
                                                              ^
  symbol:   method getTimestamp()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:650: error: cannot find symbol
                invalidateCache("userProfile_" + getUserId());
                                                 ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:653: error: cannot find symbol
                invalidateCache("subscriptionStatus_" + getUserId());
                                                        ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:657: error: cannot find symbol
                invalidateCache("addresses_" + getUserId());
                                               ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:661: error: cannot find symbol
                invalidateCache("deliveries_" + getUserId() + "_*");
                                                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:662: error: cannot find symbol
                invalidateCache("delivery_stats_" + getUserId());
                                                    ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:676: error: cannot find symbol
        String docId = getUserId() + "_" + deviceId;
                       ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:701: error: cannot find symbol
                            deviceData.put("userId", getUserId());
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:760: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:788: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:809: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:822: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:856: error: cannot find symbol
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:872: error: cannot find symbol
            operation.setStatus(SyncOperation.STATUS_PENDING);
                     ^
  symbol:   method setStatus(String)
  location: variable operation of type Object
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:873: error: cannot find symbol
            operation.setUpdatedAt(new Date());
                     ^
  symbol:   method setUpdatedAt(Date)
  location: variable operation of type Object
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:892: error: incompatible types: Object cannot be converted to SyncOperation
                                processSyncOperation(operation)
                                                     ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:969: error: cannot find symbol
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1025: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1053: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1095: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1206: error: cannot find symbol
            SyncRepository syncRepo = (SyncRepository) RepositoryProvider.getRepository();
                                                       ^
  symbol:   variable RepositoryProvider
  location: class SyncWorker
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:205: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing createdAt timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:220: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastLoginAt timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:234: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing privacyPolicyAccepted timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:248: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing termsAccepted timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:300: error: incompatible types: Date cannot be converted to Timestamp
        profile.setCreatedAt(new Date());
                             ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:336: error: cannot find symbol
            Log.e("UserProfileSerializer", "Invalid profile: userId is required");
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:343: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: email format is invalid");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:353: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: accountStatus is invalid");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:360: error: cannot find symbol
            Log.e("UserProfileSerializer", "Invalid profile: version must be non-negative");
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:370: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: defaultTipPercentage must be between 0 and 100");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:378: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: theme must be 'light', 'dark', or 'system'");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:394: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: subscription status is invalid");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:402: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: subscription expiryDate cannot be before startDate");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:483: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing startDate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:497: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing expiryDate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:511: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastVerified timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:662: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastUsageUpdate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
        List<String> deviceIds = (List<String>) map.get("deviceIds");
        ^
  symbol:   class List
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
        List<String> deviceIds = (List<String>) map.get("deviceIds");
                                  ^
  symbol:   class List
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:713: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastSyncTime timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:843: error: cannot find symbol
            Log.e("UserProfileSerializer", "Error deserializing profile from JSON", e);
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:163: error: cannot find symbol
        View view = inflater.inflate(R.layout.dialog_delivery_detail, null);
                                             ^
  symbol:   variable dialog_delivery_detail
  location: class layout
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:172: error: cannot find symbol
        updateButton = view.findViewById(R.id.update_button);
                                             ^
  symbol:   variable update_button
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:173: error: cannot find symbol
        deleteButton = view.findViewById(R.id.delete_button);
                                             ^
  symbol:   variable delete_button
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:174: error: cannot find symbol
        closeButton = view.findViewById(R.id.close_button);
                                            ^
  symbol:   variable close_button
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:62: error: cannot find symbol
            configRepository.getConfigValue("faq_content", "")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:91: error: cannot find symbol
            configRepository.getConfigValue("faq_title", "Knowledge Base")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:111: error: cannot find symbol
            configRepository.getConfigBoolean("track_faq_views", true)
                            ^
  symbol:   method getConfigBoolean(String,boolean)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:116: error: cannot find symbol
                        return configRepository.incrementCounter("faq_view_count");
                                               ^
  symbol:   method incrementCounter(String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:118: error: cannot find symbol
                    return configRepository.noOpCompletable();
                                           ^
  symbol:   method noOpCompletable()
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:158: error: cannot find symbol
            configRepository.getConfigValue("default_preferences", "{}")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:173: error: cannot find symbol
        if (status.isInProgress()) {
                  ^
  symbol:   method isInProgress()
  location: variable status of type SyncStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:174: error: cannot find symbol
            syncIcon.setImageResource(R.drawable.ic_sync);
                                                ^
  symbol:   variable ic_sync
  location: class drawable
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:175: error: cannot find symbol
        } else if (status.isError()) {
                         ^
  symbol:   method isError()
  location: variable status of type SyncStatus
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:228: error: cannot find symbol
        if (status.isInProgress()) {
                  ^
  symbol:   method isInProgress()
  location: variable status of type SyncStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:233: error: cannot find symbol
        if (status.isError()) {
                  ^
  symbol:   method isError()
  location: variable status of type SyncStatus
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                    return oldItem.getId().equals(newItem.getId());
                                  ^
  symbol:   method getId()
  location: variable oldItem of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                    return oldItem.getId().equals(newItem.getId());
                                                         ^
  symbol:   method getId()
  location: variable newItem of type Address
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:132: error: cannot find symbol
                address.isFavorite() ? View.VISIBLE : View.GONE);
                       ^
  symbol:   method isFavorite()
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                                      ^
  symbol:   method isFavorite()
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                    ^
  symbol:   method getId()
  location: variable address of type Address
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
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
100 errors

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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:892: error: incompatible types: Object cannot be converted to SyncOperation
                                  processSyncOperation(operation)
                                                       ^
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:300: error: incompatible types: Date cannot be converted to Timestamp
          profile.setCreatedAt(new Date());
                               ^
  Note: Some input files use unchecked or unsafe operations.
  Note: Recompile with -Xlint:unchecked for details.
  Note: Recompile with -Xlint:deprecation for details.
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:312: error: local variables referenced from a lambda expression must be final or effectively final
                              docRef.set(deviceInfo)
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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:212: error: cannot find symbol
              operation.setUserId(getUserId());
                                  ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:276: error: cannot find symbol
                      .whereEqualTo("userId", getUserId())
                                              ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:360: error: cannot find symbol
                                  operation.markAsFailed(
                                           ^
    symbol:   method markAsFailed(String,String)
    location: variable operation of type SyncOperation
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:579: error: cannot find symbol
                  errorMap.put("code", operation.getError().getCode());
                                                           ^
    symbol:   method getCode()
    location: class String
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:580: error: cannot find symbol
                  errorMap.put("message", operation.getError().getMessage());
                                                              ^
    symbol:   method getMessage()
    location: class String
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:581: error: cannot find symbol
                  errorMap.put("timestamp", operation.getError().getTimestamp());
                                                                ^
    symbol:   method getTimestamp()
    location: class String
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:650: error: cannot find symbol
                  invalidateCache("userProfile_" + getUserId());
                                                   ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:653: error: cannot find symbol
                  invalidateCache("subscriptionStatus_" + getUserId());
                                                          ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:657: error: cannot find symbol
                  invalidateCache("addresses_" + getUserId());
                                                 ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:661: error: cannot find symbol
                  invalidateCache("deliveries_" + getUserId() + "_*");
                                                  ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:662: error: cannot find symbol
                  invalidateCache("delivery_stats_" + getUserId());
                                                      ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:676: error: cannot find symbol
          String docId = getUserId() + "_" + deviceId;
                         ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:701: error: cannot find symbol
                              deviceData.put("userId", getUserId());
                                                       ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:760: error: cannot find symbol
                  getUserId(),
                  ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:788: error: cannot find symbol
                  getUserId(),
                  ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:809: error: cannot find symbol
                  getUserId(),
                  ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:822: error: cannot find symbol
                      .whereEqualTo("userId", getUserId())
                                              ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:856: error: cannot find symbol
                              if (operation != null && getUserId().equals(operation.getUserId())) {
                                                       ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:872: error: cannot find symbol
              operation.setStatus(SyncOperation.STATUS_PENDING);
                       ^
    symbol:   method setStatus(String)
    location: variable operation of type Object
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:873: error: cannot find symbol
              operation.setUpdatedAt(new Date());
                       ^
    symbol:   method setUpdatedAt(Date)
    location: variable operation of type Object
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:969: error: cannot find symbol
                              if (operation != null && getUserId().equals(operation.getUserId())) {
                                                       ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1025: error: cannot find symbol
                      .whereEqualTo("userId", getUserId())
                                              ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1053: error: cannot find symbol
                      .whereEqualTo("userId", getUserId())
                                              ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1095: error: cannot find symbol
                      .whereEqualTo("userId", getUserId())
                                              ^
    symbol:   method getUserId()
    location: class SyncRepositoryImpl
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:62: error: cannot find symbol
              configRepository.getConfigValue("faq_content", "")
                              ^
    symbol:   method getConfigValue(String,String)
    location: variable configRepository of type ConfigRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:91: error: cannot find symbol
              configRepository.getConfigValue("faq_title", "Knowledge Base")
                              ^
    symbol:   method getConfigValue(String,String)
    location: variable configRepository of type ConfigRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:111: error: cannot find symbol
              configRepository.getConfigBoolean("track_faq_views", true)
                              ^
    symbol:   method getConfigBoolean(String,boolean)
    location: variable configRepository of type ConfigRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:116: error: cannot find symbol
                          return configRepository.incrementCounter("faq_view_count");
                                                 ^
    symbol:   method incrementCounter(String)
    location: variable configRepository of type ConfigRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:118: error: cannot find symbol
                      return configRepository.noOpCompletable();
                                             ^
    symbol:   method noOpCompletable()
    location: variable configRepository of type ConfigRepository
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:158: error: cannot find symbol
              configRepository.getConfigValue("default_preferences", "{}")
                              ^
    symbol:   method getConfigValue(String,String)
    location: variable configRepository of type ConfigRepository
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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:173: error: cannot find symbol
          if (status.isInProgress()) {
                    ^
    symbol:   method isInProgress()
    location: variable status of type SyncStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:175: error: cannot find symbol
          } else if (status.isError()) {
                           ^
    symbol:   method isError()
    location: variable status of type SyncStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:228: error: cannot find symbol
          if (status.isInProgress()) {
                    ^
    symbol:   method isInProgress()
    location: variable status of type SyncStatus
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:233: error: cannot find symbol
          if (status.isError()) {
                    ^
    symbol:   method isError()
    location: variable status of type SyncStatus
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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                      return oldItem.getId().equals(newItem.getId());
                                    ^
    symbol:   method getId()
    location: variable oldItem of type Address
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                      return oldItem.getId().equals(newItem.getId());
                                                           ^
    symbol:   method getId()
    location: variable newItem of type Address
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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:132: error: cannot find symbol
                  address.isFavorite() ? View.VISIBLE : View.GONE);
                         ^
    symbol:   method isFavorite()
    location: variable address of type Address
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                  viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                                        ^
    symbol:   method isFavorite()
    location: variable address of type Address
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                  viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                      ^
    symbol:   method getId()
    location: variable address of type Address
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
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1206: error: cannot find symbol
              SyncRepository syncRepo = (SyncRepository) RepositoryProvider.getRepository();
                                                         ^
    symbol:   variable RepositoryProvider
    location: class SyncWorker
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:205: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing createdAt timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:220: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing lastLoginAt timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:234: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing privacyPolicyAccepted timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:248: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing termsAccepted timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:336: error: cannot find symbol
              Log.e("UserProfileSerializer", "Invalid profile: userId is required");
              ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:343: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Invalid profile: email format is invalid");
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:353: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Invalid profile: accountStatus is invalid");
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:360: error: cannot find symbol
              Log.e("UserProfileSerializer", "Invalid profile: version must be non-negative");
              ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:370: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Invalid profile: defaultTipPercentage must be between 0 and 100");
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:378: error: cannot find symbol
                      Log.e("UserProfileSerializer", "Invalid profile: theme must be 'light', 'dark', or 'system'");
                      ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:394: error: cannot find symbol
                      Log.e("UserProfileSerializer", "Invalid profile: subscription status is invalid");
                      ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:402: error: cannot find symbol
                      Log.e("UserProfileSerializer", "Invalid profile: subscription expiryDate cannot be before startDate");
                      ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:483: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing startDate timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:497: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing expiryDate timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:511: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing lastVerified timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:662: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing lastUsageUpdate timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
          List<String> deviceIds = (List<String>) map.get("deviceIds");
          ^
    symbol:   class List
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
          List<String> deviceIds = (List<String>) map.get("deviceIds");
                                    ^
    symbol:   class List
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:713: error: cannot find symbol
                  Log.e("UserProfileSerializer", "Error parsing lastSyncTime timestamp", e);
                  ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:843: error: cannot find symbol
              Log.e("UserProfileSerializer", "Error deserializing profile from JSON", e);
              ^
    symbol:   variable Log
    location: class UserProfileSerializer
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:163: error: cannot find symbol
          View view = inflater.inflate(R.layout.dialog_delivery_detail, null);
                                               ^
    symbol:   variable dialog_delivery_detail
    location: class layout
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:172: error: cannot find symbol
          updateButton = view.findViewById(R.id.update_button);
                                               ^
    symbol:   variable update_button
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:173: error: cannot find symbol
          deleteButton = view.findViewById(R.id.delete_button);
                                               ^
    symbol:   variable delete_button
    location: class id
  C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:174: error: cannot find symbol
          closeButton = view.findViewById(R.id.close_button);
                                              ^
    symbol:   variable close_button
    location: class id
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
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:48)
Caused by: org.gradle.api.internal.tasks.compile.CompilationFailedException: Compilation failed; see the compiler output below.
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:892: error: incompatible types: Object cannot be converted to SyncOperation
                                processSyncOperation(operation)
                                                     ^
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:300: error: incompatible types: Date cannot be converted to Timestamp
        profile.setCreatedAt(new Date());
                             ^
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Note: Recompile with -Xlint:deprecation for details.
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\config\ConfigRepositoryImpl.java:312: error: local variables referenced from a lambda expression must be final or effectively final
                            docRef.set(deviceInfo)
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:212: error: cannot find symbol
            operation.setUserId(getUserId());
                                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:276: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:360: error: cannot find symbol
                                operation.markAsFailed(
                                         ^
  symbol:   method markAsFailed(String,String)
  location: variable operation of type SyncOperation
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:579: error: cannot find symbol
                errorMap.put("code", operation.getError().getCode());
                                                         ^
  symbol:   method getCode()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:580: error: cannot find symbol
                errorMap.put("message", operation.getError().getMessage());
                                                            ^
  symbol:   method getMessage()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:581: error: cannot find symbol
                errorMap.put("timestamp", operation.getError().getTimestamp());
                                                              ^
  symbol:   method getTimestamp()
  location: class String
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:650: error: cannot find symbol
                invalidateCache("userProfile_" + getUserId());
                                                 ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:653: error: cannot find symbol
                invalidateCache("subscriptionStatus_" + getUserId());
                                                        ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:657: error: cannot find symbol
                invalidateCache("addresses_" + getUserId());
                                               ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:661: error: cannot find symbol
                invalidateCache("deliveries_" + getUserId() + "_*");
                                                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:662: error: cannot find symbol
                invalidateCache("delivery_stats_" + getUserId());
                                                    ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:676: error: cannot find symbol
        String docId = getUserId() + "_" + deviceId;
                       ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:701: error: cannot find symbol
                            deviceData.put("userId", getUserId());
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:760: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:788: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:809: error: cannot find symbol
                getUserId(),
                ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:822: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:856: error: cannot find symbol
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:872: error: cannot find symbol
            operation.setStatus(SyncOperation.STATUS_PENDING);
                     ^
  symbol:   method setStatus(String)
  location: variable operation of type Object
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:873: error: cannot find symbol
            operation.setUpdatedAt(new Date());
                     ^
  symbol:   method setUpdatedAt(Date)
  location: variable operation of type Object
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:969: error: cannot find symbol
                            if (operation != null && getUserId().equals(operation.getUserId())) {
                                                     ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1025: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1053: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1095: error: cannot find symbol
                    .whereEqualTo("userId", getUserId())
                                            ^
  symbol:   method getUserId()
  location: class SyncRepositoryImpl
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:62: error: cannot find symbol
            configRepository.getConfigValue("faq_content", "")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:91: error: cannot find symbol
            configRepository.getConfigValue("faq_title", "Knowledge Base")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:111: error: cannot find symbol
            configRepository.getConfigBoolean("track_faq_views", true)
                            ^
  symbol:   method getConfigBoolean(String,boolean)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:116: error: cannot find symbol
                        return configRepository.incrementCounter("faq_view_count");
                                               ^
  symbol:   method incrementCounter(String)
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\faq\FaqViewModel.java:118: error: cannot find symbol
                    return configRepository.noOpCompletable();
                                           ^
  symbol:   method noOpCompletable()
  location: variable configRepository of type ConfigRepository
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\login\AuthViewModel.java:158: error: cannot find symbol
            configRepository.getConfigValue("default_preferences", "{}")
                            ^
  symbol:   method getConfigValue(String,String)
  location: variable configRepository of type ConfigRepository
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:173: error: cannot find symbol
        if (status.isInProgress()) {
                  ^
  symbol:   method isInProgress()
  location: variable status of type SyncStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\MainActivity.java:175: error: cannot find symbol
        } else if (status.isError()) {
                         ^
  symbol:   method isError()
  location: variable status of type SyncStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:228: error: cannot find symbol
        if (status.isInProgress()) {
                  ^
  symbol:   method isInProgress()
  location: variable status of type SyncStatus
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\main\MainViewModel.java:233: error: cannot find symbol
        if (status.isError()) {
                  ^
  symbol:   method isError()
  location: variable status of type SyncStatus
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                    return oldItem.getId().equals(newItem.getId());
                                  ^
  symbol:   method getId()
  location: variable oldItem of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:45: error: cannot find symbol
                    return oldItem.getId().equals(newItem.getId());
                                                         ^
  symbol:   method getId()
  location: variable newItem of type Address
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:132: error: cannot find symbol
                address.isFavorite() ? View.VISIBLE : View.GONE);
                       ^
  symbol:   method isFavorite()
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                                      ^
  symbol:   method isFavorite()
  location: variable address of type Address
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\ui\address\adapters\AddressesAdapter.java:159: error: cannot find symbol
                viewModel.setAddressFavorite(address.getId(), !address.isFavorite());
                                                    ^
  symbol:   method getId()
  location: variable address of type Address
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
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\repository\sync\SyncRepositoryImpl.java:1206: error: cannot find symbol
            SyncRepository syncRepo = (SyncRepository) RepositoryProvider.getRepository();
                                                       ^
  symbol:   variable RepositoryProvider
  location: class SyncWorker
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:205: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing createdAt timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:220: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastLoginAt timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:234: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing privacyPolicyAccepted timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:248: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing termsAccepted timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:336: error: cannot find symbol
            Log.e("UserProfileSerializer", "Invalid profile: userId is required");
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:343: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: email format is invalid");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:353: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: accountStatus is invalid");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:360: error: cannot find symbol
            Log.e("UserProfileSerializer", "Invalid profile: version must be non-negative");
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:370: error: cannot find symbol
                Log.e("UserProfileSerializer", "Invalid profile: defaultTipPercentage must be between 0 and 100");
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:378: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: theme must be 'light', 'dark', or 'system'");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:394: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: subscription status is invalid");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:402: error: cannot find symbol
                    Log.e("UserProfileSerializer", "Invalid profile: subscription expiryDate cannot be before startDate");
                    ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:483: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing startDate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:497: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing expiryDate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:511: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastVerified timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:662: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastUsageUpdate timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
        List<String> deviceIds = (List<String>) map.get("deviceIds");
        ^
  symbol:   class List
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:695: error: cannot find symbol
        List<String> deviceIds = (List<String>) map.get("deviceIds");
                                  ^
  symbol:   class List
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:713: error: cannot find symbol
                Log.e("UserProfileSerializer", "Error parsing lastSyncTime timestamp", e);
                ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\data\serialization\UserProfileSerializer.java:843: error: cannot find symbol
            Log.e("UserProfileSerializer", "Error deserializing profile from JSON", e);
            ^
  symbol:   variable Log
  location: class UserProfileSerializer
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:163: error: cannot find symbol
        View view = inflater.inflate(R.layout.dialog_delivery_detail, null);
                                             ^
  symbol:   variable dialog_delivery_detail
  location: class layout
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:172: error: cannot find symbol
        updateButton = view.findViewById(R.id.update_button);
                                             ^
  symbol:   variable update_button
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:173: error: cannot find symbol
        deleteButton = view.findViewById(R.id.delete_button);
                                             ^
  symbol:   variable delete_button
  location: class id
C:\Users\ReifiedAsh\AndroidStudioProjects\Autogratuity\app\src\main\java\com\autogratuity\dialogs\DeliveryDetailDialog.java:174: error: cannot find symbol
        closeButton = view.findViewById(R.id.close_button);
                                            ^
  symbol:   variable close_button
  location: class id
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
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:48)


BUILD FAILED in 1s
30 actionable tasks: 1 executed, 29 up-to-date
