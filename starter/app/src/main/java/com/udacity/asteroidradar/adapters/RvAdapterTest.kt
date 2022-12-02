package com.udacity.asteroidradar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R


class RvAdapterTest(val listener: OnItemClick) :
    ListAdapter<Asteroid, RvAdapterTest.MyViewHolder>(AsteroidDiffUtil()) {
    class AsteroidDiffUtil : DiffUtil.ItemCallback<Asteroid>() {
        override fun areItemsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvAdapterTest.MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.custom_tv, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindDate(getItem(position))
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iv: ImageView = view.findViewById(R.id.iv_test)
        private val tv1: TextView = view.findViewById(R.id.tv_rvName)
        private val tv2: TextView = view.findViewById(R.id.tv_rvAge)
        fun bindDate(asteroid: Asteroid) {
            tv1.text = asteroid.closeApproachDate
            tv2.text = asteroid.codename
            if (asteroid.isPotentiallyHazardous) {
                iv.setImageResource(R.drawable.ic_status_potentially_hazardous)
            } else {
                iv.setImageResource(R.drawable.ic_status_normal)
            }
        }

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClick {
        fun onItemClick(positionClick: Int)
    }
}







