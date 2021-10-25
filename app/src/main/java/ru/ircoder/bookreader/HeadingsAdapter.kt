package ru.ircoder.bookreader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.ircoder.bookreader.databinding.HeadingsItemBinding

class HeadingsAdapter(private val chapters: List<Chapter>, private val onItemClicked: (position: Int) -> Unit): RecyclerView.Adapter<HeadingsAdapter.HeadingsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadingsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HeadingsItemBinding.inflate(inflater)
        return HeadingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeadingsViewHolder, position: Int) {
        holder.bind(chapters[position])
        holder.itemView.setOnClickListener {
            onItemClicked(position)
        }
    }

    override fun getItemCount(): Int = chapters.size

    inner class HeadingsViewHolder(private val binding: HeadingsItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(chapter: Chapter) {
            with(binding) {
                tvItemHeading.text = chapter.heading
            }
        }
    }
}