package com.udacity.asteroidradar.api

import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.Call
import retrofit2.http.GET

interface ApiImage {
    @GET("planetary/apod?api_key=${Constants.nasaApiKey}")
    fun getImage(): Call<PictureOfDay>
}