package com.dicoding.picodiploma.loginwithanimation.data.retrofit

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dicoding.picodiploma.loginwithanimation.data.StoryDao
import com.dicoding.picodiploma.loginwithanimation.data.response.StoryResponse

@Database(
    entities = [StoryResponse::class],
    version = 1,
    exportSchema = false
)

abstract class StoryDatabase: RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object{

    }
}