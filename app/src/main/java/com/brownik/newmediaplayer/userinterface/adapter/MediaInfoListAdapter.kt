package com.brownik.newmediaplayer.userinterface.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brownik.newmediaplayer.data.MediaInfoData
import com.brownik.newmediaplayer.R
import com.brownik.newmediaplayer.databinding.MediaInfoRvBinding

class MediaInfoListAdapter(
    private val onClick: (MediaInfoData) -> Unit,
) :
    ListAdapter<MediaInfoData, MediaInfoListAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(
        private val binding: MediaInfoRvBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(data: MediaInfoData) = with(binding) {
            binding.mediaInfoData = data
            binding.musicInfoLayer.setOnClickListener {
                onClick(data)
            }
        }
    }

    override fun submitList(list: MutableList<MediaInfoData>?) {
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            MediaInfoRvBinding.bind(layoutInflater.inflate(R.layout.media_info_rv, parent, false))
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    object DiffCallback : DiffUtil.ItemCallback<MediaInfoData>() {
        override fun areItemsTheSame(
            oldItem: MediaInfoData,
            newItem: MediaInfoData,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: MediaInfoData,
            newItem: MediaInfoData,
        ): Boolean {
            return false
        }
    }
}