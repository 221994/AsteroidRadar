package com.udacity.asteroidradar.adapters

import androidx.recyclerview.widget.DiffUtil
import com.udacity.asteroidradar.Asteroid

class RvAdapterDiffUtil(
    private val oldAsteroidList: List<Asteroid>,
    private val newAsteroidList: List<Asteroid>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldAsteroidList.size
    }

    override fun getNewListSize(): Int {
        return newAsteroidList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldAsteroidList[oldItemPosition].closeApproachDate == newAsteroidList[newItemPosition].closeApproachDate
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldAsteroidList[oldItemPosition].closeApproachDate != newAsteroidList[newItemPosition].closeApproachDate -> {
                false
            }
            oldAsteroidList[oldItemPosition].codename != newAsteroidList[newItemPosition].codename -> {
                false
            }

            else ->
                return true
        }
    }
}