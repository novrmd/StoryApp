package com.dicoding.picodiploma.loginwithanimation.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailBinding

@Suppress("DEPRECATION")
class DetailActivity : AppCompatActivity() {

    private lateinit var storyItem: ListStoryItem
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.detail_page_title)
            setDisplayHomeAsUpEnabled(true)
        }

        storyItem = intent.getParcelableExtra("storyItem")!!

        setData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setData() = with(binding) {
        Glide.with(this@DetailActivity)
            .load(storyItem.photoUrl)
            .fitCenter()
            .into(ivProfilePhoto)

        tvName.text = storyItem.name
        tvDescription.text = storyItem.description
    }
}


