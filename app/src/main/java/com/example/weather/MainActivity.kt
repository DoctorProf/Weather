package com.example.weather

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response


data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val country: String,
)

data class Current(
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val humidity: Int,
    val cloud: Int,
)

data class Condition(
    val text: String,
)

class MainActivity : AppCompatActivity() {

    interface WeatherApi {
        @GET("current.json")
        fun getCurrentWeather(
            @Query("key") apiKey: String,
            @Query("q") location: String,
            @Query("lang") lang : String
        ): Call<WeatherResponse>
    }

    private lateinit var editText: EditText
    private lateinit var button: Button

    private lateinit var textViewCountry: TextView
    private lateinit var textViewCity: TextView
    private lateinit var imageViewDay : ImageView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewCondition: TextView
    private lateinit var textViewHumidity: TextView
    private lateinit var textViewCloud: TextView


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
                    textViewCountry.setText(weatherResponse?.location?.country)
                    textViewCity.setText(weatherResponse?.location?.name)
                    imageViewDay.setImageResource(if(weatherResponse?.current?.is_day == 1) R.drawable.sun else R.drawable.moon )
                    textViewTemperature.setText("${weatherResponse?.current?.temp_c} °C")
                    textViewCondition.setText(weatherResponse?.current?.condition?.text)
                    textViewHumidity.setText("Влажность ${weatherResponse?.current?.humidity} %")
                    textViewCloud.setText("Облака ${weatherResponse?.current?.cloud} %")
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editText = findViewById(R.id.editText)
        button = findViewById(R.id.button)
        textViewCountry = findViewById(R.id.textViewCountry)
        textViewCity = findViewById(R.id.textViewCity)
        imageViewDay = findViewById(R.id.imageViewDay)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        textViewCondition = findViewById(R.id.textViewCondition)
        textViewHumidity = findViewById(R.id.textViewHumidity)
        textViewCloud = findViewById(R.id.textViewCloud)

        button.setOnClickListener {
            val city = editText.text.toString()

           if(city.isNotEmpty()) {
                sendRequest(city)
           }
        }
    }
}