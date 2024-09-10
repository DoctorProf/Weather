package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response


data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: ForeCast
)

data class ForeCast(
    val forecastday: List<Predict>
)

data class Predict(
    val day: Day,
    val astro: Astro
)

data class Day(
    val maxtemp_c: String,
    val mintemp_c: String,
    val daily_chance_of_rain: Int,
    val daily_chance_of_snow: Int
)

data class Astro(
    val sunrise: String,
    val moonrise: String
)

data class Location(
    val name: String,
    val country: String,
    val localtime : String
)

data class Current(
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val humidity: Int,
    val cloud: Int
)

data class Condition(
    val text: String
)

class MainActivity : AppCompatActivity() {

    interface WeatherApi {
        @GET("forecast.json")
        fun getCurrentWeather(
            @Query("key") apiKey: String,
            @Query("q") location: String,
            @Query("lang") lang : String
        ): Call<WeatherResponse>
    }

    private lateinit var binding: ActivityMainBinding

    fun sendRequest(city : String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        val call = weatherApi.getCurrentWeather("a85313ae4112409bb16212359240409", city, "RU")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    with(binding) {
                        textViewTime.text = weatherResponse?.location?.localtime
                        textViewCountry.text = weatherResponse?.location?.country
                        textViewCity.text = weatherResponse?.location?.name
                        imageViewDay.setImageResource(if(weatherResponse?.current?.is_day == 1) R.drawable.sun else R.drawable.moon )
                        textViewTemperature.text = "${weatherResponse?.current?.temp_c} °C"
                        textViewCondition.text = weatherResponse?.current?.condition?.text
                        textViewHumidity.text = "${getString(R.string.humidity)} ${weatherResponse?.current?.humidity} %"
                        textViewCloud.text = "${getString(R.string.cloud)} ${weatherResponse?.current?.cloud} %"
                        textViewSunrise.text = weatherResponse?.forecast?.forecastday?.getOrNull(0)?.astro?.sunrise
                        textViewMoonrise.text = weatherResponse?.forecast?.forecastday?.getOrNull(0)?.astro?.moonrise
                        textViewMinTemp.text = "${weatherResponse?.forecast?.forecastday?.getOrNull(0)?.day?.mintemp_c} °C"
                        textViewMaxTemp.text = "${weatherResponse?.forecast?.forecastday?.getOrNull(0)?.day?.maxtemp_c} °C"
                        textViewRainChance.text = "${weatherResponse?.forecast?.forecastday?.getOrNull(0)?.day?.daily_chance_of_rain} %"
                    }
                } else {
                    println("Request failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                println("Request failed: ${t.message}")
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Glide.with(this)
            .load(R.drawable.rain)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(500)))
            .into(binding.imageViewRain)
        binding.button.setOnClickListener {
            val city = binding.editText.text.toString()

           if(city.isNotEmpty()) {
                sendRequest(city)
           }
        }
        binding.imageButton.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("URL", "https://github.com/DoctorProf")
            startActivity(intent)
        }
    }
}