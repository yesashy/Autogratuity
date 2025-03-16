import { db, auth } from '../firebase-config';
import { 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  writeBatch, 
  updateDoc, 
  addDoc,
  serverTimestamp
} from 'firebase/firestore';

/**
 * Service to handle data import processing and resolution for Autogratuity
 */
class ImportDataService {
  /**
   * Main function to fix the import data flow issue
   * @param {string} userId - The current user's ID
   * @returns {Promise<object>} Result of the fix operation
   */
  async fixImportDataFlow(userId) {
    try {
      console.log('Starting import data flow fix...');
      
      // 1. Check for the mapping count vs. actual deliveries
      const userRef = doc(db, 'users', userId);
      const userDoc = await getDoc(userRef);
      
      if (!userDoc.exists()) {
        throw new Error('User document not found');
      }
      
      const userData = userDoc.data();
      const mappingCount = userData.mappingCount || 0;
      
      // 2. Query for actual deliveries
      const deliveriesRef = collection(db, 'deliveries');
      const userDeliveriesQuery = query(deliveriesRef, where('userId', '==', userId));
      const deliveriesDocs = await getDocs(userDeliveriesQuery);
      
      console.log(`Expected deliveries: ${mappingCount}, Actual deliveries: ${deliveriesDocs.size}`);
      
      // 3. First check for GeoJSON imports directly
      const geoJsonImportsRef = collection(db, 'geoJsonImports');
      const userGeoJsonQuery = query(geoJsonImportsRef, where('userId', '==', userId));
      const geoJsonDocs = await getDocs(userGeoJsonQuery);
      
      let pendingRecords = [];
      let pendingAddresses = new Set();
      
      geoJsonDocs.forEach(geoDoc => {
        const geoData = geoDoc.data();
        if (geoData.features) {
          // Transform GeoJSON features to delivery records
          const transformedRecords = geoData.features.map(feature => 
            this.transformGeoJsonToDelivery(feature, userId)
          );
          pendingRecords = [...pendingRecords, ...transformedRecords];
          
          // Track unique addresses
          transformedRecords.forEach(record => {
            if (record.address && record.location) {
              pendingAddresses.add(JSON.stringify({
                address: record.address,
                location: record.location
              }));
            }
          });
        }
      });
      
      // 4. If no GeoJSON imports, check temporary imports collection
      if (pendingRecords.length === 0) {
        const importsRef = collection(db, 'imports');
        const userImportsQuery = query(importsRef, where('userId', '==', userId));
        const importsDocs = await getDocs(userImportsQuery);
        
        importsDocs.forEach(importDoc => {
          const importData = importDoc.data();
          if (importData.status === 'pending' && importData.records) {
            pendingRecords = [...pendingRecords, ...importData.records];
            
            // Track unique addresses
            importData.records.forEach(record => {
              if (record.address && record.location) {
                pendingAddresses.add(JSON.stringify({
                  address: record.address,
                  location: record.location
                }));
              }
            });
          }
        });
      }
      
      console.log(`Found ${pendingRecords.length} records to process`);
      console.log(`Found ${pendingAddresses.size} unique addresses to process`);
      
      // 5. Process any found pending records into proper deliveries
      if (pendingRecords.length > 0) {
        await this.batchCreateDeliveries(pendingRecords, userId);
        await this.batchCreateAddresses(Array.from(pendingAddresses).map(a => JSON.parse(a)), userId);
        console.log(`Created ${pendingRecords.length} delivery records from imports`);
      }
      
      // 6. Update user's UI refresh token to force reload of data
      await updateDoc(userRef, {
        uiRefreshToken: Date.now(),
        lastImportResolved: true,
        deliveryCount: (await getDoc(userRef)).data().deliveryCount + pendingRecords.length || pendingRecords.length
      });
      
      // 7. Create a record of this fix operation
      await addDoc(collection(db, 'importFixLogs'), {
        importId: `geoJson_${Date.now()}`,
        userId,
        fixedCount: pendingRecords.length,
        addressCount: pendingAddresses.size,
        timestamp: serverTimestamp(),
        success: true
      });
      
      console.log('Import data flow fix completed successfully');
      return {
        success: true,
        message: `Fixed data flow for ${pendingRecords.length} imported records`,
        processedRecords: pendingRecords.length,
        processedAddresses: pendingAddresses.size
      };
    } catch (error) {
      console.error('Error fixing import data flow:', error);
      
      // Log the error
      await addDoc(collection(db, 'importFixLogs'), {
        userId,
        error: error.message,
        timestamp: serverTimestamp(),
        success: false
      });
      
      throw error;
    }
  }

  /**
   * Helper method to get import metadata for a user
   * Used to determine if import resolver should be shown
   * @param {string} userId - The user's ID
   * @returns {Promise<Object|null>} Import metadata or null if none
   */
  async getImportMetadata(userId) {
    if (!userId) return null;
    
    try {
      // Check if there are any GeoJSON imports for this user
      const geoJsonImportsRef = collection(db, 'geoJsonImports');
      const userGeoJsonQuery = query(geoJsonImportsRef, where('userId', '==', userId));
      const geoJsonDocs = await getDocs(userGeoJsonQuery);
      
      if (!geoJsonDocs.empty) {
        // Get the most recent import
        let latestImport = null;
        let recordCount = 0;
        
        geoJsonDocs.forEach(doc => {
          const data = doc.data();
          if (data.features && data.features.length > 0) {
            recordCount += data.features.length;
            
            if (!latestImport || data.timestamp > latestImport.timestamp) {
              latestImport = data;
            }
          }
        });
        
        if (latestImport) {
          return {
            id: latestImport.id || 'unknown',
            timestamp: latestImport.timestamp,
            recordCount,
            source: 'geoJson'
          };
        }
      }
      
      // Check if there are any pending imports
      const importsRef = collection(db, 'imports');
      const userImportsQuery = query(importsRef, where('userId', '==', userId));
      const importsDocs = await getDocs(userImportsQuery);
      
      if (!importsDocs.empty) {
        let latestImport = null;
        let recordCount = 0;
        
        importsDocs.forEach(doc => {
          const data = doc.data();
          if (data.records && data.records.length > 0) {
            recordCount += data.records.length;
            
            if (!latestImport || data.timestamp > latestImport.timestamp) {
              latestImport = data;
            }
          }
        });
        
        if (latestImport) {
          return {
            id: latestImport.id || 'unknown',
            timestamp: latestImport.timestamp,
            recordCount,
            source: 'import'
          };
        }
      }
      
      return null;
    } catch (error) {
      console.error('Error getting import metadata:', error);
      return null;
    }
  }

  /**
   * Transform a GeoJSON feature to a delivery record - uses the same approach as the Java GeoJsonImportUtil
   * to maintain consistency between platforms
   * 
   * @param {Object} feature - A GeoJSON feature
   * @param {string} userId - The current user's ID
   * @returns {Object} A properly formatted delivery record
   */
  transformGeoJsonToDelivery(feature, userId) {
    const properties = feature.properties || {};
    const geometry = feature.geometry || { coordinates: [0, 0] };
    
    // Extract coordinates - GeoJSON has [longitude, latitude] format
    const [longitude, latitude] = geometry.coordinates;
    
    // Common property names from various GeoJSON sources
    const possibleAmountProps = ['amount', 'tip', 'payout', 'earnings', 'payment', 'value'];
    const possibleDateProps = ['timestamp', 'date', 'time', 'created', 'createdAt', 'completedAt'];
    const possibleAddressProps = ['address', 'location', 'place', 'destination'];
    const possibleServiceProps = ['service', 'provider', 'app', 'platform', 'company'];
    const possibleCustomerProps = ['customer', 'name', 'recipient', 'client'];
    const possibleNotesProps = ['notes', 'note', 'comment', 'comments', 'description'];
    
    // Extract data with fallbacks for different property naming conventions
    const amount = this.extractNumericProperty(properties, possibleAmountProps) || 0;
    const timestamp = this.extractDateProperty(properties, possibleDateProps) || new Date();
    const address = this.extractStringProperty(properties, possibleAddressProps) || "Unknown Address";
    const service = this.extractStringProperty(properties, possibleServiceProps) || "Unknown";
    const customer = this.extractStringProperty(properties, possibleCustomerProps) || "Unknown";
    const notes = this.extractStringProperty(properties, possibleNotesProps) || "";
    
    // Create delivery record compatible with Java DeliveryData
    return {
      userId,
      amount,
      tip: amount, // Ensure tip field is set
      timestamp,
      address,
      location: {
        latitude,
        longitude
      },
      service,
      customer,
      notes,
      createdAt: new Date(),
      updatedAt: new Date(),
      source: "geoJsonImport",
      status: "completed",
      importedFromGeoJson: true
    };
  }

  /**
   * Helper to extract a numeric property from an object using multiple possible keys
   * @param {Object} obj - Object to extract from
   * @param {Array<string>} possibleKeys - Array of possible property names
   * @returns {number|null} Extracted value or null if not found
   */
  extractNumericProperty(obj, possibleKeys) {
    for (const key of possibleKeys) {
      if (obj[key] !== undefined && obj[key] !== null) {
        const value = parseFloat(obj[key]);
        if (!isNaN(value)) return value;
      }
    }
    return null;
  }

  /**
   * Helper to extract a date property from an object using multiple possible keys
   * @param {Object} obj - Object to extract from
   * @param {Array<string>} possibleKeys - Array of possible property names
   * @returns {Date|null} Extracted date or null if not found
   */
  extractDateProperty(obj, possibleKeys) {
    for (const key of possibleKeys) {
      if (obj[key] !== undefined && obj[key] !== null) {
        const value = new Date(obj[key]);
        if (!isNaN(value.getTime())) return value;
      }
    }
    return null;
  }

  /**
   * Helper to extract a string property from an object using multiple possible keys
   * @param {Object} obj - Object to extract from
   * @param {Array<string>} possibleKeys - Array of possible property names
   * @returns {string|null} Extracted string or null if not found
   */
  extractStringProperty(obj, possibleKeys) {
    for (const key of possibleKeys) {
      if (obj[key] !== undefined && obj[key] !== null && typeof obj[key] === 'string') {
        return obj[key];
      }
    }
    return null;
  }

  /**
   * Create multiple delivery records in batch
   * @param {Array} deliveries - Array of delivery objects
   * @param {string} userId - The current user's ID
   */
  async batchCreateDeliveries(deliveries, userId) {
    const batch = writeBatch(db);
    const deliveriesRef = collection(db, 'deliveries');
    
    deliveries.forEach(delivery => {
      // Ensure userId is set
      delivery.userId = userId;
      
      const newDeliveryRef = doc(deliveriesRef);
      batch.set(newDeliveryRef, delivery);
    });
    
    // Commit the batch
    await batch.commit();
  }

  /**
   * Create multiple address records in batch
   * @param {Array} addresses - Array of address objects
   * @param {string} userId - The current user's ID
   */
  async batchCreateAddresses(addresses, userId) {
    const batch = writeBatch(db);
    const addressesRef = collection(db, 'addresses');
    
    addresses.forEach(addressData => {
      const addressId = `${userId}_${addressData.location.latitude}_${addressData.location.longitude}`;
      const addressDocRef = doc(addressesRef, addressId);
      
      // Create address with fields matching Java Address class
      batch.set(addressDocRef, {
        userId,
        fullAddress: addressData.address,
        normalizedAddress: addressData.address.toLowerCase().trim(),
        geoPoint: {
          latitude: addressData.location.latitude,
          longitude: addressData.location.longitude
        },
        coordinates: `${addressData.location.latitude},${addressData.location.longitude}`,
        deliveryCount: 1,
        averageTip: 0,
        doNotDeliver: false,
        lastDeliveryTimestamp: Date.now(),
        notes: "",
        searchTerms: this.generateSearchTerms(addressData.address),
        createdAt: new Date()
      }, { merge: true });
    });
    
    // Commit the batch
    await batch.commit();
  }

  /**
   * Generate search terms from an address
   * @param {string} address - The address string
   * @returns {Array<string>} List of search terms
   */
  generateSearchTerms(address) {
    if (!address) return [];
    
    const normalizedAddress = address.toLowerCase().trim();
    const terms = normalizedAddress.split(/[\s,]+/);
    return terms.filter(term => term.length > 0);
  }

  /**
   * Helper function to refresh UI data after import
   * @returns {Promise<object>} Processed data for UI display
   */
  async refreshUIAfterImport() {
    const userId = auth.currentUser?.uid;
    if (!userId) {
      console.error('No authenticated user found');
      return { success: false, message: 'Authentication required' };
    }
    
    try {
      // Fetch the latest user data
      const userRef = doc(db, 'users', userId);
      const userDoc = await getDoc(userRef);
      
      if (!userDoc.exists()) {
        return { success: false, message: 'User profile not found' };
      }
      
      // Fetch all user deliveries with fresh query
      const deliveriesRef = collection(db, 'deliveries');
      const userDeliveriesQuery = query(
        deliveriesRef, 
        where('userId', '==', userId)
      );
      
      const deliveriesDocs = await getDocs(userDeliveriesQuery);
      const deliveries = [];
      
      deliveriesDocs.forEach(deliveryDoc => {
        deliveries.push({
          id: deliveryDoc.id,
          ...deliveryDoc.data()
        });
      });
      
      // Process deliveries for UI display
      const processedData = this.processDeliveriesForUI(deliveries);
      
      return {
        success: true,
        data: processedData,
        message: `Loaded ${deliveries.length} deliveries`
      };
    } catch (error) {
      console.error('Error refreshing UI after import:', error);
      return { success: false, message: error.message };
    }
  }

  /**
   * Process raw delivery data into formats needed for UI display
   * @param {Array} deliveries - Array of raw delivery objects
   * @returns {Object} Processed data for UI components
   */
  processDeliveriesForUI(deliveries) {
    // Convert Firebase timestamps to JavaScript Dates if needed
    const processedDeliveries = deliveries.map(delivery => {
      const processed = { ...delivery };
      
      // Handle timestamp conversion if it's a Firebase timestamp
      if (delivery.timestamp && typeof delivery.timestamp.toDate === 'function') {
        processed.timestamp = delivery.timestamp.toDate();
      }
      
      return processed;
    });
    
    // Sort deliveries by date (newest first)
    const sortedDeliveries = [...processedDeliveries].sort((a, b) => {
      const dateA = a.timestamp instanceof Date ? a.timestamp : new Date(a.timestamp);
      const dateB = b.timestamp instanceof Date ? b.timestamp : new Date(b.timestamp);
      return dateB.getTime() - dateA.getTime();
    });
    
    // Process for Today view
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const todayDeliveries = sortedDeliveries.filter(d => {
      const deliveryDate = d.timestamp instanceof Date ? d.timestamp : new Date(d.timestamp);
      return deliveryDate >= today;
    });
    
    // Process for 7 Days view
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    sevenDaysAgo.setHours(0, 0, 0, 0);
    
    const sevenDayDeliveries = sortedDeliveries.filter(d => {
      const deliveryDate = d.timestamp instanceof Date ? d.timestamp : new Date(d.timestamp);
      return deliveryDate >= sevenDaysAgo;
    });
    
    // Process for 30 Days view
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    thirtyDaysAgo.setHours(0, 0, 0, 0);
    
    const thirtyDayDeliveries = sortedDeliveries.filter(d => {
      const deliveryDate = d.timestamp instanceof Date ? d.timestamp : new Date(d.timestamp);
      return deliveryDate >= thirtyDaysAgo;
    });
    
    // Calculate stats for each time period
    return {
      today: this.calculateStats(todayDeliveries),
      sevenDays: this.calculateStats(sevenDayDeliveries),
      thirtyDays: this.calculateStats(thirtyDayDeliveries),
      allDeliveries: sortedDeliveries
    };
  }

  /**
   * Calculate statistics for a set of deliveries
   * @param {Array} deliveries - Array of delivery objects for a time period
   * @returns {Object} Stats for the delivery set
   */
  calculateStats(deliveries) {
    const totalTips = deliveries.reduce((sum, d) => sum + (d.tip || d.amount || 0), 0);
    const averageTip = deliveries.length > 0 ? totalTips / deliveries.length : 0;
    const pendingCount = deliveries.filter(d => d.status === 'pending').length;
    
    return {
      count: deliveries.length,
      totalTips,
      averageTip,
      pendingCount
    };
  }
}

export default new ImportDataService();
