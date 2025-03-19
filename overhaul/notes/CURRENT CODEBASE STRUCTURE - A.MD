Autogratuity\
├── .gitattributes
├── .gitignore
├── README.md
├── app\
│   ├── .gitignore
│   ├── build.gradle
│   ├── google-services.json
│   ├── proguard-rules.pro
│   ├── src\
│   │   ├── androidTest\
│   │   │   ├── java\
│   │   │   │   └── com\
│   │   │   │       └── autogratuity\
│   │   │   │           └── ExampleInstrumentedTest.java
│   │   ├── main\
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── DO NOT DELETE codebase-visualizer.py
│   │   │   ├── assets\
│   │   │   │   ├── faq.html
│   │   │   ├── java\
│   │   │   │   ├── com\
│   │   │   │   │   └── autogratuity\
│   │   │   │   │       ├── AutogratuityApp.java
│   │   │   │   │       ├── FaqActivity.java
│   │   │   │   │       ├── LoginActivity.java
│   │   │   │   │       ├── MainActivity.java
│   │   │   │   │       ├── ProSubscribeActivity.java
│   │   │   │   │       ├── ShiptNotificationListenerService.java
│   │   │   │   │       ├── WebAppActivity.java
│   │   │   │   │       ├── data\
│   │   │   │   │       │   ├── local\
│   │   │   │   │       │   │   ├── AddressDao.java
│   │   │   │   │       │   │   ├── AddressEntity.java
│   │   │   │   │       │   │   ├── AppDatabase.java
│   │   │   │   │       │   │   ├── Converters.java
│   │   │   │   │       │   │   ├── DeliveryDao.java
│   │   │   │   │       │   │   ├── DeliveryEntity.java
│   │   │   │   │       │   │   ├── JsonSerializer.java
│   │   │   │   │       │   │   ├── ListConverter.java
│   │   │   │   │       │   │   ├── PendingOperationDao.java
│   │   │   │   │       │   │   ├── PendingOperationEntity.java
│   │   │   │   │       │   │   ├── PreferenceManager.java
│   │   │   │   │       │   ├── model\
│   │   │   │   │       │   │   ├── Address.java
│   │   │   │   │       │   │   ├── Amounts.java
│   │   │   │   │       │   │   ├── AppConfig.java
│   │   │   │   │       │   │   ├── Coordinates.java
│   │   │   │   │       │   │   ├── Delivery.java
│   │   │   │   │       │   │   ├── DeliveryStats.java
│   │   │   │   │       │   │   ├── ErrorInfo.java
│   │   │   │   │       │   │   ├── Metadata.java
│   │   │   │   │       │   │   ├── Reference.java
│   │   │   │   │       │   │   ├── Status.java
│   │   │   │   │       │   │   ├── SubscriptionStatus.java
│   │   │   │   │       │   │   ├── SyncOperation.java
│   │   │   │   │       │   │   ├── SyncStatus.java
│   │   │   │   │       │   │   ├── Times.java
│   │   │   │   │       │   │   ├── UserProfile.java
│   │   │   │   │       │   ├── repository\
│   │   │   │   │       │   │   ├── address\
│   │   │   │   │       │   │   │   ├── AddressRepository.java
│   │   │   │   │       │   │   │   ├── AddressRepositoryImpl.java
│   │   │   │   │       │   │   ├── config\
│   │   │   │   │       │   │   │   ├── ConfigRepository.java
│   │   │   │   │       │   │   │   ├── ConfigRepositoryImpl.java
│   │   │   │   │       │   │   ├── core\
│   │   │   │   │       │   │   │   ├── DataRepository.java
│   │   │   │   │       │   │   │   ├── FirestoreRepository.java
│   │   │   │   │       │   │   │   ├── RepositoryProvider.java
│   │   │   │   │       │   │   ├── delivery\
│   │   │   │   │       │   │   │   ├── DeliveryRepository.java
│   │   │   │   │       │   │   │   ├── DeliveryRepositoryImpl.java
│   │   │   │   │       │   │   ├── preference\
│   │   │   │   │       │   │   │   ├── PreferenceRepository.java
│   │   │   │   │       │   │   │   ├── PreferenceRepositoryImpl.java
│   │   │   │   │       │   │   ├── subscription\
│   │   │   │   │       │   │   │   ├── SubscriptionRepository.java
│   │   │   │   │       │   │   │   ├── SubscriptionRepositoryImpl.java
│   │   │   │   │       │   │   ├── sync\
│   │   │   │   │       │   │   │   ├── SyncRepository.java
│   │   │   │   │       │   │   │   ├── SyncRepositoryImpl.java
│   │   │   │   │       │   │   ├── utils\
│   │   │   │   │       │   ├── security\
│   │   │   │   │       │   │   ├── AuthenticationManager.java
│   │   │   │   │       │   │   ├── EncryptionUtils.java
│   │   │   │   │       │   │   ├── ValidationUtils.java
│   │   │   │   │       │   ├── serialization\
│   │   │   │   │       │   │   ├── AddressSerializer.java
│   │   │   │   │       │   │   ├── DeliverySerializer.java
│   │   │   │   │       │   │   ├── SubscriptionSerializer.java
│   │   │   │   │       │   │   ├── UserProfileSerializer.java
│   │   │   │   │       │   ├── util\
│   │   │   │   │       │   │   └── NetworkMonitor.java
│   │   │   │   │       ├── dialogs\
│   │   │   │   │       │   ├── AddDeliveryDialog.java
│   │   │   │   │       │   ├── DeliveryDetailDialog.java
│   │   │   │   │       ├── receivers\
│   │   │   │   │       │   ├── BootCompletedReceiver.java
│   │   │   │   │       │   ├── CaptureProcessReceiver.java
│   │   │   │   │       ├── services\
│   │   │   │   │       │   ├── DoNotDeliverService.java
│   │   │   │   │       │   ├── NotificationPersistenceService.java
│   │   │   │   │       │   ├── RobustShiptAccessibilityService.java
│   │   │   │   │       │   ├── ShiptCaptureBackgroundService.java
│   │   │   │   │       ├── ui\
│   │   │   │   │       │   ├── address\
│   │   │   │   │       │   │   ├── AddressViewModel.java
│   │   │   │   │       │   │   ├── AddressesFragment.java
│   │   │   │   │       │   │   ├── adapters\
│   │   │   │   │       │   │   │   └── AddressesAdapter.java
│   │   │   │   │       │   ├── common\
│   │   │   │   │       │   │   ├── BaseViewModel.java
│   │   │   │   │       │   │   ├── LiveDataStatCard.java
│   │   │   │   │       │   │   ├── README_CUSTOM_VIEWS.md
│   │   │   │   │       │   │   ├── RepositoryViewModelFactory.java
│   │   │   │   │       │   │   ├── StatCardExtensions.java
│   │   │   │   │       │   ├── dashboard\
│   │   │   │   │       │   │   ├── DashboardFragment.java
│   │   │   │   │       │   │   ├── DashboardViewModel.java
│   │   │   │   │       │   ├── delivery\
│   │   │   │   │       │   │   ├── DeliveriesFragment.java
│   │   │   │   │       │   │   ├── DeliveryViewModel.java
│   │   │   │   │       │   │   ├── adapters\
│   │   │   │   │       │   │   │   └── DeliveriesAdapter.java
│   │   │   │   │       │   ├── dialog\
│   │   │   │   │       │   │   ├── DeliveryDialogViewModel.java
│   │   │   │   │       │   │   ├── DeliveryDialogViewModelFactory.java
│   │   │   │   │       │   ├── faq\
│   │   │   │   │       │   │   ├── FaqViewModel.java
│   │   │   │   │       │   ├── import\
│   │   │   │   │       │   │   ├── BulkUploadFragment.java
│   │   │   │   │       │   │   ├── BulkUploadViewModel.java
│   │   │   │   │       │   ├── login\
│   │   │   │   │       │   │   ├── AuthViewModel.java
│   │   │   │   │       │   ├── main\
│   │   │   │   │       │   │   ├── MainViewModel.java
│   │   │   │   │       │   ├── map\
│   │   │   │   │       │   │   ├── MapFragment.java
│   │   │   │   │       │   │   ├── MapViewModel.java
│   │   │   │   │       │   ├── subscription\
│   │   │   │   │       │   │   ├── SubscriptionViewModel.java
│   │   │   │   │       │   ├── webapp\
│   │   │   │   │       │   │   └── WebAppViewModel.java
│   │   │   │   │       ├── utils\
│   │   │   │   │       │   ├── DataValidationSystem.java
│   │   │   │   │       │   ├── ExportManager.java
│   │   │   │   │       │   ├── GeoJsonImportUtil.java
│   │   │   │   │       │   ├── ImportManager.java
│   │   │   │   │       │   ├── KmlImportUtil.java
│   │   │   │   │       │   ├── MapManager.java
│   │   │   │   │       │   ├── ShiptCaptureProcessor.java
│   │   │   │   │       │   ├── SubscriptionManager.java
│   │   │   │   │       │   ├── UsageTracker.java
│   │   │   │   │       ├── views\
│   │   │   │   │       │   ├── StatCard.java
│   │   │   │   │       └── workers\
│   │   │   │   │           └── SyncWorker.java