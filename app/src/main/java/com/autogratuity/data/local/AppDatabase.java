package com.autogratuity.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Room database for local storage and caching
 */
@Database(
    entities = {
        DeliveryEntity.class,
        AddressEntity.class,
        PendingOperationEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class, ListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "autogratuity-db";
    
    private static volatile AppDatabase instance;
    
    /**
     * Get the DeliveryDao for delivery operations
     */
    public abstract DeliveryDao deliveryDao();
    
    /**
     * Get the AddressDao for address operations
     */
    public abstract AddressDao addressDao();
    
    /**
     * Get the PendingOperationDao for pending operation management
     */
    public abstract PendingOperationDao pendingOperationDao();
    
    /**
     * Get the singleton instance of the database
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
