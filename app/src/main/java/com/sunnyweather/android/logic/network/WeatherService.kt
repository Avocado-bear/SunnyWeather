//package com.sunnyweather.android.logic.network
//
//import com.sunnyweather.android.SunnyWeatherApplication
//import retrofit2.Call
//import retrofit2.http.GET
//import retrofit2.http.Path
//
//interface WeatherService {
//
//    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lag}/realtime.json")
//    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>
//}