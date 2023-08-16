package com.example.weatherforecast.fragments

import DialogManager
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.example.weatherforecast.API.APIConstance
import com.example.weatherforecast.API.APIService
import com.example.weatherforecast.API.WeatherData
import com.example.weatherforecast.MainViewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.adapters.VpAdapter
import com.example.weatherforecast.constance.Constance
import com.example.weatherforecast.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import android.provider.Settings
import android.widget.EditText

class MainFragment : Fragment() {
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val tList = mutableListOf<String>()
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private lateinit var apiService: APIService
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        tList.add(getString(R.string.title_hoursFragment))
        tList.add(getString(R.string.title_daysFragment))
        initVpView()
        initRetrofit()
        updateCurrentCard()

        binding.btRefresh.setOnClickListener {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            checkLocation()
        }
        binding.btSearch.setOnClickListener {
            DialogManager.searchByLocationDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> getWeatherForecast(it1) }
                }
            })
        }
    }

    private fun initVpView() = with(binding) {
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) {
            tab, position -> tab.text = tList[position]
        }.attach()
    }

    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                getWeatherForecast("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun initRetrofit() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(APIConstance.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        apiService = retrofit.create(APIService::class.java)
    }

    private fun getWeatherForecast(city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val forecast = apiService.getWeatherForecast(
                APIConstance.KEY,
                city,
                APIConstance.DAYS
            )
            requireActivity().runOnUiThread {
                parseWeatherData(forecast)
            }
        }
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDaysData(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    private fun parseDaysData(mainObject: JSONObject): List<WeatherData> {
        val list = ArrayList<WeatherData>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherData(
                mainObject.getJSONObject("location").getString("name"),
                day.getString("date"),
                day.getJSONObject("day")
                    .getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c")
                    .toFloat().toInt().toString()
                        + Constance.DEGREE_C,
                day.getJSONObject("day").getString("mintemp_c")
                    .toFloat().toInt().toString()
                        + Constance.DEGREE_C,
                day.getJSONObject("day")
                    .getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, day: WeatherData) {
        val item = WeatherData(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c")
                .toFloat().toInt().toString()
                    + Constance.DEGREE_C,
            "",
            "",
            "",
            day.hours
        )
        model.liveDataCurrent.value = item
        Log.d("Tag", "${item.hours}")
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            tvLocation.text = it.city
            tvCurrentTempC.text = it.currentTemp.ifEmpty { "${it.minTemp} / ${it.maxTemp}" }
            tvCurrentCondition.text = it.condition
            tvDate.text = it.time
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {

        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}