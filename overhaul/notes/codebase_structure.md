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
│   │   │   │   │       ├── adapters\
│   │   │   │   │       │   ├── AddressesAdapter.java
│   │   │   │   │       │   ├── DeliveriesAdapter.java
│   │   │   │   │       │   ├── ViewPagerAdapter.java
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
│   │   │   │   │       │   │   ├── AppConfig.java
│   │   │   │   │       │   │   ├── Delivery.java
│   │   │   │   │       │   │   ├── DeliveryStats.java
│   │   │   │   │       │   │   ├── SubscriptionStatus.java
│   │   │   │   │       │   │   ├── SyncOperation.java
│   │   │   │   │       │   │   ├── SyncStatus.java
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
│   │   │   │   │       ├── models\
│   │   │   │   │       │   ├── Address.java
│   │   │   │   │       │   ├── Coordinates.java
│   │   │   │   │       │   ├── Delivery.java
│   │   │   │   │       │   ├── DeliveryData.java
│   │   │   │   │       │   ├── FirestoreModel.java
│   │   │   │   │       │   ├── ImportVerification.java
│   │   │   │   │       │   ├── TipData.java
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
│   │   │   │   │       │   │   ├── RepositoryViewModelFactory.java
│   │   │   │   │       │   ├── dashboard\
│   │   │   │   │       │   │   ├── DashboardFragment.java
│   │   │   │   │       │   │   ├── DashboardViewModel.java
│   │   │   │   │       │   ├── delivery\
│   │   │   │   │       │   │   ├── DeliveriesFragment.java
│   │   │   │   │       │   │   ├── DeliveryViewModel.java
│   │   │   │   │       │   │   ├── adapters\
│   │   │   │   │       │   │   │   └── DeliveriesAdapter.java
│   │   │   │   │       │   ├── import\
│   │   │   │   │       │   │   ├── BulkUploadFragment.java
│   │   │   │   │       │   │   ├── BulkUploadViewModel.java
│   │   │   │   │       │   ├── importing\
│   │   │   │   │       │   ├── main\
│   │   │   │   │       │   │   ├── MainViewModel.java
│   │   │   │   │       │   ├── map\
│   │   │   │   │       │   │   ├── MapFragment.java
│   │   │   │   │       │   │   ├── MapViewModel.java
│   │   │   │   │       │   ├── subscription\
│   │   │   │   │       │   │   └── SubscriptionViewModel.java
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
│   ├── CODEBASE STRUCTURE DISASTER.txt
│   ├── MASTER_NOTES.md
│   ├── Necessary-Changes.md
│   ├── PHASE2_IMPLEMENTATION_PLAN.md
│   ├── UI Overhaul - Critical.md
│   ├── deprecated_files\
│   │   ├── Notes from ChatGPT\
│   │   │   ├── Autogratuity Architectural Overhaul - Enhanced Master Notes.md
│   │   │   ├── Note from ChatGPT.md
│   │   ├── UI Overhaul - Critical.md.temp
│   │   ├── flutter fix.md
│   │   ├── react_native_backup\
│   │   │   ├── components\
│   │   │   │   ├── AdminPanel.js
│   │   │   ├── firebase-config.js
│   │   │   └── utils\
│   ├── firebase_dependency_update.md
│   ├── firestore config\
│   │   ├── new_firebase_structure.json
│   │   ├── new_firebase_structure.jsonl
│   │   ├── reference.json
│   ├── flutter_cleanup_instructions.md
│   ├── flutter_cleanup_summary.md
│   ├── implementation_status.md
│   ├── notes\
│   │   ├── ADAPTER_MIGRATION_ANALYSIS.md
│   │   ├── CODEBASE_AUDIT.md
│   │   ├── CONSOLIDATED_CHECKLIST.md
│   │   ├── IMMEDIATE_ACTION_PLAN.md
│   │   ├── Today.md
│   │   └── address_viewmodel_implementation.md
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