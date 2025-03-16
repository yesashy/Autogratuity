// Import the Firebase functions we need
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

// Your Firebase configuration
// In a real app, this would typically be in a .env file or environment variable
const firebaseConfig = {
  // Replace with your actual Firebase config, or have it loaded from environment
  apiKey: "YOUR_API_KEY",
  authDomain: "autogratuity.firebaseapp.com",
  projectId: "autogratuity",
  storageBucket: "autogratuity.appspot.com",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID",
  measurementId: "YOUR_MEASUREMENT_ID"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Get Firebase Auth instance
const auth = getAuth(app);

// Get Firestore instance
const db = getFirestore(app);

// Export the Firebase services
export { app, auth, db };
