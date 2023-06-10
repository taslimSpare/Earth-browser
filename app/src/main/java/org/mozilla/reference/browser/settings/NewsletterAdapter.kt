package org.mozilla.reference.browser.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.reference.browser.databinding.NewsletterItemBinding

class NewsletterAdapter : ListAdapter<NewsletterAdapter.Newsletter, NewsletterAdapter.ViewHolder>(NewsletterDiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: NewsletterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        constructor(parent: ViewGroup) : this(
            NewsletterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        fun bind(newsletter: Newsletter) {
            binding.title.text = newsletter.title
            binding.subText.text = newsletter.shortDescription
        }
    }

    class NewsletterDiffUtilCallBack : DiffUtil.ItemCallback<Newsletter>() {
        override fun areItemsTheSame(oldItem: Newsletter, newItem: Newsletter) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Newsletter, newItem: Newsletter) = oldItem == newItem

    }

    data class Newsletter(
        val id: String,
        val title: String,
        val shortDescription: String
    )

}