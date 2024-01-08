package com.dicoding.picodiploma.loginwithanimation.data.retrofit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem

class StoryPagingSource(private val token: String, private val apiService: ApiService) : PagingSource<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> = try {
        val page = params.key ?: INITIAL_PAGE_INDEX
        val responseData = apiService.getStories(token, page, params.loadSize).listStory

        LoadResult.Page(
            data = responseData,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (responseData.isNullOrEmpty()) null else page + 1
        )
    } catch (exception: Exception) {
        LoadResult.Error(exception)
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.run {
                prevKey?.plus(1) ?: nextKey?.minus(1)
            }
        }
}