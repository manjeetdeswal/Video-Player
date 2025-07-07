package com.manjeet_deswal.video_player_lite.rec

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.databinding.ItemFolderBinding
import com.manjeet_deswal.video_player_lite.ui.VideoList

class FolderAdapter(
    private val medialFiles:ArrayList<Folder>,
    private val folderList:ArrayList<String>
) : RecyclerView.Adapter<FolderAdapter.FolderHolder>() {





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
     val binding =ItemFolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return FolderHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FolderHolder, position: Int) {
      val folderindex =folderList[position].lastIndexOf("/")
        val folderName =folderList[position].substring(folderindex + 1)
        holder.folderName.text =folderName

        if (folderName == "0"){
            holder.folderName.text ="Internal Storage"
        }
        holder.videoCount.text=noOfFolder(folderList[position]).toString() + " Videos"

        holder.itemView.setOnClickListener {
            val intetnt =Intent(holder.folderName.context ,VideoList::class.java)
            intetnt.putExtra("folName",folderName)
            holder.folderName.context.startActivity(intetnt)
        }
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    inner class FolderHolder(binding: ItemFolderBinding):RecyclerView.ViewHolder(binding.root){
         val folderName =binding.folderName
        val videoCount =binding.videoCount

    }
    private fun noOfFolder(folder:String):Int{
        var filesNo:Int =0
        for (media in medialFiles){
            if (media.path.substring(0,media.path.lastIndexOf("/"))
                    .endsWith(folder)){

                filesNo++
            }

        }
        return folderList.size
    }
}