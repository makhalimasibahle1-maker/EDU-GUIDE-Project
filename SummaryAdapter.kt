package com.example.videoconfrence.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoconfrence.R

class SummaryAdapter(
    private var summaries: List<LectureSummaryEntity>,
    private val onItemClick: (LectureSummaryEntity) -> Unit
) : RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvLecturer: TextView = itemView.findViewById(R.id.tvLecturer)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvSummary: TextView = itemView.findViewById(R.id.tvSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lecture_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val item = summaries[position]
        holder.tvTitle.text = item.title
        holder.tvLecturer.text = "Lecturer: ${item.lecturerName}"
        holder.tvDate.text = "Date: ${item.createdAt}"
        holder.tvSummary.text = item.summaryNotes

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = summaries.size

    fun updateData(newSummaries: List<LectureSummaryEntity>) {
        summaries = newSummaries
        notifyDataSetChanged()
    }
}