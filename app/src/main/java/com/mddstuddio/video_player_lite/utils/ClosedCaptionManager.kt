package com.mddstuddio.video_player_lite.utils

import com.google.android.exoplayer2.source.TrackGroup

import com.google.android.exoplayer2.C

import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo

import com.google.android.exoplayer2.source.TrackGroupArray

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.*
import java.util.*
import kotlin.collections.ArrayList


class ClosedCaptionManager {

    fun ClosedCaptionManager(
        mappingTrackSelector: MappingTrackSelector?,
        player: SimpleExoPlayer?
    ) {
        this.player = player
        trackSelector = mappingTrackSelector
    }

    var player: SimpleExoPlayer? = null
    var trackSelector: MappingTrackSelector? = null

    // These two could be fields OR passed around
    var textTrackIndex = 0
    var trackGroups: TrackGroupArray? = null

    var pairTrackList: ArrayList<Pair<Int, Int>> = ArrayList()

    private fun checkAndSetClosedCaptions(): Boolean {
        // This is the body of the logic  for see if there are even video tracks
        // It also does some field setting
        val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo ?: return false
        for (i in 0 until mappedTrackInfo.rendererCount) {
            trackGroups = mappedTrackInfo.getTrackGroups(i)
            if (trackGroups!!.length != 0) {
                when (player!!.getRendererType(i)) {
                    C.TRACK_TYPE_TEXT -> {
                        textTrackIndex = i
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun buildTrackList() {
        // This next part is actually about getting the list.
        // Below you'd be building up items in a list. This just does
        // views directly, but you could just have a list of track names (with indexes)
        for (groupIndex in 0 until trackGroups!!.length) {
            val group = trackGroups!![groupIndex]
            for (trackIndex in 0 until group.length) {
                if (trackIndex == 0) {
                    // Beginning of a new set, the demo app adds a divider
                }
                //CheckedTextView trackView = ...; // The TextView to show in the list
                // The below points to a util which extracts the quality from the TrackGroup
                //trackView.setText(DemoUtil.buildTrackName(group.getFormat(trackIndex)));
             //   Log.e("Thing", DemoUtil.buildTrackName(group.getFormat(trackIndex)))
                pairTrackList.add(Pair(groupIndex, trackIndex))
            }
        }
    }

  /*  private fun onTrackViewClick(trackPair: Pair<Int, Int>) {
        // Assuming you tagged the view with the groupIndex and trackIndex, you
        // can build your override with that info.
        val (groupIndex, trackIndex) = trackPair
        // This is the override you'd use for something that isn't adaptive.
        // `override = new SelectionOverride(FIXED_FACTORY, groupIndex, trackIndex);`
        // Otherwise they call their helper for adaptives (HLS/DASH), which roughly does:
        val tracks = getTracksAdding(
            DefaultTrackSelector.SelectionOverride(
                ExoTrackSelection.Factory, groupIndex, trackIndex
            ),
            trackIndex
        )
        val factory: TrackSelection.Factory =
            if (tracks.size == 1) ExoTrackSelection.Factory() else AdaptiveTrackSelection.Factory(
                BANDWIDTH_METER
            )
        val override = DefaultTrackSelector.SelectionOverride(factory, groupIndex, tracks)

        // Then we actually set our override on the selector to switch the text track
        trackSelector.setSelectionOverride(textTrackIndex, trackGroups, override)
    }*/

    private fun getTracksAdding(override: DefaultTrackSelector.SelectionOverride, addedTrack: Int): IntArray {
        var tracks: IntArray = override.tracks
        tracks = Arrays.copyOf(tracks, tracks.size + 1)
        tracks[tracks.size - 1] = addedTrack
        return tracks
    }
}