package com.manjeet_deswal.video_player_lite.rec

import android.app.Activity
import android.app.Dialog
import android.content.*
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.databinding.ItemVideoBinding


import com.manjeet_deswal.video_player_lite.ui.VideoPlayerActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VideoAdapter (val context: Context, var allMediaList: ArrayList<Folder> = ArrayList()): RecyclerView.Adapter<VideoAdapter.VideoHolder>() {


    private lateinit var bottomSheetDialog: BottomSheetDialog
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {

        val binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        return VideoHolder(binding)
    }



    override fun getItemCount(): Int {
      return allMediaList.size
    }

    class VideoHolder(binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.videoThumb
        val title = binding.videoTitle

        val vidSize =binding.videoSize
        val vidOption =binding.videoOption

        val duraion =binding.duration

        val date =binding.videoDate




    }

    fun updateList(list: ArrayList<Folder>) {
        allMediaList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val video =allMediaList[position]

        holder.apply {
            title.text = video.name

            val size =allMediaList[position].size
            vidSize.text=android.text.format.Formatter.formatFileSize(context,
                size.toLong())

            duraion.text = video.duration.toString()
           // adlayout.visibility=View.GONE

            val og=getDate(video.dateAdded.toLong())
            date.text=og
           Glide.with(context).load(video.uri).into(image)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("videoloist", Gson().toJson(allMediaList))
                intent.putExtra("pos", position)
                context.startActivity(intent)
            }


                vidOption.setOnClickListener {


                    bottomSheetDialog= BottomSheetDialog(context, R.style.BottomTheme)
                    val view =LayoutInflater.from(context).inflate(R.layout.video_btm_sheet,it.findViewById(R.id.btmSheet))

                    view.findViewById<LinearLayoutCompat>(R.id.bsPLay).setOnClickListener {
                        holder.itemView.performClick()
                        bottomSheetDialog.dismissWithAnimation
                    }
                    view.findViewById<LinearLayoutCompat>(R.id.btn_rename).setOnClickListener {
                        val dialog = Dialog(context)
                        dialog.setContentView(R.layout.rename_layout)
                        dialog.getWindow()?.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val editText: EditText = dialog.findViewById(R.id.rename_edit_text)
                        val cancel: Button = dialog.findViewById(R.id.cancel_rename_button)
                        val rename_btn: Button = dialog.findViewById(R.id.rename_button)
                        dialog.show()
                        val file: File = File(allMediaList[position].path)
                        var nameText = file.name
                        nameText = nameText.substring(0, nameText.lastIndexOf("."))
                        editText.setText(nameText)
                        editText.requestFocus()
                        dialog.getWindow()
                            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                        cancel.setOnClickListener{
                            dialog.cancel()
                        }
                        rename_btn.setOnClickListener {
                            val onlyPath = file.parentFile.absolutePath
                            var ext = file.absolutePath
                            ext = ext.substring(ext.lastIndexOf("."))
                            val newPath = onlyPath + "/" + editText.text.toString() + ext
                            val newFile = File(newPath)
                            val rename = file.renameTo(newFile)
                            if (rename) {
                                val resolver: ContentResolver =
                                    context.getApplicationContext().getContentResolver()
                                resolver.delete(
                                    MediaStore.Files.getContentUri("external"),
                                    MediaStore.MediaColumns.DATA + "=?",
                                    arrayOf(file.absolutePath)
                                )
                                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                intent.data = Uri.fromFile(newFile)
                                context.getApplicationContext().sendBroadcast(intent)
                                Toast.makeText(context, "SuccessFull!", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Oops! rename failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            notifyDataSetChanged()
                            notifyItemChanged(position)
                            dialog.dismiss()
                        }
                        bottomSheetDialog.dismissWithAnimation
                    }

                    view.findViewById<LinearLayoutCompat>(R.id.btn_share).setOnClickListener {

                        shareVideo(video.name,video.path)
                        bottomSheetDialog.dismissWithAnimation
                    }




                    view.findViewById<LinearLayoutCompat>(R.id.btn_delete).setOnClickListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Delete Video")
                        builder.setMessage(
                            "Are you sure you have to Delete " + video.name
                        )

                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            val file: File = File(
                                video.path
                            )


                            val mediaID: Long = getFilePathToMediaID(
                                file.absolutePath,
                                context.applicationContext
                            )

                            val Uri_one = ContentUris.withAppendedId(
                                MediaStore.Video.Media.getContentUri("external"), mediaID
                            )

                            val uriLists: MutableList<Uri> = java.util.ArrayList()
                            uriLists.add(Uri_one)



                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                //code
                                requestDeletePermission(uriLists)

                                Handler().postDelayed({
                                    allMediaList.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyDataSetChanged()
                                    notifyItemChanged(position,allMediaList.size)
                                }, 2000)


                            }

                            if (file.exists() && file.delete()) {
                                notifyItemRemoved(position)
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                        builder.setNegativeButton(android.R.string.no) { dialog, _ ->
                            dialog.cancel()
                        }

                        builder.show()

                        bottomSheetDialog.dismissWithAnimation
                    }

                    view.findViewById<LinearLayoutCompat>(R.id.btn_properties).setOnClickListener {
                        val alertDialog = AlertDialog.Builder(context)
                        alertDialog.setTitle("Properties")
                        val filename= "Name: " +video.name
                        val filesize= "Size: " +android.text.format.Formatter.formatFileSize(context,
                            size.toLong())
                        val fileduration= "Duration: " +video.duration
                        val meta= MediaMetadataRetriever()
                        meta.setDataSource(video.path)


                        val heigtt= meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        val width=meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

                        val res ="Resolution: "+heigtt+ "x"+ width
                        val path="Path: "+video.path


                        alertDialog.setMessage(filename +"\n\n" +filesize +"\n\n"+fileduration +"\n\n"+res+"\n\n" +path)


                        alertDialog.setPositiveButton("ok"
                        ) { p0, _ -> p0?.dismiss() }
                        alertDialog.setNegativeButton("cancel"
                        ) { p0, _ -> p0?.dismiss() }
                        alertDialog.show()
                        bottomSheetDialog.dismissWithAnimation
                    }

                    bottomSheetDialog.setContentView(view)
                    bottomSheetDialog.show()
                }







        }
    }

    fun getDate(`val`: Long): String? {
        var `val` = `val`
        `val` *= 1000L
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(`val`))
    }

    private fun shareVideo(title: String?, path: String) {
        MediaScannerConnection.scanFile(
            context, arrayOf(path),
            null
        ) { path, uri ->
            val shareIntent = Intent(
                Intent.ACTION_SEND
            )
            shareIntent.type = "video/*"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT, title
            )
            shareIntent.putExtra(
                Intent.EXTRA_TITLE, title
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.str_share_this_video)
                )
            )
        }
    }


    private fun requestDeletePermission(uriList: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(context.getContentResolver(), uriList)
            try {
                /* startIntentSenderForResult(activity,pi.getIntentSender(), REQUEST_PERM_DELETE, new Intent(), 0, 0,
                        0);*/
                val bundle = Bundle()
                ActivityCompat.startIntentSenderForResult(
                    context as Activity, pi.intentSender, 101,
                    Intent(), 1, 1, 2, bundle
                )
            } catch (e: IntentSender.SendIntentException) {
            }
        }
    }


    private fun getFilePathToMediaID(songPath: String, context: Context): Long {
        var id: Long = 0
        val cr = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Video.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val sortOrder = MediaStore.Video.Media.TITLE + " ASC"
        val cursor = cr.query(
            uri, projection,
            "$selection=?", selectionArgs, null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        return id
    }
}