package com.udacity.asteroidradar.main

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.*
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.adapters.RvAdapterTest
import com.udacity.asteroidradar.api.ApiImage
import com.udacity.asteroidradar.database.MyRoomDataBase
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainFragment() : Fragment() {
    lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startWorkManager()
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).client(client)
            .addConverterFactory(MoshiConverterFactory.create()).build()
        val apiImage: ApiImage = retrofit.create(ApiImage::class.java)
        apiImage.getImage().enqueue(object : Callback<PictureOfDay> {
            override fun onResponse(call: Call<PictureOfDay>, response: Response<PictureOfDay>) {
                if (response.body()?.mediaType.toString() == "image") {
                    val result = response.body()?.url.toString()
                    val title = response.body()?.title.toString()
                    binding.activityMainImageOfTheDay.contentDescription = title
                    Picasso.get().load(result).into(binding.activityMainImageOfTheDay)
                    binding.textView.text = title

                }
            }

            override fun onFailure(call: Call<PictureOfDay>, t: Throwable) {
            }
        })
        getTodayAsteroidsFromDB().observe(requireActivity(), object : Observer<List<Asteroid>> {
            override fun onChanged(t: List<Asteroid>?) {
                val adapter = RvAdapterTest(object : RvAdapterTest.OnItemClick {
                    override fun onItemClick(positionClick: Int) {
                        val result = t!![positionClick]
                        val action = MainFragmentDirections.actionShowDetail(result)
                        findNavController().navigate(action)
                    }
                })
                adapter.submitList(t)
                binding.asteroidRecycler.layoutManager = LinearLayoutManager(context)
                binding.asteroidRecycler.adapter = adapter
            }
        })
    }

    private fun getAllAsteroidsFromDB(): LiveData<List<Asteroid>> {
        val db = MyRoomDataBase.getDatabase(requireContext().applicationContext)
        return db.asteroidDao().getAllAsteroid()
    }

    private fun getTodayAsteroidsFromDB(): LiveData<List<Asteroid>> {
        val db = MyRoomDataBase.getDatabase(requireContext().applicationContext)
        return db.asteroidDao().getAllAsteroidToday(currentDate())
    }

    private fun get7DaysAsteroidsFromDB(): LiveData<List<Asteroid>> {
        val db = MyRoomDataBase.getDatabase(requireContext().applicationContext)
        return db.asteroidDao().getAllAsteroid7Days(currentDate(), dateAfter7days())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_week_asteroids -> {
                get7DaysAsteroidsFromDB().observe(
                    requireActivity(),
                    object : Observer<List<Asteroid>> {
                        override fun onChanged(t: List<Asteroid>?) {
                            val adapter = RvAdapterTest(object : RvAdapterTest.OnItemClick {
                                override fun onItemClick(positionClick: Int) {
                                    val result = t!![positionClick]
                                    val action = MainFragmentDirections.actionShowDetail(result)
                                    findNavController().navigate(action)
                                }
                            })
                            adapter.submitList(t)
                            binding.asteroidRecycler.layoutManager = LinearLayoutManager(context)
                            binding.asteroidRecycler.adapter = adapter

                        }
                    })
            }
            R.id.show_today_asteroids -> {
                getTodayAsteroidsFromDB().observe(
                    requireActivity(),
                    object : Observer<List<Asteroid>> {
                        override fun onChanged(t: List<Asteroid>?) {
                            val adapter = RvAdapterTest(object : RvAdapterTest.OnItemClick {
                                override fun onItemClick(positionClick: Int) {
                                    val result = t!![positionClick]
                                    val action = MainFragmentDirections.actionShowDetail(result)
                                    findNavController().navigate(action)
                                }
                            })
                            adapter.submitList(t)
                            binding.asteroidRecycler.layoutManager = LinearLayoutManager(context)
                            binding.asteroidRecycler.adapter = adapter


                        }
                    })
            }
            R.id.show_saved_asteroids -> {
                getAllAsteroidsFromDB().observe(
                    requireActivity(),
                    object : Observer<List<Asteroid>> {
                        override fun onChanged(t: List<Asteroid>?) {
                            val adapter = RvAdapterTest(object : RvAdapterTest.OnItemClick {
                                override fun onItemClick(positionClick: Int) {
                                    val result = t!![positionClick]
                                    val action = MainFragmentDirections.actionShowDetail(result)
                                    findNavController().navigate(action)
                                }
                            })
                            adapter.submitList(t)
                            binding.asteroidRecycler.layoutManager = LinearLayoutManager(context)
                            binding.asteroidRecycler.adapter = adapter

                        }
                    })
            }

        }
        return true
    }

    companion object {
        fun currentDate(): String {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            return simpleDateFormat.format(calendar.time)
        }

        fun dateAfter7days(): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            return simpleDateFormat.format(calendar.time)
        }
    }

    private fun startWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(
                WorkMangerClass::class.java, 1, TimeUnit.DAYS
            ).setConstraints(constraints)
                .build()
        WorkManager.getInstance(requireActivity()).enqueue(periodicWorkRequest)
    }

}
