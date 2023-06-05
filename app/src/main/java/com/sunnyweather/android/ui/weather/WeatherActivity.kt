package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    val viewModel by lazy{ ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer {result ->
            val weather = result.getOrNull()
            if(weather != null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        binding.nowLayout.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                //隐藏键盘
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather){
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局中的数据
        binding.nowLayout.apply {
            placeName.text = viewModel.placeName
            val currentTempText = "${realtime.temperature.toInt()} ℃"
            currentTemp.text = currentTempText
            currentSky.text = getSky(realtime.skycon).info
            val currentPM25Text = "空气指数${realtime.airQuality.aqi.chn.toInt()}"
            currentAQI.text = currentPM25Text
            nowLayout.setBackgroundColor(getSky(realtime.skycon).bg)
        }
        //填充forecast.xml布局中的数据
        var temp = this;
        binding.forecastLayout.apply {
            forecastLayout.removeAllViews()
            val days = daily.skycon.size
            for(i in 0 until days){
                val skycon = daily.skycon[i]
                val temperature = daily.temperature[i]
                val view = LayoutInflater.from(temp).inflate(R.layout.forecast_item, forecastLayout, false)
                val dateInfo = view.findViewById(R.id.dateInfo) as TextView
                val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
                val skyInfo = view.findViewById(R.id.skyInfo) as TextView
                val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateInfo.text = simpleDateFormat.format(skycon.date)
                val sky = getSky(skycon.value)
                skyIcon.setImageResource(sky.icon)
                skyInfo.text = sky.info
                val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
                temperatureInfo.text = tempText
                forecastLayout.addView(view)
            }
        }
        //填充life_index.xml布局中的数据
        binding.lifeIndexLayout.apply {
            val lifeIndex = daily.lifeIndex
            coldRiskText.text = lifeIndex.coldRisk[0].desc
            dressingText.text = lifeIndex.dressing[0].desc
            ultravioletText.text = lifeIndex.ultraviolet[0].desc
            carWashingText.text = lifeIndex.carWashing[0].desc
        }

        binding.weatherLayout.visibility = View.VISIBLE


    }

}