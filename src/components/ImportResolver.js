import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import ImportDataService from '../utils/ImportDataService';

/**
 * Component to handle resolving import data flow issues
 * Can be shown when an import is detected but no data appears in the UI
 */
const ImportResolver = ({ onComplete, userId, recordCount = 117 }) => {
  const [isFixing, setIsFixing] = useState(false);
  const [status, setStatus] = useState('');
  const [result, setResult] = useState(null);

  const handleFixImport = async () => {
    if (!userId) {
      Alert.alert('Error', 'You must be logged in to fix imports');
      return;
    }

    setIsFixing(true);
    setStatus('Analyzing import data...');

    try {
      // Step 1: Run the fix import function
      setStatus('Processing imported data...');
      const fixResult = await ImportDataService.fixImportDataFlow(userId);
      setResult(fixResult);

      // Step 2: Refresh the UI with the new data
      setStatus('Refreshing display...');
      const refreshResult = await ImportDataService.refreshUIAfterImport();

      setStatus('Import fix completed!');
      
      // Notify parent component that the fix is complete
      if (onComplete) {
        onComplete({
          fixed: fixResult.success,
          processed: fixResult.processedRecords,
          refreshed: refreshResult.success,
          data: refreshResult.data
        });
      }
    } catch (error) {
      console.error('Error fixing import:', error);
      setStatus(`Error: ${error.message}`);
      Alert.alert('Import Fix Failed', 
        'There was a problem resolving the import. Please try again or contact support.');
    } finally {
      setIsFixing(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Import Data Issue Detected</Text>
      
      <Text style={styles.description}>
        Your imported data ({recordCount} records) isn't showing up in the app.
        This utility can fix the connection between your imported data and the app display.
      </Text>

      {result && (
        <View style={styles.resultContainer}>
          <Text style={styles.resultText}>
            {result.success 
              ? `Successfully processed ${result.processedRecords} records!` 
              : `Error: ${result.message}`}
          </Text>
        </View>
      )}

      {isFixing ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#4CAF50" />
          <Text style={styles.statusText}>{status}</Text>
        </View>
      ) : (
        <TouchableOpacity 
          style={styles.fixButton}
          onPress={handleFixImport}
          disabled={isFixing}
        >
          <Text style={styles.fixButtonText}>Fix Import Data</Text>
        </TouchableOpacity>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#333',
    borderRadius: 8,
    padding: 16,
    margin: 16,
    borderWidth: 1,
    borderColor: '#555',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 12,
  },
  description: {
    fontSize: 14,
    color: '#ddd',
    marginBottom: 16,
    lineHeight: 20,
  },
  fixButton: {
    backgroundColor: '#4CAF50',
    padding: 12,
    borderRadius: 4,
    alignItems: 'center',
  },
  fixButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 16,
  },
  loadingContainer: {
    alignItems: 'center',
    padding: 12,
  },
  statusText: {
    color: '#fff',
    marginTop: 8,
  },
  resultContainer: {
    backgroundColor: '#444',
    padding: 12,
    borderRadius: 4,
    marginBottom: 16,
  },
  resultText: {
    color: '#fff',
  }
});

export default ImportResolver;
