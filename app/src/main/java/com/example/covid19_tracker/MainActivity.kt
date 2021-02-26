package com.example.covid19_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var statWiseAdapter: StateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
           list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.list_header,list,false))
        fetchResults()
    }

    private fun fetchResults() {
        GlobalScope.launch(Dispatchers.Main) {
            val response: Response = withContext(Dispatchers.IO){Client.api.execute()}
            if (response.isSuccessful){
          val data: com.example.covid19_tracker.Response? = Gson().fromJson(response.body?.string(),com.example.covid19_tracker.Response::class.java)
                launch(Dispatchers.Main) {
                    bindCombinedData(data?.statewise!![0])
                    bindStateWiseData(data.statewise.subList(1,data.statewise.size))
                }
            }
        }
    }

    private fun bindStateWiseData(subList: List<StatewiseItem>) {
                 statWiseAdapter = StateAdapter(subList)
        list.adapter = statWiseAdapter


    }

    private fun bindCombinedData(data: StatewiseItem) {
            val lastUpdated = data.lastupdatedtime
        val  simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        lastUpdatedTv.text = "Last Updated \n ${getTimeAgo(simpleDateFormat.parse(lastUpdated))}"


        confirmedTv.text = data.confirmed
        recoveredTv.text = data.recovered
        activeTv.text = data.active
        deceasedTv.text = data.deaths

    }
    fun getTimeAgo(past:Date):String{
        val now = Date()
        val seconds:Long = TimeUnit.MILLISECONDS.toSeconds(now.time-past.time)
        val minutes:Long = TimeUnit.MILLISECONDS.toMinutes(now.time-past.time)
        val hours:Long = TimeUnit.MILLISECONDS.toHours(now.time-past.time)



        return when{
            seconds < 60 ->{
                "Few seconds ago"
            }
            minutes < 60 ->{
                "$minutes minutes ago"
            }
            hours < 24 -> {
                "$hours hour ${minutes % 60} min ago "
            }
            else ->{
                SimpleDateFormat("dd/mm/yy,hh:mm:a").format(past).toString()
            }

        }
    }
}