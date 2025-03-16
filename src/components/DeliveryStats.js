import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

/**
 * Component to display delivery statistics
 * Shows tips received, pending tips, average tip, and delivery count
 */
const DeliveryStats = ({ data }) => {
  // Format currency values with 2 decimal places
  const formatCurrency = (value) => {
    return `$${value.toFixed(2)}`;
  };

  return (
    <View style={styles.container}>
      <View style={styles.statsRow}>
        <View style={styles.statsBox}>
          <Text style={styles.statsLabel}>Tips Received</Text>
          <Text style={styles.statsValue}>{formatCurrency(data.totalTips || 0)}</Text>
        </View>
        
        <View style={styles.statsBox}>
          <Text style={styles.statsLabel}>Pending Tips</Text>
          <Text style={styles.statsValue}>{data.pendingCount || 0}</Text>
        </View>
      </View>
      
      <View style={styles.statsRow}>
        <View style={styles.statsBox}>
          <Text style={styles.statsLabel}>Average Tip</Text>
          <Text style={styles.statsValue}>{formatCurrency(data.averageTip || 0)}</Text>
        </View>
        
        <View style={styles.statsBox}>
          <Text style={styles.statsLabel}>Deliveries</Text>
          <Text style={styles.statsValue}>{data.count || 0}</Text>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#333',
    borderRadius: 8,
    padding: 16,
    margin: 16,
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  statsBox: {
    flex: 1,
  },
  statsLabel: {
    color: '#aaa',
    fontSize: 14,
    marginBottom: 4,
  },
  statsValue: {
    color: '#fff',
    fontSize: 24,
    fontWeight: 'bold',
  }
});

export default DeliveryStats;
