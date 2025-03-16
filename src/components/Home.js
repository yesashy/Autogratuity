import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, RefreshControl } from 'react-native';
import { auth } from '../firebase-config';
import ImportDataService from '../utils/ImportDataService';
import ImportResolver from './ImportResolver';
import DeliveryStats from './DeliveryStats';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

/**
 * Home Screen Component for Autogratuity
 * Displays tips and deliveries statistics with tabs for different date ranges
 */
const Home = () => {
  const [activeTab, setActiveTab] = useState('today');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [statsData, setStatsData] = useState({
    today: { count: 0, totalTips: 0, averageTip: 0, pendingCount: 0 },
    sevenDays: { count: 0, totalTips: 0, averageTip: 0, pendingCount: 0 },
    thirtyDays: { count: 0, totalTips: 0, averageTip: 0, pendingCount: 0 },
    allDeliveries: []
  });
  const [showImportResolver, setShowImportResolver] = useState(false);
  const [importMetadata, setImportMetadata] = useState(null);

  // Check for import issues and load data on mount
  useEffect(() => {
    loadDeliveryData();
    checkForImportIssues();
  }, []);

  // Check if we have imports that haven't been properly processed
  const checkForImportIssues = async () => {
    // This is a simplification - in a real app we would query Firestore to check
    // if there are imports that need processing
    
    // For demo purposes, if we have 0 deliveries but we know imports happened, show resolver
    if (statsData.allDeliveries.length === 0) {
      try {
        // Check for import metadata
        const result = await ImportDataService.getImportMetadata(auth.currentUser?.uid);
        if (result && result.recordCount > 0) {
          setImportMetadata(result);
          setShowImportResolver(true);
        }
      } catch (error) {
        console.error('Error checking for imports:', error);
      }
    }
  };

  // Load all delivery data
  const loadDeliveryData = async () => {
    setIsRefreshing(true);
    try {
      const result = await ImportDataService.refreshUIAfterImport();
      if (result.success) {
        setStatsData(result.data);
        
        // If we got data and the resolver is showing, hide it
        if (result.data.allDeliveries.length > 0 && showImportResolver) {
          setShowImportResolver(false);
        }
        // If we have no data but know imports happened, show resolver
        else if (result.data.allDeliveries.length === 0) {
          checkForImportIssues();
        }
      }
    } catch (error) {
      console.error('Error loading delivery data:', error);
    } finally {
      setIsRefreshing(false);
    }
  };

  // Handle the refresh gesture
  const onRefresh = () => {
    loadDeliveryData();
  };

  // Handler for when the import resolver completes its work
  const handleImportFixComplete = (result) => {
    if (result.fixed && result.refreshed) {
      // If fix was successful and we got data back, update our state
      if (result.data) {
        setStatsData(result.data);
      }
      
      // Refresh data just to be sure
      loadDeliveryData();
      
      // Hide the resolver
      setShowImportResolver(false);
    }
  };

  // Get current tab data
  const getTabData = () => {
    switch (activeTab) {
      case 'today':
        return statsData.today;
      case 'sevenDays':
        return statsData.sevenDays;
      case 'thirtyDays':
        return statsData.thirtyDays;
      default:
        return statsData.today;
    }
  };

  return (
    <ScrollView 
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
      }
    >
      {/* Import Resolver - Only shown when needed */}
      {showImportResolver && (
        <ImportResolver 
          userId={auth.currentUser?.uid} 
          onComplete={handleImportFixComplete}
          recordCount={importMetadata?.recordCount || 117}
        />
      )}

      {/* Date Range Tabs */}
      <View style={styles.tabsContainer}>
        <TouchableOpacity 
          style={[styles.tab, activeTab === 'today' && styles.activeTab]}
          onPress={() => setActiveTab('today')}
        >
          <Text style={[styles.tabText, activeTab === 'today' && styles.activeTabText]}>
            TODAY
          </Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={[styles.tab, activeTab === 'sevenDays' && styles.activeTab]}
          onPress={() => setActiveTab('sevenDays')}
        >
          <Text style={[styles.tabText, activeTab === 'sevenDays' && styles.activeTabText]}>
            7 DAYS
          </Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={[styles.tab, activeTab === 'thirtyDays' && styles.activeTab]}
          onPress={() => setActiveTab('thirtyDays')}
        >
          <Text style={[styles.tabText, activeTab === 'thirtyDays' && styles.activeTabText]}>
            30 DAYS
          </Text>
        </TouchableOpacity>
      </View>

      {/* Stats Display */}
      <DeliveryStats data={getTabData()} />

      {/* Add Delivery Button */}
      <TouchableOpacity style={styles.addButton}>
        <Icon name="plus" size={20} color="#fff" />
        <Text style={styles.addButtonText}>Add delivery</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#222',
  },
  tabsContainer: {
    flexDirection: 'row',
    backgroundColor: '#333',
    marginTop: 16,
    marginHorizontal: 16,
    borderRadius: 8,
    overflow: 'hidden',
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
  },
  activeTab: {
    backgroundColor: '#444',
  },
  tabText: {
    color: '#aaa',
    fontWeight: 'bold',
    fontSize: 14,
  },
  activeTabText: {
    color: '#fff',
  },
  addButton: {
    backgroundColor: '#4CAF50',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 14,
    borderRadius: 50,
    margin: 16,
  },
  addButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    marginLeft: 8,
  },
});

export default Home;
