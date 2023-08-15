package com.example.weatherforecast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.API.WeatherData
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.ListItemBinding
import com.squareup.picasso.Picasso

class WeatherAdapter(val listener: Listener?) : ListAdapter<WeatherData, WeatherAdapter.Holder>(Comparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemWeather: WeatherData? = null

        init {
            itemView.setOnClickListener {
                itemWeather?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: WeatherData) = with(binding) {
            itemWeather = item
            tvDate.text = item.time
            tvCondition.text = item.condition
            tvTemp.text = item.currentTemp.ifEmpty { "${item.minTemp} / ${item.maxTemp}" }
            Picasso.get().load("https:" + item.imageUrl).into(imageView)
        }
    }

    class Comparator : DiffUtil.ItemCallback<WeatherData>() {
        override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }

    }

    interface Listener{
        fun onClick(item: WeatherData)
    }
}