package com.sunnyweather.android.logic.network

import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {
    //通过指定泛型的方式调用create方法，这样就不用每次都写一遍WeatherService::class.java了
    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()

    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()

    //同步请求的版本。因为用的是Retrofit的execute同步请求，execute并不是suspend函数，所以自己也不需要声明为suspend
    fun getRealtimeWeather2(lng:String, lat:String):RealtimeResponse{
        var execute = weatherService.getRealtimeWeather(lng, lat).execute()

        return execute.body()?:throw RuntimeException("response body is null")
    }
    //对比上面的泛型调用方式
    private val placeService = ServiceCreator.create(PlaceService::class.java)

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}