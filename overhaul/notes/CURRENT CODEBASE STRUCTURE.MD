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
│   │   │   ├── res\
│   │   │   │   ├── anim\
│   │   │   │   │   ├── rotate_360.xml
│   │   │   │   ├── drawable\
│   │   │   │   │   ├── ic_add.xml
│   │   │   │   │   ├── ic_arrows_left_right.xml
│   │   │   │   │   ├── ic_check.xml
│   │   │   │   │   ├── ic_dashboard.xml
│   │   │   │   │   ├── ic_edit.xml
│   │   │   │   │   ├── ic_home.xml
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   ├── ic_logout.xml
│   │   │   │   │   ├── ic_map.xml
│   │   │   │   │   ├── ic_menu.xml
│   │   │   │   │   ├── ic_package.xml
│   │   │   │   │   ├── ic_pro.xml
│   │   │   │   │   ├── ic_refresh.xml
│   │   │   │   │   ├── ic_shipt_logo.xml
│   │   │   │   │   ├── ic_swap_app.xml
│   │   │   │   │   ├── ic_swap_apps.xml
│   │   │   │   │   ├── ic_upload.xml
│   │   │   │   │   ├── ic_verified_badge.xml
│   │   │   │   ├── layout\
│   │   │   │   │   ├── activity_faq.xml
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_main_drawer.xml
│   │   │   │   │   ├── activity_pro_subscribe.xml
│   │   │   │   │   ├── activity_webapp.xml
│   │   │   │   │   ├── dialog_add_delivery.xml
│   │   │   │   │   ├── dialog_export_data.xml
│   │   │   │   │   ├── dialog_register.xml
│   │   │   │   │   ├── example_livedata_statcard.xml
│   │   │   │   │   ├── fragment_addresses.xml
│   │   │   │   │   ├── fragment_bulk_upload.xml
│   │   │   │   │   ├── fragment_dashboard.xml
│   │   │   │   │   ├── fragment_deliveries.xml
│   │   │   │   │   ├── fragment_map.xml
│   │   │   │   │   ├── item_activity.xml
│   │   │   │   │   ├── item_address.xml
│   │   │   │   │   ├── item_delivery.xml
│   │   │   │   │   ├── item_import_error.xml
│   │   │   │   │   ├── item_import_result.xml
│   │   │   │   │   ├── item_stat_card.xml
│   │   │   │   │   ├── item_tipping_area.xml
│   │   │   │   │   ├── nav_header.xml
│   │   │   │   ├── menu\
│   │   │   │   │   ├── drawer_menu.xml
│   │   │   │   │   ├── main_menu.xml
│   │   │   │   ├── mipmap-anydpi\
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   ├── ic_launcher_round.xml
│   │   │   │   ├── mipmap-anydpi-v26\
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   ├── ic_launcher_round.xml
│   │   │   │   ├── mipmap-hdpi\
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-mdpi\
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xhdpi\
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxhdpi\
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxxhdpi\
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── values\
│   │   │   │   │   ├── attrs.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   ├── values-night\
│   │   │   │   │   ├── themes.xml
│   │   │   │   └── xml\
│   │   │   │       ├── backup_rules.xml
│   │   │   │       ├── data_extraction_rules.xml
│   │   │   │       └── shipt_accessibility_service_config.xml
│   │   └── test\
│   │       └── java\
│   │           └── com\
│   │               └── autogratuity\
│   │                   ├── ExampleUnitTest.java
│   │                   └── data\
│   │                       └── repository\
│   │                           ├── FirestoreRepositoryTest.java
│   │                           └── RepositoryTestSuite.java
├── build.gradle
├── codebase-visualizer.py
├── gradle\
│   ├── libs.versions.toml
│   ├── wrapper\
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── overhaul\
│   ├── Untitled-1.json
│   ├── firestore config\
│   │   ├── new_firebase_structure.json
│   │   ├── new_firebase_structure.jsonl
│   │   ├── reference.json
│   ├── notes\
│   │   ├── .obsidian\
│   │   │   ├── app.json
│   │   │   ├── appearance.json
│   │   │   ├── core-plugins.json
│   │   │   ├── graph.json
│   │   │   ├── workspace.json
│   │   ├── ADAPTER_MIGRATION_ANALYSIS.md
│   │   ├── Autogratuity Architectural Flow Visualization.md
│   │   ├── Autogratuity Architectural Flow.md
│   │   ├── Autogratuity Build Error Fixes.md
│   │   ├── Build Error Log.md
│   │   ├── CODEBASE_AUDIT.md
│   │   ├── CONSOLIDATED_CHECKLIST.md
│   │   ├── CONSOLIDATED_CHECKLIST_V2.md
│   │   ├── IMMEDIATE_ACTION_PLAN.md
│   │   ├── MASTER_NOTES.md
│   │   ├── PHASE2_IMPLEMENTATION_PLAN.md
│   │   ├── Today.md
│   │   ├── UI Overhaul - Critical.md
│   │   ├── Untitled 1.canvas
│   │   ├── Untitled 1.md
│   │   ├── Untitled 2.canvas
│   │   ├── Untitled 2.md
│   │   ├── Untitled-1 1.json
│   │   ├── Untitled-1.json
│   │   ├── Untitled.canvas
│   │   ├── Untitled.md
│   │   ├── address_viewmodel_implementation.md
│   │   ├── build resolve checklist.md
│   │   ├── codebase_structure.md
│   │   ├── f\
│   │   └── implementation_status.md
├── settings.gradle
├── src\
│   ├── components\
│   │   ├── AdminPanel.js
│   │   ├── DeliveryStats.js
│   │   ├── Home.js
│   │   ├── ImportResolver.js
│   ├── firebase-config.js
│   ├── utils\
│   │   ├── AdminBypass.js
│   │   ├── FirestoreProStatusFix.js
│   │   └── ImportDataService.js
└── sync.gradle