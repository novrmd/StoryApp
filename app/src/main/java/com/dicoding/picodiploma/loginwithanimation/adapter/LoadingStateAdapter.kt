package com.dicoding.picodiploma.loginwithanimation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.databinding.ItemLoadingBinding

class LoadingStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingStateViewHolder {
        return LoadingStateViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingStateViewHolder private constructor(
        private val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorMsg.isVisible = loadState is LoadState.Error

            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): LoadingStateViewHolder {
                val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return LoadingStateViewHolder(binding).apply {
                    binding.retryButton.setOnClickListener { retry.invoke() }
                }
            }
        }
    }
}
