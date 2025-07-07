package com.manjeet_deswal.video_player_lite.ui


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.manjeet_deswal.video_player_lite.R
import com.manjeet_deswal.video_player_lite.data.Folder
import com.manjeet_deswal.video_player_lite.data.Icon
import com.manjeet_deswal.video_player_lite.databinding.ActivityVideoBinding
import com.manjeet_deswal.video_player_lite.rec.IconAdapter
import com.manjeet_deswal.video_player_lite.rec.VideoListAdapter
import com.manjeet_deswal.video_player_lite.utils.BrightNessDialog
import com.manjeet_deswal.video_player_lite.utils.NotAdapter
import com.manjeet_deswal.video_player_lite.utils.PlayListDialog
import com.manjeet_deswal.video_player_lite.utils.Volume_Dialog
import com.manjeet_deswal.video_player_lite.utils.dpToPx
import com.manjeet_deswal.video_player_lite.utils.dtpv.DoubleTapPlayerView
import com.manjeet_deswal.video_player_lite.utils.dtpv.youtube.YouTubeOverlay
import com.manjeet_deswal.video_player_lite.utils.hideSystemBars
import com.manjeet_deswal.video_player_lite.utils.normalizeScaleFactor
import com.manjeet_deswal.video_player_lite.utils.pxToDp
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.properties.Delegates
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.truncate


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoPlayerActivity : AppCompatActivity(), GestureDetector.OnGestureListener,
    AudioManager.OnAudioFocusChangeListener, ScaleGestureDetector.OnScaleGestureListener {


    private lateinit var notificationAdapter: NotAdapter

  //  private lateinit var mediaSession: androidx.media3.session.MediaSession
    val id: Long? = null
    var speed = 1.0f

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


    private lateinit var playerView: PlayerView


    private var mute = false

    private var audioManager: AudioManager? = null

    private var volumeInt: Int = 0


    private lateinit var binding: ActivityVideoBinding
    private lateinit var trackSelection: DefaultTrackSelector
    private var canScale = true

    enum class ControlsMode {
        FULLSCREEN, LOCK
    }

    var cropType = 1
    var expand = false
    private lateinit var control: ControlsMode
     var medialist = ArrayList<Folder>()
    val iconModelList = ArrayList<Icon>()
    var dark = false
    private val playerList = ArrayList<Folder>()
    private var parameterName: PlaybackParameters? = null
    private lateinit var nightMode: View
    private lateinit var adapter: IconAdapter
    val adapterM: VideoListAdapter? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var scaleFactor = 1.0f
    private var layout1Visible = true
    private lateinit var layout1Root: View
    private lateinit var layout2Root: View


    private lateinit var btnNextSong: ImageView
    private lateinit var btnPrevious: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var subtitle: ImageView
    private lateinit var audioTrackButton: ImageView
    private lateinit var unlock: ImageView
    private lateinit var fit: ImageView
    private lateinit var lock: ImageView
    private lateinit var volume: ImageView
    private lateinit var customRecycleView: RecyclerView

    private lateinit var menu:ImageView
    private lateinit var playlist: ImageView
    private lateinit var exoPlay: ImageView
    private lateinit var brightness: ImageView
    private lateinit var rootLayout: RelativeLayout
    private lateinit var cropText: TextView
    private lateinit var titleVideo: TextView


    @RequiresApi(Build.VERSION_CODES.N)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityVideoBinding.inflate(layoutInflater)

        layout1Root = binding.root

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            createNotificationChannel()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        exoPlay = findViewById(R.id.exo_play)
        btnNextSong = findViewById(R.id.btn_nextSong)
        btnPrevious = findViewById(R.id.btn_previous)
        backBtn = findViewById(R.id.backBtn)
        backBtn = findViewById(R.id.backBtn)
        audioTrackButton = findViewById(R.id.audioTrackButton)
        menu = findViewById(R.id.more)
        subtitle = findViewById(R.id.subtitle)
        unlock = findViewById(R.id.unlock)
        fit = findViewById(R.id.fit)
        lock = findViewById(R.id.lock)
        volume = findViewById(R.id.volume)
        customRecycleView = findViewById(R.id.customRecycleView)
        playlist = findViewById(R.id.playlist)
        brightness = findViewById(R.id.brightness)
        rootLayout = findViewById(R.id.rootLayout)
        cropText = findViewById(R.id.cropText)
        titleVideo = findViewById(R.id.title_Video)







        playerView = binding.playerView


        scaleGestureDetector = ScaleGestureDetector(this, this)

        (playerView as DoubleTapPlayerView).isDoubleTapEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        nightMode = binding.nightMode
        gestureDetector = GestureDetectorCompat(this, this)
        hideSystemBars(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val window = window
            if (window != null) {
                window.setDecorFitsSystemWindows(false)
                val windowInsetsController = window.insetsController
                if (windowInsetsController != null) {
                    // On Android 12 BEHAVIOR_DEFAULT allows system gestures without visible system bars
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }

        btnNextSong.setOnClickListener {
            playNext()
        }


        menu.setOnClickListener{
            val popupMenu = PopupMenu(this,menu)
            popupMenu.menuInflater.inflate(R.menu.player,popupMenu.menu)
            popupMenu.menu.findItem(R.id.repeat).title = "Repeat : $repeat"
            popupMenu.setOnMenuItemClickListener { item ->
                if (item!!.itemId == R.id.repeat) {
                    repeat = !repeat

                }
                true
            }
            popupMenu.show()
        }

        volume.setOnClickListener {
            val dialog = Volume_Dialog()
            dialog.show(supportFragmentManager, "volDialo")

            adapterM?.notifyDataSetChanged()
        }



        btnPrevious.setOnClickListener {
            playPrev()
        }
        backBtn.setOnClickListener {
            finish()
        }
        lock.setOnClickListener {
            binding.playerView.isDoubleTapEnabled = true
            control = ControlsMode.FULLSCREEN
            rootLayout.visibility = View.VISIBLE
            lock.visibility = View.INVISIBLE

            Toast.makeText(this@VideoPlayerActivity, "UnLocked", Toast.LENGTH_SHORT).show()
        }
        unlock.setOnClickListener {
            control = ControlsMode.LOCK
            binding.playerView.isDoubleTapEnabled = false
            rootLayout.visibility = View.INVISIBLE
            lock.visibility = View.VISIBLE
            Toast.makeText(this@VideoPlayerActivity, "Locked", Toast.LENGTH_SHORT).show()
        }
        audioTrackButton.setOnClickListener {
            audioSelect()
        }
        subtitle.setOnClickListener {
            subtitleSelect()
        }
        playlist.setOnClickListener {
            val dialog = PlayListDialog(medialist, adapterM)

            dialog.show(supportFragmentManager, dialog.tag)
        }
        brightness.setOnClickListener {
            val brightDial = BrightNessDialog(this)
            brightDial.show(supportFragmentManager, "bri")
        }
        fit.setOnClickListener {
            fitScreen()
        }

        exoPlay.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                exoPlay.setImageResource(R.drawable.ic_round_play_arrow)
            } else {
                exoPlay.setImageResource(R.drawable.ic_round_pause_24)
                player.play()
            }
        }


        control = ControlsMode.FULLSCREEN
        try {
            if (intent.data?.scheme.contentEquals("content")) {

                val cursor = intent.data?.let {
                    contentResolver.query(
                        it, arrayOf(MediaStore.Video.Media.DATA), null, null, null
                    )
                }
                cursor?.let {
                    it.moveToFirst()
                    val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val file = File(path)
                    val video = Folder(
                        uri = Uri.fromFile(file).toString(),
                        id = 0L,
                        name = file.name,
                        file.name,
                        file.length().toString(),
                        "",
                        path,
                        ""
                    )
                    playerList.add(video)
                    cursor.close()
                }
                playerView = binding.playerView
                playVideo1()
            } else {
                pos = intent.getIntExtra("pos", 1)
                val type = object : TypeToken<List<Folder?>?>() {}.type
                medialist = Gson().fromJson(intent.getStringExtra("videoloist"), type)
                getSharedPreferences("vidPos", Context.MODE_PRIVATE).edit {
                    putInt("postion", pos)
                }
                titleVideo.text = medialist[pos].name

                titleVideo.text = medialist[pos].name

                playerView = binding.playerView
                playVideo()
                doublePLayer()
                subtitle()
            }
        } catch (e: Exception) {
           // Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
           "channel_my", // Add this string to strings.xml
            NotificationManager.IMPORTANCE_LOW // Use LOW to avoid sound for ongoing notifications
        ).apply {
            description ="playback_channel_description" // Add to strings.xml
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No runtime permission needed before Android 13
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, setup notification if player is active
                if ( player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) { // Check if player is ready
                    if (player.playbackState != Player.STATE_IDLE) createNotification(player)
                }
            } else {
                Toast.makeText(this, "Notification permission denied. Playback notifications will not be shown.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun toggleLayoutVisibility() {
        if (layout1Visible) {
            layout1Root.visibility = View.GONE
            layout2Root.visibility = View.VISIBLE
        } else {
            layout1Root.visibility = View.VISIBLE
            layout2Root.visibility = View.GONE
        }
        layout1Visible = !layout1Visible
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun playPrev() {
        try {
            if (pos > 0  ) {
                player.stop()
                --pos
                titleVideo.text = medialist[pos].name
                exoPlay.setImageResource(R.drawable.ic_round_pause_24)
                playVideo()
            }
            else{
               Toast.makeText(this@VideoPlayerActivity, "NO Previous Video", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //Toast.makeText(this@VideoPlayerActivity, "NO Previous Video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (audioManager == null) audioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.requestAudioFocus(
            this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun playVideo() {
        try {
            player.release()
        } catch (_: Exception) {
        }


        trackSelection = DefaultTrackSelector(this)
        trackSelection.setParameters(
            trackSelection.buildUponParameters().setPreferredAudioLanguages("en")
                .setPreferredTextLanguage("en")
        )





        player = ExoPlayer.Builder(this).setTrackSelector(trackSelection).build()


        playerView.player = player

        doublePLayer()

       // val mediaItem = MediaItem.fromUri(medialist[pos].uri.toUri())
        val mediaItem =   createRichMediaItem(medialist[pos])



        player.setMediaItem(mediaItem)

        player.prepare()



        player.play()

        playerView.keepScreenOn = true

        createNotification(player)


      /*  mediaSession = androidx.media3.session.MediaSession.Builder(this, player)
            .build()

        mediaSession.player = player
*/

        /* mediaSession.isActive = true
         mediaSession.setMetadata(
             MediaMetadataCompat.Builder()
                 .putString(MediaMetadataCompat.METADATA_KEY_TITLE, medialist[pos].name).putString(
                     MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, medialist[pos].uri
                 ).putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, medialist[pos].uri)
                 .putString(
                     MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, medialist[pos].displayName
                 ).build()
         )
         val mediaSessionConnector = androidx.media3.session.MediaSession

        mediaSessionConnector.
     */

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_IDLE) {
                    scaleFactor = 1.0f
                    playerView.videoSurfaceView?.scaleX = scaleFactor
                    playerView.videoSurfaceView?.scaleY = scaleFactor
                }
            }
        })
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) if (repeat) playVideo() else playNext()
            }
        })
        playerError()

    }
    private fun createRichMediaItem(folder: Folder): MediaItem {
        val videoUri = folder.uri.toUri()
        val thumbnailUri = folder.uri.toUri()

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(folder.displayName)
            .setArtist(folder.name)
            .setArtworkUri(thumbnailUri)
            .build()

        return MediaItem.Builder()
            .setUri(videoUri)
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleGestureDetector.onTouchEvent(event)

        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createNotification(playerMY: ExoPlayer) {

        playerNotificationManager?.setPlayer(null) // Clear previous manager if any

        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return playerMY.currentMediaItem?.mediaMetadata?.title ?: "Unknown Title"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@VideoPlayerActivity, VideoPlayerActivity::class.java)
                // Add flags to restore activity to its current state if needed
                return PendingIntent.getActivity(
                    this@VideoPlayerActivity, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return playerMY.currentMediaItem?.mediaMetadata?.artist // Or any other relevant text
            }

            // For the notification's large icon (e.g., video thumbnail or app icon)
            override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                // Try to load actual video thumbnail asynchronously here if possible.
                // For now, using app launcher icon as a placeholder.
              /*  val drawable = ContextCompat.getDrawable(this@VideoPlayerActivity, R.mipmap.ic_launcher)
                if (drawable is BitmapDrawable) {
                    return drawable.bitmap
                }*/
              /*  val newBitmap = getBitmapFromUri(this@VideoPlayerActivity,
                    playerMY.currentMediaItem?.mediaMetadata!!.artworkUri!!
                )*/
                // Convert other drawables to Bitmap
              /*  val bitmap = createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
*/
                Glide.with(this@VideoPlayerActivity)
                    .asBitmap()
                    .load(player.currentMediaItem?.mediaMetadata?.artworkUri)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            callback.onBitmap(resource) // This updates the notification icon
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })

                return null
            }
        }

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(mediaDescriptionAdapter)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) {
                        // If you were using a foreground service, you'd start it here.
                        // For an activity, this ensures the notification stays while playing.
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    // If user dismisses notification, you might want to stop playback.
                    if (dismissedByUser) {
                        player.stop()
                        // Optionally: finish()
                    }
                }
            })
            // For Android 12+ compatibility regarding channel name/description for existing channels
            .setChannelNameResourceId(R.string.fit)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .build().apply {
                setPlayer(player)
                mediaSession?.let { setMediaSessionToken(it.sessionCompatToken) }

                // Control which actions are available
                setUseNextAction(true)

                setUsePreviousAction(true)
                setUseNextActionInCompactView(true) // Show next in compact view
                setUsePreviousActionInCompactView(true) // Show prev in compact view

                // Dynamically disable next/prev if only one item is playing (e.g. from external intent)
                if (player.mediaItemCount <= 1) {
                    setUseNextAction(false)
                    setUsePreviousAction(false)
                    setUseNextActionInCompactView(false)
                    setUsePreviousActionInCompactView(false)
                }
            }

    }

    private fun playVideo1() {
        try {
            player.release()
        } catch (_: Exception) {
        }
        trackSelection = DefaultTrackSelector(this)

        trackSelection.setParameters(
            trackSelection.buildUponParameters().setPreferredAudioLanguages("en")
                .setPreferredTextLanguage("en")
        )
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelection).build()
        playerView.player = player
            //playerView.controllerShowTimeoutMs = 1500
        // Toast.makeText(this,  playerView.subtitleView?.isActivated.toString(), Toast.LENGTH_SHORT).show()
        titleVideo.text = playerList[0].name
        val mediaItem = MediaItem.fromUri(playerList[0].uri)
        player.setMediaItem(mediaItem)


        player.prepare()
        doublePLayer()
        playerView.keepScreenOn = true
        player.play()
        player.addListener(object : Player.Listener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) playNext()
            }
        })
        playerError()
    }


    private fun playerError() {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("TAG", "onPlayerError:$error ")
                Toast.makeText(this@VideoPlayerActivity, "Unknown error found", Toast.LENGTH_SHORT)
                    .show()
            }

        })
        player.playWhenReady
    }


    override fun onDestroy() {
        super.onDestroy()

       // mediaSession.release()

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


    @RequiresApi(Build.VERSION_CODES.N)
    private fun playNext() {

        try {
            if ( pos < medialist.size -1) {
                player.stop()
                ++pos
                titleVideo.text = medialist[pos].name
                exoPlay.setImageResource(R.drawable.ic_round_pause_24)
                playVideo()
            }
            else{
                Toast.makeText(this, "NO Next Video", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
           // Toast.makeText(this, "NO Next Video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun audioSelect() {

        val audioTrack = ArrayList<String>()
        val audioList = ArrayList<String>()
        for (group in player.currentTracks.groups) {
            if (group.mediaTrackGroup.type == C.TRACK_TYPE_AUDIO) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    audioTrack.add(groupInfo.getFormat(i).language.toString())
                    audioList.add(
                        "${audioList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage + " (${
                            groupInfo.getFormat(
                                i
                            ).label
                        })"
                    )
                }
            }
        }

        if (audioList[0].contains("null")) audioList[0] = "1. Default Track"

        val tempTracks = audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
        val audioDialog =
            MaterialAlertDialogBuilder(this, R.style.alertDialog).setTitle("Select Language")
                .setCancelable(true).setPositiveButton("Off Audio") { self, _ ->
                    trackSelection.setParameters(
                        trackSelection.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_AUDIO, true
                        )
                    )
                    self.dismiss()
                }.setItems(tempTracks) { _, position ->
                    Snackbar.make(binding.root, audioList[position] + " Selected", 3000).show()
                    trackSelection.setParameters(
                        trackSelection.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                            .setPreferredAudioLanguage(audioTrack[position])
                    )
                }.create()
        audioDialog.show()

        audioDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        audioDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun subtitleSelect() {
        val subtitles = ArrayList<String>()
        val subtitlesList = ArrayList<String>()
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    subtitles.add(groupInfo.getFormat(i).language.toString())
                    subtitlesList.add(
                        "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                + " (${groupInfo.getFormat(i).label})"
                    )
                }
            }
        }

        val tempTracks = subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
        val sDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
            .setTitle("Select Subtitles")
            .setOnCancelListener {  }
            .setPositiveButton("Off Subtitles") { self, _ ->
                trackSelection.setParameters(
                    trackSelection.buildUponParameters().setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, true
                    )
                )
                self.dismiss()
            }
            .setItems(tempTracks) { _, position ->
                Snackbar.make(binding.root, subtitlesList[position] + " Selected", 3000).show()
                trackSelection.setParameters(
                    trackSelection.buildUponParameters()
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                        .setPreferredTextLanguage(subtitles[position])
                )
            }
            .create()
        sDialog.show()
        sDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        sDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

    }

    private fun fitScreen() {
        when (cropType) {
            1 -> {
                cropText.visibility = View.VISIBLE
                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                cropText.text = getString(R.string.full_screen)
                cropType = 2
                Handler(Looper.getMainLooper()).postDelayed({
                    cropText.visibility = View.GONE
                }, 1000)
            }

            2 -> {
                cropText.visibility = View.VISIBLE
                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                cropText.text = getString(R.string.zoom)
                cropType = 3
                Handler(Looper.getMainLooper()).postDelayed({
                    cropText.visibility = View.GONE
                }, 1000)
            }

            3 -> {
                cropText.visibility = View.VISIBLE
                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                cropText.text = getString(R.string.fit)
                cropType = 4
                Handler(Looper.getMainLooper()).postDelayed({
                    cropText.visibility = View.GONE
                }, 1000)
            }

            4 -> {
                cropText.visibility = View.VISIBLE
                playerView.resizeMode =
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                cropText.text = getString(R.string.full_width)
                cropType = 5
                Handler(Looper.getMainLooper()).postDelayed({
                    cropText.visibility = View.GONE
                }, 1000)
            }

            5 -> {
                cropText.visibility = View.VISIBLE
                playerView.resizeMode =
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                cropText.text = getString(R.string.full_height)
                cropType = 1
                Handler(Looper.getMainLooper()).postDelayed({
                    cropText.visibility = View.GONE
                }, 1000)
            }


        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun subtitle() {

        speed = 1.0f
        parameterName = PlaybackParameters(speed)
        player.playbackParameters = parameterName as PlaybackParameters

        iconModelList.add(Icon(R.drawable.ic_round_chevron_right_24, ""))
        iconModelList.add(Icon(R.drawable.ic_baseline_access_time_24, "Timer"))
        iconModelList.add(Icon(R.drawable.ic_speed_forward_24, "Speed"))
        iconModelList.add(Icon(R.drawable.ic_round_volume_off_24, "Mute"))
        iconModelList.add(Icon(R.drawable.ic_round_screen_rotation_24, "Rotate"))

        adapter = IconAdapter(iconModelList, this)

        customRecycleView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        customRecycleView.adapter = adapter
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
                            iconModelList.add(Icon(R.drawable.ic_baseline_access_time_24, "Timer"))
                            iconModelList.add(Icon(R.drawable.ic_round_volume_up_24, "Vol"))
                            iconModelList.add(Icon(R.drawable.ic_speed_forward_24, "Speed"))
                            iconModelList.add(Icon(R.drawable.ic_round_volume_off_24, "Mute"))
                            iconModelList.add(
                                Icon(R.drawable.ic_round_screen_rotation_24, "Rotate")
                            )
                            adapter.notifyDataSetChanged()

                            expand = false

                        } else {
                            if (iconModelList.size == 5) {
                                iconModelList.add(Icon(R.drawable.ic_round_nights_stay_24, "Night"))
                                iconModelList.add(
                                    Icon(
                                        R.drawable.ic_round_equalizer_24, "Equalizer"
                                    )
                                )

                            }
                            iconModelList[position] = Icon(R.drawable.ic_round_arrow_left_24, "")
                            adapter.notifyDataSetChanged()
                            expand = true
                        }

                    }

                    1 -> {

                        val dialog = Dialog(this@VideoPlayerActivity)

                        dialog.setContentView(R.layout.dialog_timer)
                        dialog.setCancelable(true)
                        val timerText = dialog.findViewById<EditText>(R.id.editTimer)
                        val cancel = dialog.findViewById<Button>(R.id.btnCancel)
                        val ok = dialog.findViewById<Button>(R.id.btnOk)
                        dialog.show()
                        cancel.setOnClickListener {
                            dialog.cancel()
                        }
                        ok.setOnClickListener {
                            val text = timerText.text.toString()
                            if (text.isNotEmpty()) {
                                val time = TimeUnit.MINUTES.toMillis(text.toLong())
                                val timer = object : CountDownTimer(time, 1000) {

                                    override fun onTick(p0: Long) {
                                        adapter.list[1].iconTitle = p0.toString()

                                    }

                                    override fun onFinish() {
                                        finishAffinity()
                                    }


                                }

                                Toast.makeText(
                                    this@VideoPlayerActivity, "Timer Started", Toast.LENGTH_SHORT
                                ).show()
                                timer.start()
                                dialog.hide()
                            } else {
                                Toast.makeText(
                                    this@VideoPlayerActivity, "Enter Minutes", Toast.LENGTH_SHORT
                                ).show()

                            }


                        }




                        adapterM?.notifyDataSetChanged()

                    }

                    2 -> {
                        val builder = AlertDialog.Builder(this@VideoPlayerActivity)
                        builder.setTitle("Select PlayBack Speed")
                        builder.setPositiveButton("Ok", null)
                        val arr = arrayOf("0.5x", "1.0x(Normal)", "1.25", "1.5x", "2.0x", "4.0x")
                        val checkedItem = -1


                        builder.setSingleChoiceItems(
                            arr, checkedItem
                        ) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    speed = 0.5f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters
                                }

                                1 -> {
                                    speed = 1.0f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters
                                }

                                2 -> {
                                    speed = 1.25f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters

                                }

                                3 -> {
                                    speed = 1.5f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters

                                }

                                4 -> {
                                    speed = 2.0f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters
                                }

                                5 -> {
                                    speed = 4.0f
                                    parameterName = PlaybackParameters(speed)
                                    player.playbackParameters = parameterName as PlaybackParameters
                                }
                            }
                        }
                        val dialog = builder.create()
                        dialog.show()


                    }

                    3 -> {
                        if (mute) {
                            player.volume = 0.5F
                            iconModelList[position] =
                                Icon(R.drawable.ic_round_volume_off_24, "Mute")
                            mute = false
                            adapter.notifyDataSetChanged()


                        } else {
                            player.volume = 0.5f
                            iconModelList[position] =
                                Icon(R.drawable.ic_round_volume_up_24, "UnMute")
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
                            iconModelList[position] =
                                Icon(R.drawable.ic_round_nights_stay_24, "Night")
                            adapter.notifyDataSetChanged()
                            dark = false
                        } else {
                            nightMode.visibility = View.VISIBLE
                            iconModelList[position] =
                                Icon(R.drawable.ic_round_nights_stay_24, "Day")
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
                                this@VideoPlayerActivity, "No Equalizer found", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }

        })

    }

    @SuppressLint("ClickableViewAccessibility")
    fun doublePLayer() {

        if (control == ControlsMode.FULLSCREEN) {

            binding.ytOverlay.performListener(object : YouTubeOverlay.PerformListener {
                override fun onAnimationEnd() {

                    binding.ytOverlay.visibility = View.GONE
                }

                override fun onAnimationStart() {
                    binding.ytOverlay.visibility = View.VISIBLE
                }

            })
        }

        binding.ytOverlay.player(player)
        binding.playerView.setOnTouchListener { _, motionEvent ->
            gestureDetector?.onTouchEvent(motionEvent)
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                binding.brightnessIcon.visibility = View.GONE
                binding.volumeIcon.visibility = View.GONE
            }
            scaleGestureDetector.onTouchEvent(motionEvent)
            return@setOnTouchListener false
        }
    }


    override fun onAudioFocusChange(p0: Int) {
        if (p0 <= 0) {
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volumeInt, 0)
        }
    }


    private fun setScale(scale: Float) {

        val videoSurfaceView = playerView.videoSurfaceView
        try {
            videoSurfaceView!!.scaleX = scale
            videoSurfaceView.scaleY = scale
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
        }

        //videoSurfaceView.animate().setStartDelay(0).setDuration(0).scaleX(scale).scaleY(scale).start();

    }


    private fun restoreSurfaceView() {
        if (playerView.videoSurfaceView!!.alpha != 1f) {
            playerView.videoSurfaceView!!.alpha = 1f
        }
    }


    @SuppressLint("DefaultLocale")
    private fun formatMiles(time: Long): String {
        val totalSeconds = abs(time.toInt() / 1000)
        val seconds = totalSeconds % 60
        val minutes = totalSeconds % 3600 / 60
        val hours = totalSeconds / 3600
        return if (hours > 0) String.format(
            "%d:%02d:%02d", hours, minutes, seconds
        ) else String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatMilesSign(time: Long): String {
        return if (time > -1000 && time < 1000) formatMiles(time) else (if (time < 0) "−" else "+") + formatMiles(
            time
        )
    }

    override fun onDown(e: MotionEvent): Boolean {
        gestureScrollY = 0f
        scrollMode = ScrollMode.NONE
        gestureScrollX = 0f
        gestureInitialVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        // Assuming 'brightnessInt' holds the current brightness level (0-30)
        gestureInitialBrightness = brightnessInt
        return true
    }

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        // This now correctly toggles the controller visibility

        return true
    }

    override fun onLongPress(e: MotionEvent) = Unit
    override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }


    override fun onScroll(
        event: MotionEvent?,
        event1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {

        if (event == null) return false

        val sWidth = Resources.getSystem().displayMetrics.widthPixels
        val sHeight = Resources.getSystem().displayMetrics.heightPixels

        // Define the center "dead zone" (e.g., middle 50%)
        val deadZoneLeft = sWidth * 0.25f
        val deadZoneRight = sWidth * 0.75f
        val deadZoneTop = sHeight * 0.25f
        val deadZoneBottom = sHeight * 0.99f



        //
        // Ignore scrolls that start in the center of the screen
        if (event1.x > deadZoneLeft && event1.x < deadZoneRight &&
            event1.y > deadZoneTop && event1.y < deadZoneBottom
        ) {
            return false
        }

        if (event1.y < IGNORE_BORDER || event1.x < IGNORE_BORDER ||
            event1.y > sHeight - IGNORE_BORDER || event1.x > sWidth - IGNORE_BORDER
        ) return false

        if (event.pointerCount >= 2 || event1.pointerCount >= 2) {
            return false // Ignore multi-touch gestures for this logic
        }

        if (control != ControlsMode.FULLSCREEN) return false

        // Step 1: Determine and "lock in" the scroll mode for the gesture
        if (scrollMode == ScrollMode.NONE) {
            // Use a threshold to distinguish between a tap and a scroll
            if (abs(distanceX) > abs(distanceY)) {
                scrollMode = ScrollMode.SEEK
            } else {
                scrollMode = if (event.x < sWidth / 2) ScrollMode.BRIGHTNESS else ScrollMode.VOLUME
            }
        }

        // Step 2: Execute logic based on the locked-in mode
        when (scrollMode) {

            ScrollMode.SEEK -> {
                if (control == ControlsMode.FULLSCREEN) {


                    seekStart = player.currentPosition
                    seekChange = 0L
                    seekMax = player.duration
                    // Exclude edge areas

                    if (gestureScrollY == 0f || gestureScrollX == 0f) {
                        gestureScrollY = 0.0001f
                        gestureScrollX = 0.0001f
                        return false
                    }

                    gestureScrollX += distanceX
                    if (abs(gestureScrollX) > SCROLL_STEP && abs(
                            gestureScrollX
                        ) > SCROLL_STEP_SEEK
                    ) {
                        // Do not show controller if not already visible

                        var position: Long = 0
                        val distanceDiff =
                            0.5f.coerceAtLeast(abs(pxToDp(distanceX) / 4).coerceAtMost(10f))
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
                            var message: String = formatMilesSign(seekChange)

                            message += """  ${formatMiles(position)}    """.trimIndent()
                            binding.curDuration.visibility = View.VISIBLE

                            binding.curDuration.text = message
                            Handler().postDelayed({
                                binding.curDuration.visibility = View.GONE
                            }, 600)
                            gestureScrollX = 0.0001f
                        }
                    }

                }
            }

            ScrollMode.BRIGHTNESS -> {
                val verticalScrollDistance = event.y - event1.y
                val scrollSensitivity = 2.5f
                val maxBrightness = 30

                val brightnessChange = (verticalScrollDistance / sHeight) * maxBrightness * scrollSensitivity
                val newBrightness = (gestureInitialBrightness + brightnessChange)
                    .coerceIn(0f, maxBrightness.toFloat())
                    .toInt()

                if (newBrightness != brightnessInt) {
                    brightnessInt = newBrightness
                    setScreenBrightness(brightnessInt, this)
                }

                binding.brightnessIcon.text = brightnessInt.toString()
                binding.brightnessIcon.visibility = View.VISIBLE
                binding.volumeIcon.visibility = View.GONE
            }
            ScrollMode.VOLUME -> {
                val verticalScrollDistance = event.y - event1.y
                val scrollSensitivity = 2.5f
                val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                val volumeChange = (verticalScrollDistance / sHeight) * maxVolume * scrollSensitivity
                val newVolume = (gestureInitialVolume + volumeChange)
                    .coerceIn(0f, maxVolume.toFloat())
                    .toInt()

                if (newVolume != audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                }

                binding.volumeIcon.text = newVolume.toString()
                binding.volumeIcon.visibility = View.VISIBLE
                binding.brightnessIcon.visibility = View.GONE
            }
            // Do nothing for ScrollMode.NONE
            else -> {}
        }

        return true




    }




    override fun onScale(detector: ScaleGestureDetector): Boolean {

        if (control == ControlsMode.FULLSCREEN) {
            if (canScale) {
                val factor = scaleGestureDetector.scaleFactor
                mScaleFactor *= factor + (1 - factor) / 3 * 2
                mScaleFactor = normalizeScaleFactor(mScaleFactor, mScaleFactorFit)
                setScale(mScaleFactor)
                restoreSurfaceView()

                playerView.setCustomErrorMessage((mScaleFactor * 100).toInt().toString() + "%")
                return true
            }
        }

        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {


        /* mScaleFactor = playerView.videoSurfaceView!!.scaleX
         if (playerView.resizeMode != AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
             canScale = false
             playerView.setAspectRatioListener { _: Float, _: Float, _: Boolean ->
                 playerView.setAspectRatioListener(null)
                 mScaleFactorFit = getScaleFit()
                 mScaleFactor = mScaleFactorFit
                 canScale = true
             }
             playerView.videoSurfaceView!!.alpha = 0f
             playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
         } else {
             mScaleFactorFit = getScaleFit()
             canScale = true
         }*/
        return true
    }


    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (control == ControlsMode.FULLSCREEN) {
            if (mScaleFactor - mScaleFactorFit < 0.001) {
                setScale(1f)
                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT

            }
            playerView.setCustomErrorMessage(null)
            restoreSurfaceView()
        }
    }

    companion object {
        fun setScreenBrightness(value: Int, activity: Activity) {

            val percent = value.coerceIn(0, 30) / 30f
            val lp = activity.window.attributes
            lp.screenBrightness = percent
            activity.window.attributes = lp
        }
        var pos by Delegates.notNull<Int>()
        var repeat = false
        private var gestureInitialVolume: Int = 0
        // You can do the same for brightness
        private var gestureInitialBrightness: Int = 0
        const val NOTIFICATION_ID = 1 // Or any unique integer
        const val NOTIFICATION_CHANNEL_ID = "video_playback_channel"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

        var brightnessInt: Int = 0
        lateinit var player: ExoPlayer

    }
    private enum class ScrollMode {
        NONE,
        SEEK,
        VOLUME,
        BRIGHTNESS
    }
    private var scrollMode = ScrollMode.NONE
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: androidx.media3.session.MediaSession? = null
}