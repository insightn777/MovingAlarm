package com.ksc.movingalarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ksc.movingalarm.data.Record
import com.ksc.movingalarm.data.RecordViewModel
import com.ksc.movingalarm.databinding.ItemRecordBinding
import kotlinx.android.synthetic.main.activity_report.*

class ReportActivity : AppCompatActivity() {

    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val adapter = RecordListAdapter() {
            Toast.makeText(this@ReportActivity,"$it",Toast.LENGTH_SHORT).show()
        }
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this@ReportActivity)

        recordViewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        recordViewModel.allrecords.observe(this, Observer { records ->
            records?.let { adapter.setRecords(it) }
        })
    }
}