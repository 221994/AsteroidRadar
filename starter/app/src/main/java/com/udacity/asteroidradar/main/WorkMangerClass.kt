package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.ApiAsteroids
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.MyRoomDataBase
import com.udacity.asteroidradar.main.MainFragment.Companion.currentDate
import com.udacity.asteroidradar.main.MainFragment.Companion.dateAfter7days
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Runnable


class WorkMangerClass(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        ContextCompat.getMainExecutor(applicationContext).execute(object : Runnable {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun run() {
                val retroFitAsteroid = Retrofit.Builder().baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val api: ApiAsteroids =
                    retroFitAsteroid.create(ApiAsteroids::class.java)
                api.getAsteroid(
                    currentDate(),
                    dateAfter7days(),
                    Constants.nasaApiKey
                )
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            try {
                                val json = JSONObject(response.body()?.string())
                                val array = parseAsteroidsJsonResult(json)
                                val cr = CoroutineScope(Dispatchers.IO)
                                cr.launch {
                                    MyRoomDataBase.getDatabase(applicationContext!!.applicationContext)
                                        .asteroidDao()
                                        .insertAsteroid(array)
                                }

                            } catch (e: Exception) {
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        }
                    })

            }

        })
        return Result.success()
    }

}