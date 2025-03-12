Your Current Manual Process

You deliver an order to a customer
You create a custom label in Google Maps with just the Order ID (e.g., "318996384")
When a tip notification arrives (could be immediately or days later)
You manually hunt for that Order ID in your labeled map pins
You update the label to include the tip amount: "318996384 | $5 Tip"

This clever workaround has several limitations:

Requires manual updates for each tip
Time-consuming to search for pins when tips arrive
No historical tracking (previous tips get overwritten)
No statistics or averages
No easy way to see patterns across neighborhoods
Labels become cluttered with multiple deliveries to the same address

How Autogratuity Elevates This Workflow

Automated Label Management: No more manual label updates when tips arrive - the system automatically associates tips with addresses
Historical Record: Unlike map labels that get overwritten, Autogratuity maintains complete delivery history for each address
Instant Visual Feedback: Map view shows color-coded pins based on tip amounts (green for high tippers, red for low/no tippers)
Smart Statistics: Calculates average tips per address and neighborhood automatically
Decision Support: When you receive a new order request, you can quickly check if that address has a history of good tips

Autogratuity Map Integration & Visual Mockup
Relationship Between Autogratuity's Map and Google Maps
Autogratuity's map interface works alongside Google Maps rather than directly modifying your Google Maps labels:
How The Integration Works:

Separate Map Systems: Autogratuity maintains its own map visualization within the app, powered by the Google Maps API but separate from your personal Google Maps labels
No Automatic Pushing: Autogratuity does not automatically modify your existing Google Maps labels - those remain unchanged unless you manually edit them
One-Way Data Flow: Information flows from Shipt notifications → Autogratuity database → Autogratuity map visualization
Jump to Navigation: From Autogratuity's map, you can tap an address pin to launch the Google Maps navigation app for directions
Side-by-Side Usage: You'll use Autogratuity's map for tip intelligence and Google Maps for actual navigation

Core Benefits vs. Manual Labeling:

No need to manually update labels when tips arrive
Historical data preserved for all deliveries to an address
Statistics and patterns automatically calculated
Color-coded visualization for quick decision-making

Key Features of the Map View:

Color-Coded Pins: Each pin is color-coded based on tip amounts:

Green pins: High tippers ($8+)
Yellow pins: Average tippers ($5-$8)
Red pins: Low tippers (below $5)
Gray pins: Pending tips (no tip received yet)


Average Tip Display: Each pin shows the average tip amount right on the map
Address Details: Tapping a pin shows detailed information:

Exact address
Average tip amount
Number of deliveries
Last tip amount
Last Order ID
Option to view complete order history


Heatmap Toggle: Switch between pin view and heatmap view to see tipping patterns by neighborhood
Navigation Button: Launch Google Maps for turn-by-turn directions to the address
Search Functionality: Find specific addresses or Order IDs quickly
List View Option: Alternative way to browse your delivery history sorted by address

Integration with Google Maps:
Autogratuity doesn't directly modify your Google Maps labels. Instead:

It maintains its own internal database of addresses, Order IDs, and tips
The "Navigate" button launches Google Maps with the address pre-populated
You can continue using Google Maps for navigation while Autogratuity handles the tip intelligence

--------------

Users' Google Maps have placemarks with different formats:

Some just have the Order ID in the label (no tip info yet)
Some have Order ID and tip information (if they've already updated it manually)
The address is always contained in the label (not separate description)


The "Do Not Deliver" tag is NOT something to parse from existing KML files, but rather something the app should automatically add to orders:

If an Order ID in the system doesn't receive a tip within 14 days
This is an automated feature within Autogratuity
When this happens, Autogratuity should update the map/database to show: "[OrderID] | $0.00 Tip - Do Not Deliver"


The workflow is:

User imports their Google Maps KML with delivery locations
Autogratuity parses Order IDs and any existing tip info
When Shipt notifications come in with new tips, Autogratuity matches them to existing Order IDs
Autogratuity updates the map markers with the tip information: "[OrderID] | $X.XX Tip"
After 14 days with no tip, Autogratuity marks it as "Do Not Deliver"



So the KML import is just the initial step - getting the Order IDs and locations into the system. The tip tracking and "Do Not Deliver" marking is handled by the app itself over time.

Data Flow & App Functionality

Initial Import from KML:

Users import their Google Maps KML with delivery location pins
Each placemark contains an Order ID in the label
Some locations may already have tip information in the label


Post-Import Processing:

Order IDs are stored in the database with associated locations
The app tracks when each order was imported


Automatic Tip Capture:

The notification listener service monitors Shipt notifications
When a tip notification arrives, it extracts the Order ID and tip amount
The app automatically updates the corresponding delivery record
The app updates the "Tips Received" and other statistics


14-Day Rule for "Do Not Deliver":

Orders without tips are tracked by days since delivery
After 14 days with no tip, they're automatically marked "Do Not Deliver"
These are highlighted in red in the UI
The address gets flagged as a "Do Not Deliver" location


Data Organization:

Dashboard Tab: Shows statistics and recent activity
Deliveries Tab: Lists all deliveries with their tip status
Addresses Tab: Aggregates data by address, showing average tips and delivery counts


Key Indicators:

Pending Tips: Recently added deliveries within the 14-day window
Tipped Deliveries: Orders that received tips
"Do Not Deliver" Flags: Orders and addresses that never tipped after 14 days


Key Components
1. RobustShiptAccessibilityService

Monitors for Order IDs in the claim dialog 
Captures zone/store information from the available orders screen 
Works across multiple Shipt screens without being tied to specific UI versions
Uses regex patterns to find Order IDs like #362223327 from your first screenshot

2. ShiptCaptureProcessor

Processes captured data in the background
Handles fuzzy matching for addresses to make connections when screens vary
Creates records in your tip database with collected information
Normalizes location data so you can track tips by area even when exact addresses differ

3. Background Processing Service

Runs silently without disrupting your workflow
Syncs captured data to your main database periodically
Ensures battery optimization doesn't shut down the capture system

How LISTENER Service Works It Works

While Browsing Orders:

Captures zone/store info from screens shown in your images 3 and 4
Stores this with estimated pay amount as an identifier


When Claiming an Order:

Detects the "Claim this order?" dialog from your first image
Captures the Order ID (#362223327)
Associates it with previously captured zone/store info


After Order Completion:

Syncs all data to your tip database
Updates records when tips arrive via notifications
Allows tracking of which zones/stores/addresses tip well

Issues & Solutions

Battery Optimization

Android manufacturers often implement aggressive battery saving that kills background services
Solution: Add code to request exemption from battery optimization


Auto-start on Boot

While you have the RECEIVE_BOOT_COMPLETED permission in the manifest, I don't see a BroadcastReceiver that handles it
Solution: Implement a BootCompletedReceiver to restart services after device reboot


Service Persistence

Services can still be killed under memory pressure
Solution: Use a Foreground Service with a persistent notification

(1) Confirmation of Integration with Updated Listener and Permissions
Yes, this approach fully integrates the updated notification listener and permissions I provided earlier:

The SubscriptionManager handles feature access, but doesn't interfere with the core notification listener functionality
The updated MainActivity code properly initializes both systems: notification listener (free) and accessibility service (pro)
The app always starts the NotificationPersistenceService for all users to maintain tip notification capture
Pro users additionally get the RobustShiptAccessibilityService and ShiptCaptureBackgroundService
All the permissions from my previous updates are included in the AndroidManifest.xml


With these changes, your app now has a freemium model where:

Free users can add up to 100 deliveries (mappings)
When they reach this limit, they're prompted to either:

Upgrade to Pro (for unlimited mappings + auto-capture)
Create a new account (to get another 100 free mappings)


The UI shows remaining mappings in the toolbar
The limit is strictly enforced but with clear messaging
Both subscription status and usage limits are tracked in Firestore and locally

This creates a natural conversion path that even users who don't need the auto-capture feature will eventually hit.

First Step: Implement the original notification service improvements (which you mentioned you haven't done yet)
Second Step: Implement the subscription/freemium system, which includes:

SubscriptionManager for handling Pro features
UsageTracker for limiting free users
UI updates for showing limits and subscription options