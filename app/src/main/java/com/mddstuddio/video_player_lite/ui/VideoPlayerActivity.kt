package com.mddstuddio.video_player_lite.ui


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.AspectRatioListener
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mddstuddio.video_player_lite.R
import com.mddstuddio.video_player_lite.data.Folder
import com.mddstuddio.video_player_lite.data.Icon
import com.mddstuddio.video_player_lite.databinding.ActivityVideoBinding
import com.mddstuddio.video_player_lite.rec.DescriptionAdapter
import com.mddstuddio.video_player_lite.rec.IconAdapter
import com.mddstuddio.video_player_lite.rec.VideoListAdapter
import com.mddstuddio.video_player_lite.utils.BrightNess_Dialog
import com.mddstuddio.video_player_lite.utils.PlayListDialog
import com.mddstuddio.video_player_lite.utils.Volume_Dialog
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.properties.Delegates


class VideoPlayerActivity : AppCompatActivity(), View.OnClickListener,GestureDetector.OnGestureListener , AudioManager.OnAudioFocusChangeListener,ScaleGestureDetector.OnScaleGestureListener {

    val id: Long? = null
    var speed = 1.0f
    private val mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private var mScaleFactorFit = 0f
    private val SEEK_STEP: Long = 1000
    private var seekStart: Long = 0
    private var seekChange: Long = 0
    private var seekMax: Long = 0

    private val IGNORE_BORDER: Float = dpToPx(24).toFloat()
    private val SCROLL_STEP: Float = dpToPx(16).toFloat()
    private val SCROLL_STEP_SEEK: Float = dpToPx(8).toFloat()
    private var gestureScrollY = 0f
    private var gestureScrollX = 0f
    private var pos by Delegates.notNull<Int>()

    private lateinit var playerView: DoubleTapPlayerView
    private lateinit var player: ExoPlayer
    private var recyleview: RecyclerView? = null
    private var next: ImageView? = null
    private var prev: ImageView? = null
    private var back: ImageView? = null
    private var lock: ImageView? = null
    private var unlock: ImageView? = null
    private var volume: ImageView? = null

    private var brightness: ImageView? = null
    private var root: RelativeLayout? = null
    private var scale: ImageView? = null
    private var audioTrackButton: ImageView? = null
    private var playlist: ImageView? = null
    private var subtitle: ImageView? = null
    private var cropText: TextView? = null
    private var title_Video: TextView? = null
    private var mute = false

    private var audioManager: AudioManager? = null
    private var brightnessInt: Int = 0
    private var volumeInt: Int = 0

    private lateinit var binding: ActivityVideoBinding
    private lateinit var trackSelection: DefaultTrackSelector
    private var canScale = true
     enum class ControlsMode {
        FULLSCREEN, LOCK
    }

    var cropType = 1
    var expand = false
    lateinit var control: ControlsMode
    var medialist = ArrayList<Folder>()
    val iconModelList = ArrayList<Icon>()
    var dark = false
    val playerList=ArrayList<Folder>()
    private var parameterName: PlaybackParameters? = null
    private lateinit var nightMode: View
    private lateinit var adapter: IconAdapter
    val adapterM: VideoListAdapter? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private lateinit var concat: ConcatenatingMediaSource

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }
    private lateinit var notificationManager:PlayerNotificationManager
    private lateinit var notificationMangerBuilder:PlayerNotificationManager.Builder
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        nightMode = binding.nightMode
        gestureDetector = GestureDetectorCompat(this, this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        next = findViewById(R.id.btn_next)
        subtitle = findViewById(R.id.subtitle)
        audioTrackButton = findViewById(R.id.audioTrackButton)
        recyleview = findViewById(R.id.CustomrecycleView)
        prev = findViewById(R.id.btn_prev)
        back = findViewById(R.id.backBtn)
        lock = findViewById(R.id.lock)

        unlock = findViewById(R.id.unlock)
        root = findViewById(R.id.rootLayout)
        scale = findViewById(R.id.fit)
        cropText = findViewById(R.id.cropText)
        title_Video = findViewById(R.id.title_Video)
        playlist = findViewById(R.id.playlist)
        volume = findViewById(R.id.volume)
        brightness = findViewById(R.id.brightness)

        next?.setOnClickListener(this)
        subtitle?.setOnClickListener(this)
        playlist?.setOnClickListener(this)
        volume?.setOnClickListener(this)
        brightness?.setOnClickListener(this)
        prev?.setOnClickListener(this)
        back?.setOnClickListener(this)
        lock?.setOnClickListener(this)
        unlock?.setOnClickListener(this)
        root?.setOnClickListener(this)
        scale?.setOnClickListener(this)
        cropText?.setOnClickListener(this)
        audioTrackButton?.setOnClickListener(this)
        title_Video?.setOnClickListener(this)
        // next=findViewById(R.id.)


        try {
            if(intent.data?.scheme.contentEquals("content")){

                val cursor = contentResolver.query(intent.data!!, arrayOf(MediaStore.Video.Media.DATA), null, null,
                    null)
                cursor?.let {
                    it.moveToFirst()
                    val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val file = File(path)
                    val video=Folder(uri = Uri.fromFile(file).toString(),id=0L, name = file.name,file.name,file.length().toString(),"",path,"")

                    playerList.add(video)
                    cursor.close()
                }
                playerView = binding.playerView
                playVideo1()
            }
            else{
                pos = intent.getIntExtra("pos", 1)
                val type = object : TypeToken<List<Folder?>?>() {}.getType()
                medialist = Gson().fromJson(intent.getStringExtra("videoloist"), type)
                val pref = getSharedPreferences("vidPos", Context.MODE_PRIVATE).edit()
                pref.putInt("postion", pos)
                pref.apply()
                title_Video?.text = medialist[pos].name

                title_Video?.text = medialist.get(pos).name


                playerView = binding.playerView
                playVideo()
                doublePLayer()
                subtitle()
            }
        }catch (e: Exception){Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()}







    }

    override fun onResume() {
        super.onResume()
        if (audioManager == null) audioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private fun playVideo() {
        try {
            player.release()
        } catch (e: Exception) {
        }
        trackSelection = DefaultTrackSelector(this)
        trackSelection.setParameters(
            trackSelection.buildUponParameters()
                .setPreferredAudioLanguages("en").setPreferredTextLanguage("en")
        )
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelection).build()
        playerView.player = player
        playerView.controllerHideOnTouch=true

        playerView.subtitleView?.setApplyEmbeddedFontSizes(false)

        val mediaItem = MediaItem.fromUri(medialist[pos].uri)
        player.setMediaItem(mediaItem)

        player.prepare()



        doublePLayer()
        playerView.keepScreenOn = true

        player.play()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED)
                    playNext()
            }
        })
        playerror()
        val mediaSession = MediaSessionCompat(this, "Player")
        mediaSession.isActive = true
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, medialist[pos].name)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, medialist[pos].displayName)
                .build()
        )
        val adapter=DescriptionAdapter(mediaSession)
        notificationMangerBuilder=PlayerNotificationManager.Builder(
            this,12345,"videoMdd"
        )
        notificationMangerBuilder.setChannelImportance(IMPORTANCE_HIGH)
        notificationMangerBuilder.setNotificationListener( object :PlayerNotificationManager.NotificationListener{
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                super.onNotificationCancelled(notificationId, dismissedByUser)
                Toast.makeText(this@VideoPlayerActivity, "Cancel", Toast.LENGTH_SHORT).show()
            }

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                super.onNotificationPosted(notificationId, notification, ongoing)
                Toast.makeText(this@VideoPlayerActivity, "Posted", Toast.LENGTH_SHORT).show()
            }
        })
        notificationMangerBuilder.setMediaDescriptionAdapter(adapter).build().setPlayer(player)
        notificationManager=notificationMangerBuilder.build()


        notificationManager.setMediaSessionToken(mediaSession.sessionToken)
        notificationManager.setUsePreviousAction(true)
        notificationManager.setUseFastForwardAction(true)
        notificationManager.setUsePlayPauseActions(true)


        notificationManager.setVisibility(VISIBILITY_PUBLIC)

        notificationManager.setPlayer(player)



    }

    private fun playVideo1() {
        try {
            player.release()
        } catch (e: Exception) {
        }
        trackSelection = DefaultTrackSelector(this)
        trackSelection.setParameters(
            trackSelection.buildUponParameters()
                .setPreferredAudioLanguages("en").setPreferredTextLanguage("en")
        )
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelection).build()
        playerView.player = player
        playerView.subtitleView?.setApplyEmbeddedFontSizes(false)
        title_Video?.text = playerList[0].name
        val mediaItem = MediaItem.fromUri(playerList[0].uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        doublePLayer()
        playerView.keepScreenOn = true
        player.play()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED)
                    playNext()
            }
        })
        playerror()
    }


    private fun playerror() {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(this@VideoPlayerActivity, "Unknown error found", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        )
        player.playWhenReady
    }
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("KEY_PLAYER_POSITION", player.contentPosition)
        outState.putBoolean("KEY_PLAYER_PLAY_WHEN_READY", player.playWhenReady)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.let {
            player.seekTo(it.getLong("KEY_PLAYER_POSITION"))
            player.playWhenReady = it.getBoolean("KEY_PLAYER_PLAY_WHEN_READY")
        }
    }


    fun playNext() {
        try {
            player.stop()
            ++pos
            title_Video?.text = medialist[pos].name
            playVideo()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "NO Next Video", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_next -> {
                playNext()
            }
            R.id.btn_prev -> {
                try {
                    player.stop()
                    --pos
                    title_Video?.text = medialist[pos].name
                    playVideo()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "NO Previous Video", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.backBtn -> {
                finish()
            }

            R.id.unlock -> {
                control = VideoPlayerActivity.ControlsMode.LOCK
                root?.visibility = View.INVISIBLE
                lock?.visibility = View.VISIBLE
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show()
            }
            R.id.audioTrackButton -> {



                val audioTrack = ArrayList<String>()
                val audioList = ArrayList<String>()
                for(group in player.currentTracksInfo.trackGroupInfos){
                    if(group.trackType == C.TRACK_TYPE_AUDIO){
                        val groupInfo = group.trackGroup
                        for (i in 0 until groupInfo.length){
                            audioTrack.add(groupInfo.getFormat(i).language.toString())
                            audioList.add("${audioList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                    + " (${groupInfo.getFormat(i).label})")
                        }
                    }
                }

                if(audioList[0].contains("null")) audioList[0] = "1. Default Track"

                val tempTracks = audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
                val audioDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Language")
                    .setCancelable(true)
                    .setPositiveButton("Off Audio"){ self, _ ->
                        trackSelection.setParameters(trackSelection.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_AUDIO, true
                        ))
                        self.dismiss()
                    }
                    .setItems(tempTracks){_, position ->
                        Snackbar.make(binding.root, audioList[position] + " Selected", 3000).show()
                        trackSelection.setParameters(trackSelection.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                            .setPreferredAudioLanguage(audioTrack[position]))
                    }
                    .create()
                audioDialog.show()

                audioDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                audioDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))







            }


            R.id.lock -> {
                control = VideoPlayerActivity.ControlsMode.FULLSCREEN
                root?.visibility = View.VISIBLE
                lock?.visibility = View.INVISIBLE
                Toast.makeText(this, "UnLocked", Toast.LENGTH_SHORT).show()
            }

            R.id.playlist -> {
                val dialog = PlayListDialog(medialist, adapterM)
                dialog.show(supportFragmentManager, dialog.tag)

            }
            R.id.volume -> {


                val diglog = Volume_Dialog()
                diglog.show(supportFragmentManager, "volDialo")

                adapterM?.notifyDataSetChanged()

            }
            R.id.brightness -> {
                val brigdial = BrightNess_Dialog()
                brigdial.show(supportFragmentManager, "bri")


            }
            R.id.subtitle -> {

                val subtitles = ArrayList<String>()
                val subtitlesList = ArrayList<String>()
                for(group in player.currentTracksInfo.trackGroupInfos){
                    if(group.trackType == C.TRACK_TYPE_TEXT){
                        val groupInfo = group.trackGroup
                        for (i in 0 until groupInfo.length){
                            subtitles.add(groupInfo.getFormat(i).language.toString())
                            subtitlesList.add("${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                    + " (${groupInfo.getFormat(i).label})")
                        }
                    }
                }
                if (subtitlesList.isEmpty()){
                    subtitlesList.add("null")
                }
                if(subtitlesList[0].contains("null")) subtitlesList[0] = "NO Subtitles"

                val tempTracks = subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
                val sDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Subtitles")
                    .setCancelable(true)
                    .setPositiveButton("Off Subtitles"){ self, _ ->
                        trackSelection.setParameters(trackSelection.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, true
                        ))
                        self.dismiss()
                    }
                    .setItems(tempTracks){_, position ->
                        Snackbar.make(binding.root, subtitlesList[position] + " Selected", 3000).show()
                        trackSelection.setParameters(trackSelection.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setPreferredTextLanguage(subtitles[position]))
                    }
                    .create()
                sDialog.show()
                sDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                sDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))




            }
            R.id.fit -> {

                when (cropType) {
                    1 -> {
                        cropText?.visibility = View.VISIBLE
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        cropText?.text = "FULL SCREEN"
                        cropType = 2
                        Handler(Looper.getMainLooper()).postDelayed({
                            cropText?.visibility = View.GONE
                        }, 1000)
                    }
                    2 -> {
                        cropText?.visibility = View.VISIBLE
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        cropText?.text = "ZOOM"
                        cropType = 3
                        Handler(Looper.getMainLooper()).postDelayed({
                            cropText?.visibility = View.GONE
                        }, 1000)
                    }
                    3 -> {
                        cropText?.visibility = View.VISIBLE
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        cropText?.text = "FIT"
                        cropType = 4
                        Handler(Looper.getMainLooper()).postDelayed({
                            cropText?.visibility = View.GONE
                        }, 1000)
                    }
                    4 -> {
                        cropText?.visibility = View.VISIBLE
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        cropText?.text = "FULL WIDTH"
                        cropType = 5
                        Handler(Looper.getMainLooper()).postDelayed({
                            cropText?.visibility = View.GONE
                        }, 1000)
                    }
                    5 -> {
                        cropText?.visibility = View.VISIBLE
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        cropText?.text = "FULL HEIGHT"
                        cropType = 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            cropText?.visibility = View.GONE
                        }, 1000)
                    }


                }


            }


        }

    }


    @SuppressLint("NotifyDataSetChanged")
    fun subtitle() {

        speed = 1.0f
        parameterName = PlaybackParameters(speed)
        player.playbackParameters = parameterName as PlaybackParameters
        iconModelList.add(Icon(R.drawable.ic_round_chevron_right_24, ""))
        iconModelList.add(Icon(R.drawable.ic_round_volume_up_24, "Vol"))
        iconModelList.add(Icon(R.drawable.ic_speed_forward_24, "Speed"))
        iconModelList.add(Icon(R.drawable.ic_round_volume_off_24, "Mute"))
        iconModelList.add(Icon(R.drawable.ic_round_screen_rotation_24, "Rotate"))

        adapter = IconAdapter(iconModelList, this, this)

        recyleview?.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyleview?.adapter = adapter
        adapter.notifyDataSetChanged()
        adapter.setOnItemClickListener(object : IconAdapter.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("SourceLockedOrientationActivity", "NotifyDataSetChanged")
            override fun onItemClick(position: Int) {
                when (position) {

                    0 -> {
                        if (expand) {
                            iconModelList.clear()
                            iconModelList.add(Icon(R.drawable.ic_round_chevron_right_24, ""))
                            iconModelList.add(Icon(R.drawable.ic_round_volume_up_24, "Vol"))
                            iconModelList.add(Icon(R.drawable.ic_speed_forward_24, "Speed"))
                            iconModelList.add(Icon(R.drawable.ic_round_volume_off_24, "Mute"))
                            iconModelList.add(
                                Icon(
                                    R.drawable.ic_round_screen_rotation_24,
                                    "Rotate"
                                )
                            )
                            adapter.notifyDataSetChanged()

                            expand = false

                        } else {
                            if (iconModelList.size == 5) {
                                iconModelList.add(Icon(R.drawable.ic_round_nights_stay_24, "Night"))
                                iconModelList.add(
                                    Icon(
                                        R.drawable.ic_round_equalizer_24,
                                        "Equalizer"
                                    )
                                )

                            }
                            iconModelList.set(position, Icon(R.drawable.ic_round_arrow_left_24, ""))
                            adapter.notifyDataSetChanged()
                            expand = true
                        }

                    }

                    1 -> {
                        val diglog = Volume_Dialog()
                        diglog.show(supportFragmentManager, "volDialo")

                        adapterM?.notifyDataSetChanged()

                    }

                    2 -> {
                        val builder = AlertDialog.Builder(this@VideoPlayerActivity)
                        builder.setTitle("Select PlayBack Speed")
                        builder.setPositiveButton("Ok", null)
                        val arr = arrayOf("0.5x", "1.0x(Normal)", "1.25", "1.5x", "2.0x", "4.0x")
                        val checkedItem = -1


                        builder.setSingleChoiceItems(
                            arr,
                            checkedItem,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    when (p1) {
                                        0 -> {
                                            speed = 0.5f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters
                                        }
                                        1 -> {
                                            speed = 1.0f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters
                                        }
                                        2 -> {
                                            speed = 1.25f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters

                                        }
                                        3 -> {
                                            speed = 1.5f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters

                                        }
                                        4 -> {
                                            speed = 2.0f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters
                                        }
                                        5 -> {
                                            speed = 4.0f
                                            parameterName = PlaybackParameters(speed)
                                            player.playbackParameters =
                                                parameterName as PlaybackParameters
                                        }
                                    }
                                }

                            })
                        val dialog = builder.create()
                        dialog.show()


                    }

                    3 -> {
                        if (mute) {
                            player.deviceVolume = 5
                            iconModelList.set(
                                position,
                                Icon(R.drawable.ic_round_volume_off_24, "Mute")
                            )
                            mute = false
                            adapter.notifyDataSetChanged()


                        } else {
                            player.deviceVolume = 0
                            iconModelList.set(
                                position,
                                Icon(R.drawable.ic_round_volume_up_24, "UnMute")
                            )
                            mute = true
                            adapter.notifyDataSetChanged()


                        }
                    }
                    4 -> {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            cropType = 3
                            adapter.notifyDataSetChanged()
                        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            adapter.notifyDataSetChanged()
                        }
                    }

                    5 -> {
                        if (dark) {
                            nightMode.visibility = View.GONE
                            iconModelList.set(
                                position,
                                Icon(R.drawable.ic_round_nights_stay_24, "Night")
                            )
                            adapter.notifyDataSetChanged()
                            dark = false
                        } else {
                            nightMode.visibility = View.VISIBLE
                            iconModelList.set(
                                position,
                                Icon(R.drawable.ic_round_nights_stay_24, "Day")
                            )
                            dark = true
                            adapter.notifyDataSetChanged()
                        }
                    }
                    6 -> {
                        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                        if ((intent.resolveActivity(packageManager) != null)) {
                            startActivityForResult(intent, 124)
                        } else {
                            Toast.makeText(
                                this@VideoPlayerActivity,
                                "No Equalizer found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }

        })

    }

    @SuppressLint("ClickableViewAccessibility")
    fun doublePLayer() {
        binding.ytOverlay.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationEnd() {
                binding.ytOverlay.visibility = View.GONE
            }

            override fun onAnimationStart() {
                binding.ytOverlay.visibility = View.VISIBLE
            }

        })
        binding.ytOverlay.player(player)
        binding.playerView.setOnTouchListener { _, motion_event ->
            gestureDetector?.onTouchEvent(motion_event)
            if (motion_event.action == MotionEvent.ACTION_UP) {
                binding.brightnessIcon.visibility = View.GONE
                binding.volumeIcon.visibility = View.GONE
            }

            return@setOnTouchListener false
        }
    }


    override fun onShowPress(p0: MotionEvent?) = Unit
    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
    playerView.hideController()
        return true
    }

    override fun onLongPress(p0: MotionEvent?) = Unit
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, p3: Float): Boolean {
      return true
    }

    override fun onDown(motionEvent: MotionEvent?): Boolean {
        gestureScrollY = 0f
        gestureScrollX = 0f

        return false
    }
    override fun onScroll(event: MotionEvent?, event1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val sWidth = Resources.getSystem().displayMetrics.widthPixels
        seekStart = player.getCurrentPosition()
        seekChange = 0L
        seekMax = player.getDuration()
        // Exclude edge areas
        if (event!!.getY() < IGNORE_BORDER || event.getX() < IGNORE_BORDER || event.getY() > sWidth - IGNORE_BORDER || event.getX() > sWidth - IGNORE_BORDER) return false

        if (gestureScrollY == 0f || gestureScrollX == 0f) {
            gestureScrollY = 0.0001f
            gestureScrollX = 0.0001f
            return false
        }

            gestureScrollX += distanceX
            if (Math.abs(gestureScrollX) > SCROLL_STEP  && Math.abs(
                    gestureScrollX
                ) > SCROLL_STEP_SEEK
            ){
                // Do not show controller if not already visible

                var position: Long = 0
                val distanceDiff =
                    Math.max(0.5f, Math.min(Math.abs(pxToDp(distanceX) / 4), 10f))
                if (player.isPlaying) {
                    if (gestureScrollX > 0) {
                        if (seekStart + seekChange - SEEK_STEP * distanceDiff >= 0) {
                           player.setSeekParameters(SeekParameters.PREVIOUS_SYNC)
                            seekChange -= (SEEK_STEP * distanceDiff).toLong()
                            position = seekStart + seekChange
                            player.seekTo(position)
                        }
                    } else {
                      player.setSeekParameters(SeekParameters.NEXT_SYNC)
                        if (seekMax == C.TIME_UNSET) {
                            seekChange += (SEEK_STEP * distanceDiff).toLong()
                            position = seekStart + seekChange
                            player.seekTo(position)
                        } else if (seekStart + seekChange + SEEK_STEP < seekMax) {
                            seekChange += (SEEK_STEP * distanceDiff).toLong()
                            position = seekStart + seekChange
                            player.seekTo(position)
                        }
                    }
                    var message: String = formatMilisSign(seekChange)!!

                        message += """  ${formatMilis(position)}
                    """.trimIndent()
                    binding.curDuration.visibility=View.VISIBLE

                   binding.curDuration.setText(message)
                    Handler().postDelayed({
                        binding.curDuration.visibility=View.GONE
                    },300)
                    gestureScrollX = 0.0001f
                }
            }













        if(abs(distanceX) < abs(distanceY)){
            if(event!!.x < sWidth/2){
                //brightness
                binding.brightnessIcon.visibility = View.VISIBLE
                binding.volumeIcon.visibility = View.GONE
                val increase = distanceY > 0
                val newValue = if(increase) brightnessInt + 1 else brightnessInt - 1
                if(newValue in 0..30) brightnessInt = newValue
                binding.brightnessIcon.text = brightnessInt.toString()
                setScreenBrightness(brightnessInt)
            }
            else{
                //volume
                binding.brightnessIcon.visibility = View.GONE
                binding.volumeIcon.visibility = View.VISIBLE
                val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val increase = distanceY > 0
                val newValue = if(increase) volumeInt + 1 else volumeInt - 1
                if(newValue in 0..maxVolume) volumeInt = newValue
                binding.volumeIcon.text = volumeInt.toString()
                audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volumeInt, 0)
            }
        }

        return true
    }

    private fun setScreenBrightness(value: Int){
        val d = 1.0f/30
        val lp = this.window.attributes
        lp.screenBrightness = d * value
        this.window.attributes = lp
    }

    override fun onAudioFocusChange(p0: Int) {
        if(p0 <= 0){
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC,volumeInt,0)
        }
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector?): Boolean {
        val previousScaleFactor = mScaleFactor
        mScaleFactor *= scaleGestureDetector!!.getScaleFactor()
        mScaleFactor = Math.max(0.25f, Math.min(mScaleFactor, 2.0f))

        if (isCrossingThreshold(previousScaleFactor, mScaleFactor, 1.0f) ||
            isCrossingThreshold(previousScaleFactor, mScaleFactor, mScaleFactorFit)
        ) playerView.videoSurfaceView!!.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        setScale(mScaleFactor)
        restoreSurfaceView()
        //clearIcon()
       // setCustomErrorMessage((mScaleFactor * 100) as Int.toString() + "%")
        return true
    }

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {

        mScaleFactor =playerView.videoSurfaceView!!.getScaleX()
        if (playerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
            canScale = false
           playerView.setAspectRatioListener(AspectRatioListener { targetAspectRatio: Float, naturalAspectRatio: Float, aspectRatioMismatch: Boolean ->
               playerView.setAspectRatioListener(null)
                mScaleFactorFit = getScaleFit()
                mScaleFactor = mScaleFactorFit
                canScale = true
            })
            playerView.videoSurfaceView!!.setAlpha(0f)
           playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
        } else {
            mScaleFactorFit = getScaleFit()
            canScale = true
        }
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        restoreSurfaceView()
    }



    fun setScale(scale: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val videoSurfaceView: View = playerView.videoSurfaceView!!
            videoSurfaceView.scaleX = scale
            videoSurfaceView.scaleY = scale
            //videoSurfaceView.animate().setStartDelay(0).setDuration(0).scaleX(scale).scaleY(scale).start();
        }
    }
    private fun isCrossingThreshold(val1: Float, val2: Float, threshold: Float): Boolean {
        return val1 < threshold && val2 >= threshold || val1 > threshold && val2 <= threshold
    }

    private fun restoreSurfaceView() {
        if (playerView.videoSurfaceView!!.getAlpha() != 1f) {
            playerView.videoSurfaceView!!.setAlpha(1f)
        }
    }
    private fun getScaleFit(): Float {
        val sWidth = Resources.getSystem().displayMetrics.widthPixels
        val sHeight = Resources.getSystem().displayMetrics.heightPixels
        return Math.min(
            sWidth as Float /playerView.videoSurfaceView!!.getHeight() as Float,
            sHeight as Float / playerView.videoSurfaceView!!.getWidth() as Float
        )
    }
    fun formatMilis(time: Long): String {
        val totalSeconds = Math.abs(time.toInt() / 1000)
        val seconds = totalSeconds % 60
        val minutes = totalSeconds % 3600 / 60
        val hours = totalSeconds / 3600
        return if (hours > 0) String.format(
            "%d:%02d:%02d",
            hours,
            minutes,
            seconds
        ) else String.format("%02d:%02d", minutes, seconds)
    }

    fun formatMilisSign(time: Long): String? {
        return if (time > -1000 && time < 1000) formatMilis(time) else (if (time < 0) "−" else "+") + formatMilis(
            time
        )
    }


}