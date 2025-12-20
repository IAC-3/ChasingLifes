package com.example.chasinglifes;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Subscription subscription);
}
