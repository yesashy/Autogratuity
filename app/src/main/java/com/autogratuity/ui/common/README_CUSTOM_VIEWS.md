# Custom View Components

This directory contains enhanced custom view components that integrate directly with the repository pattern and MVVM architecture.

## StatCard and LiveDataStatCard

StatCard is a custom view for displaying statistics with a label and a value. This view has been enhanced to work with the new architecture pattern:

- **StatCard**: Base component with direct DeliveryStats integration
- **LiveDataStatCard**: Enhanced version with LiveData binding support
- **StatCardExtensions**: Utility methods for binding StatCards to LiveData

## Usage Examples

### Using StatCard with DeliveryStats

```java
// Get a reference to your StatCard
StatCard statCard = findViewById(R.id.my_stat_card);

// Set the label and value
statCard.setStatLabel("Total Tips");

// Set the value directly from DeliveryStats
DeliveryStats stats = /* get from repository or ViewModel */;
statCard.setStatFromDeliveryStats(stats, StatCard.StatType.TOTAL_TIPS);
```

### Using StatCardExtensions with existing StatCards

```java
// In a Fragment or Activity
StatCard statCard = findViewById(R.id.my_stat_card);

// Bind to LiveData from ViewModel
Observer<?> observer = StatCardExtensions.bindStatCardToDeliveryStats(
    statCard, 
    viewModel.getStatsLiveData(), 
    getViewLifecycleOwner(), 
    StatCard.StatType.TOTAL_TIPS);

// Store the observer for cleanup if needed
observers.add(observer);
```

### Using LiveDataStatCard directly

```java
// In a Fragment or Activity
LiveDataStatCard statCard = findViewById(R.id.my_live_stat_card);

// Set the lifecycle owner
statCard.setLifecycleOwner(getViewLifecycleOwner());

// Bind to LiveData
statCard.bindToTotalTips(viewModel.getStatsLiveData());
```

### XML Layout Example

```xml
<com.autogratuity.ui.common.LiveDataStatCard
    android:id="@+id/stat_total_tips"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:statLabel="Total Tips" />
```

## Available StatTypes

The following stat types are available for use with `setStatFromDeliveryStats()` and `bindStatCardToDeliveryStats()`:

- `TOTAL_TIPS`: Total tips received
- `AVERAGE_TIP`: Average tip amount
- `HIGHEST_TIP`: Highest tip amount
- `DELIVERY_COUNT`: Number of deliveries
- `PENDING_COUNT`: Number of pending deliveries
- `TIP_RATE`: Percentage of deliveries with tips

## LiveDataStatCard Methods

LiveDataStatCard provides additional methods for binding directly to LiveData:

- `bindToTotalTips(LiveData<DeliveryStats> liveData)`
- `bindToAverageTip(LiveData<DeliveryStats> liveData)`
- `bindToHighestTip(LiveData<DeliveryStats> liveData)`
- `bindToDeliveryCount(LiveData<DeliveryStats> liveData)`
- `bindToPendingCount(LiveData<DeliveryStats> liveData)`
- `bindToTipRate(LiveData<DeliveryStats> liveData)`
- `bindTo(LiveData<T> liveData, Function<T, String> formatter)` - Generic binding with custom formatter

## Lifecycle Management

Both approaches handle lifecycle management properly:

- **StatCardExtensions**: Observers are bound to the provided LifecycleOwner
- **LiveDataStatCard**: Observers are automatically cleaned up when the view is detached from the window

## Architecture Integration

These components are designed to work with the repository pattern:

1. Repositories provide data access
2. ViewModels expose data as LiveData
3. UI components observe and display the data

This ensures a clean separation of concerns and proper architectural layers.
