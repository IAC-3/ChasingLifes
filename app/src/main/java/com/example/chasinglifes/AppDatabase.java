package com.example.chasinglifes;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Aggiorno la versione a 8 e aggiungo la nuova tabella Subscription
@Database(entities = {Placeholder.class, Session.class, User.class, Patient.class, Subscription.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SessionDao sessionDao();
    public abstract UserDao userDao();
    public abstract PatientDao patientDao();
    public abstract SubscriptionDao subscriptionDao(); // Aggiungo il nuovo DAO

    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "chasing_lifes_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
