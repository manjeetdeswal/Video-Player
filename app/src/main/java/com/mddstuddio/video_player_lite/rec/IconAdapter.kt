package com.mddstuddio.video_player_lite.rec

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mddstuddio.video_player_lite.data.Icon
import com.mddstuddio.video_player_lite.databinding.ItemIconBinding


import android.R
import android.widget.Toast


class IconAdapter(val list :ArrayList<Icon>,
                  val context: Context,
lisner:View.OnClickListener): RecyclerView.Adapter<IconAdapter.IconViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Define listener member variable
    private lateinit var listener: OnItemClickListener

    // Define the method that allows the parent activity or fragment to define the listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }



    inner class IconViewHolder(binding:ItemIconBinding,listner:OnItemClickListener):RecyclerView.ViewHolder(binding.root){
        val iconImg =binding.playbackIcon
        val iconName=binding.iconName
        init {
            // Attach a click listener to the entire row view
            itemView.setOnClickListener{
                val position = adapterPosition // gets item position
                if (position != RecyclerView.NO_POSITION) {
                 listner.onItemClick(position)

            }
        }


    }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconAdapter.IconViewHolder {
     val bind =ItemIconBinding.inflate(LayoutInflater.from(context),parent,false)
        return IconViewHolder(bind,listener)

    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
      val icon =list[position]
        holder.apply {
            iconName.text=icon.iconTitle
            Glide.with(context).load(icon.imageView).into(iconImg)

        }
    }

    override fun getItemCount(): Int {
      return  list.size
    }
}