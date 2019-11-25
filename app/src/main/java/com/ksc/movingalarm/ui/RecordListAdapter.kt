package com.ksc.movingalarm.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ksc.movingalarm.R
import com.ksc.movingalarm.data.Record
import com.ksc.movingalarm.databinding.ItemRecordBinding

class RecordListAdapter internal constructor(
    private val clickListener: (record : Record) -> Unit) : RecyclerView.Adapter<RecordListAdapter.RecordViewHolder>() {

    class RecordViewHolder(val binding: ItemRecordBinding): RecyclerView.ViewHolder(binding.root)

    private var records = emptyList<Record>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        val viewHolder =
            RecordViewHolder(
                ItemRecordBinding.bind(view)
            )
        view.setOnClickListener {
            clickListener.invoke(records[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.binding.alarmRecord = records[position]
    }

    override fun getItemCount() = records.size

    internal fun setRecords(records: List<Record>) {
        this.records = records
        notifyDataSetChanged()
    }

}


/*
class RecorAdapter(private val items :List<Record>, private val clickListener: (record : Record) -> Unit) : RecyclerView.Adapter<RecordLAdapter.RecordViewHolder>() {

    class RecordViewHolder(val binding: ItemRecordBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        // can this way either
//        val binding : ItemRecordBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_record, parent, false)
//        val viewHolder = RecordViewHolder(binding)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        val viewHolder = RecordViewHolder(ItemRecordBinding.bind(view))
        view.setOnClickListener {
            clickListener.invoke(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.binding.alarmRecord = items[position]
// google
//        holder.binding.setVariable(BR.alarmRecord, items[position]);
//        holder.binding.executePendingBindings();
    }
}
*/