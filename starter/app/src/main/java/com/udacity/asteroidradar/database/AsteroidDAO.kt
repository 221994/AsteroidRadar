package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.udacity.asteroidradar.Asteroid
import org.jetbrains.annotations.NotNull
import retrofit2.http.GET
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Dao
interface AsteroidDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAsteroid(list: List<Asteroid>)

    @Query("SELECT * from AsteroidTable ORDER BY closeApproachDate")
    fun getAllAsteroid(): LiveData<List<Asteroid>>

    @Query("SELECT*FROM AsteroidTable where closeApproachDate =:currentDate ")
    fun getAllAsteroidToday(currentDate: String): LiveData<List<Asteroid>>

    @Query("SELECT*FROM AsteroidTable where closeApproachDate BETWEEN:from AND:to ORDER BY closeApproachDate")
    fun getAllAsteroid7Days(from: String, to: String): LiveData<List<Asteroid>>


}