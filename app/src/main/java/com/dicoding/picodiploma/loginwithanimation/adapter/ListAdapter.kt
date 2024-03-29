package com.dicoding.picodiploma.loginwithanimation.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.databinding.ItemLayoutBinding
import com.dicoding.picodiploma.loginwithanimation.view.detail.DetailActivity

class ListAdapter:
PagingDataAdapter<ListStoryItem, ListAdapter.StoryListViewHolder>(DIFF_CALLBACK) {

    inner class StoryListViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(storyItem: ListStoryItem) {
            val context = binding.root.context

            binding.tvName.text = storyItem.name
            binding.tvDescription.text = storyItem.description

            Glide.with(context)
                .load(storyItem.photoUrl)
                .fitCenter()
                .into(binding.ivStoryImage)

            binding.itemLayout.setOnClickListener {
                val intent = Intent(binding.root.context, DetailActivity::class.java).apply {
                    putExtra("storyItem", storyItem)
                }

                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    binding.root.context as Activity,
                    Pair(binding.ivStoryImage, "story_image"),
                    Pair(binding.tvName, "name"),
                    Pair(binding.tvDescription, "description")
                )

                binding.root.context.startActivity(intent, optionsCompat.toBundle())
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryListViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryListViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

