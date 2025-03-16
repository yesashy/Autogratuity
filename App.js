import React, { useEffect, useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { StatusBar, View, Text, StyleSheet } from 'react-native';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './src/firebase-config';

// Import screens
import Home from './src/components/Home';
// Other screens would be imported here

const Stack = createStackNavigator();

const App = () => {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    // Set up auth state listener
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      if (initializing) setInitializing(false);
    });

    // Clean up subscription
    return unsubscribe;
  }, [initializing]);

  // Show loading state
  if (initializing) {
    return (
      <View style={styles.container}>
        <Text style={styles.loadingText}>Loading...</Text>
      </View>
    );
  }

  return (
    <>
      <StatusBar barStyle="light-content" backgroundColor="#3f0e40" />
      <NavigationContainer>
        <Stack.Navigator
          screenOptions={{
            headerStyle: {
              backgroundColor: '#3f0e40',
            },
            headerTintColor: '#fff',
            headerTitleStyle: {
              fontWeight: 'bold',
            },
          }}
        >
          <Stack.Screen
            name="Home"
            component={Home}
            options={{
              title: 'Autogratuity',
            }}
          />
          {/* Add other screens here */}
        </Stack.Navigator>
      </NavigationContainer>
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#222',
  },
  loadingText: {
    color: '#fff',
    fontSize: 18,
  },
});

export default App;
