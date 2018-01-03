package com.kapplication.launcher.behavior.player

/**
 * Created by hy on 2015/11/3.
 */
interface SeekableHost {
    fun onSeekPositionChanged(currentSeekPos: Float)

    fun onSeekRewindChanged(seeking: Boolean)

    fun onSeekFastForwardChanged(seeking: Boolean)
}
