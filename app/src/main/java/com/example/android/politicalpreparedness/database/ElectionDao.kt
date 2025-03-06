package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)

    //TODO: Add select all election query
    @Query("SELECT * from election_table")
    fun getAllElections(): LiveData<List<Election>>

    //TODO: Add select single election query
    @Query("SELECT * from election_table WHERE id = :id")
    fun getElection(id: Int): LiveData<Election?>

    //TODO: Add delete query
    @Query("DELETE from election_table WHERE id = :id")
    suspend fun deleteElection(id: Int)

    //TODO: Add clear query
    @Query("DELETE from election_table")
    suspend fun deleteAllElections()

}