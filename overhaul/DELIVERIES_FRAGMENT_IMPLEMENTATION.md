# DeliveriesFragment Implementation Guide

This document provides a comprehensive guide for implementing the DeliveriesFragment using the repository pattern established in our architectural overhaul. The implementation should follow the same approach as the successfully migrated AddressesFragment, focusing on proper loading states, error handling, and reactive updates.

## Overview

The DeliveriesFragment will display a list of deliveries for the current user, with real-time updates when data changes. The implementation will:

1. Replace direct Firestore calls with repository methods
2. Add proper loading states and error handling
3. Implement reactive updates using RxJava
4. Ensure proper lifecycle management of subscriptions

## Implementation Steps

### 1. Update Fragment Layout

Ensure the fragment layout includes:

```xml
<!-- fragment_deliveries.xml -->
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- RecyclerView for deliveries -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView_deliveries"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:padding="8dp"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
        tools:listitem="@layout/item_delivery" />

    <!-- Loading indicator -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <!-- Error state view -->
    <LinearLayout
        android:id="@+id/layout_error"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="16dp"
        android:visibility="gone">

        <TextView
            android:id="@+id/textView_error"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="8dp"
            android:gravity="center"
            android:textAppearance="@style/TextAppearance.AppCompat.Medium"
            tools:text="Error message" />

        <Button
            android:id="@+id/button_retry"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Retry" />
    </LinearLayout>

    <!-- Empty state view -->
    <LinearLayout
        android:id="@+id/layout_empty"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="16dp"
        android:visibility="gone">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="8dp"
            android:gravity="center"
            android:text="No deliveries found"
            android:textAppearance="@style/TextAppearance.AppCompat.Medium" />

        <Button
            android:id="@+id/button_add_delivery"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Add Delivery" />
    </LinearLayout>

    <!-- FAB for adding new delivery -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_add_delivery"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="Add Delivery"
        app:srcCompat="@drawable/ic_add" />
</FrameLayout>
```

### 2. Update DeliveriesFragment

Refactor the DeliveriesFragment to use the repository pattern:

```java
public class DeliveriesFragment extends Fragment {
    private static final String TAG = "DeliveriesFragment";

    // UI components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View errorLayout;
    private TextView errorTextView;
    private Button retryButton;
    private View emptyLayout;
    private FloatingActionButton fabAddDelivery;

    // Adapter
    private DeliveriesAdapter adapter;

    // Repository
    private DataRepository repository;

    // RxJava disposables
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deliveries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerView_deliveries);
        progressBar = view.findViewById(R.id.progressBar);
        errorLayout = view.findViewById(R.id.layout_error);
        errorTextView = view.findViewById(R.id.textView_error);
        retryButton = view.findViewById(R.id.button_retry);
        emptyLayout = view.findViewById(R.id.layout_empty);
        fabAddDelivery = view.findViewById(R.id.fab_add_delivery);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DeliveriesAdapter();
        recyclerView.setAdapter(adapter);

        // Get repository instance
        repository = RepositoryProvider.getRepository();

        // Set up button click listeners
        retryButton.setOnClickListener(v -> loadDeliveries());
        view.findViewById(R.id.button_add_delivery).setOnClickListener(v -> navigateToAddDelivery());
        fabAddDelivery.setOnClickListener(v -> navigateToAddDelivery());

        // Initial data load
        loadDeliveries();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start observing deliveries when the fragment is visible
        observeDeliveries();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Clear all subscriptions to prevent memory leaks
        disposables.clear();
    }

    private void loadDeliveries() {
        showLoading();

        disposables.add(
            repository.getDeliveries(50, null) // Load the most recent 50 deliveries
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleDeliveriesLoaded,
                    this::handleError
                )
        );
    }

    private void observeDeliveries() {
        disposables.add(
            repository.observeDeliveries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleDeliveriesLoaded,
                    this::handleError
                )
        );
    }

    private void handleDeliveriesLoaded(List<Delivery> deliveries) {
        hideLoading();
        
        if (deliveries.isEmpty()) {
            showEmptyState();
        } else {
            showContent();
            adapter.setDeliveries(deliveries);
        }
    }

    private void handleError(Throwable error) {
        hideLoading();
        showError(error.getMessage());
        Log.e(TAG, "Error loading deliveries", error);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showError(String message) {
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        errorTextView.setText(message != null ? message : "Unknown error occurred");
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void navigateToAddDelivery() {
        // Navigate to add delivery screen
        // This could be a new activity or fragment
        // Example:
        // startActivity(new Intent(requireContext(), AddDeliveryActivity.class));
    }
}
```

### 3. Implement DeliveriesAdapter

Create or update the DeliveriesAdapter to work with the new Delivery model:

```java
public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.DeliveryViewHolder> {
    private List<Delivery> deliveries = new ArrayList<>();
    private OnDeliveryClickListener listener;

    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
    }

    public void setOnDeliveryClickListener(OnDeliveryClickListener listener) {
        this.listener = listener;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        holder.bind(deliveries.get(position));
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final TextView addressTextView;
        private final TextView dateTextView;
        private final TextView tipTextView;
        private final TextView statusTextView;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.textView_address);
            dateTextView = itemView.findViewById(R.id.textView_date);
            tipTextView = itemView.findViewById(R.id.textView_tip);
            statusTextView = itemView.findViewById(R.id.textView_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeliveryClick(deliveries.get(position));
                }
            });
        }

        public void bind(Delivery delivery) {
            // Set address information
            if (delivery.getAddress() != null) {
                addressTextView.setText(delivery.getAddress().getFormattedAddress());
            } else {
                addressTextView.setText("Unknown Address");
            }

            // Format and set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
            String formattedDate = delivery.getDeliveryTime() != null 
                ? dateFormat.format(delivery.getDeliveryTime()) 
                : "Unknown Date";
            dateTextView.setText(formattedDate);

            // Format and set tip amount
            String tipAmount = String.format(Locale.getDefault(), "$%.2f", delivery.getTipAmount());
            tipTextView.setText(tipAmount);

            // Set delivery status with appropriate color
            statusTextView.setText(delivery.getStatus());
            int statusColor;
            switch (delivery.getStatus()) {
                case "completed":
                    statusColor = Color.GREEN;
                    break;
                case "pending":
                    statusColor = Color.BLUE;
                    break;
                case "canceled":
                    statusColor = Color.RED;
                    break;
                default:
                    statusColor = Color.GRAY;
                    break;
            }
            statusTextView.setTextColor(statusColor);
        }
    }
}
```

### 4. Create Item Layout

Create the item_delivery.xml layout for the RecyclerView:

```xml
<!-- item_delivery.xml -->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="4dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/textView_address"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textAppearance="@style/TextAppearance.AppCompat.Medium"
            android:textStyle="bold"
            tools:text="123 Main St, Anytown, USA" />

        <TextView
            android:id="@+id/textView_date"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textAppearance="@style/TextAppearance.AppCompat.Small"
            tools:text="Mar 15, 2025 2:30 PM" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Tip: "
                android:textStyle="bold" />

            <TextView
                android:id="@+id/textView_tip"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                tools:text="$5.00" />

            <TextView
                android:id="@+id/textView_status"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textStyle="italic"
                tools:text="Completed"
                tools:textColor="@android:color/holo_green_dark" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### 5. Add Delivery Detail Functionality

Implement navigation to delivery details when a delivery is clicked:

```java
// In DeliveriesFragment.java, inside onViewCreated()
adapter.setOnDeliveryClickListener(delivery -> {
    // Navigate to delivery details
    Bundle args = new Bundle();
    args.putString("deliveryId", delivery.getDeliveryId());
    Navigation.findNavController(view).navigate(R.id.action_deliveriesFragment_to_deliveryDetailFragment, args);
});
```

### 6. Add Filtering Capability

Implement filtering options for deliveries:

```java
// In DeliveriesFragment.java
private void setupFilterMenu(Toolbar toolbar) {
    toolbar.inflateMenu(R.menu.menu_deliveries);
    toolbar.setOnMenuItemClickListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.action_filter_all) {
            loadAllDeliveries();
            return true;
        } else if (itemId == R.id.action_filter_recent) {
            loadRecentDeliveries();
            return true;
        } else if (itemId == R.id.action_filter_completed) {
            loadCompletedDeliveries();
            return true;
        } else if (itemId == R.id.action_filter_pending) {
            loadPendingDeliveries();
            return true;
        }