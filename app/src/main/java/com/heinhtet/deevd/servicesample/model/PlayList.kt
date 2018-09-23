package com.heinhtet.deevd.serviceeample.model

import java.util.ArrayList

/**
 * Created by Hein Htet on 5/5/18.
 */
class PlayList(val id: Long, val name: String) {

    private val songs = ArrayList<Long>()

    /**
     * Returns a list with all the songs inside this Playlist.
     * @return
     */
    val songIds: ArrayList<Long>
        get() {
            val list = ArrayList<Long>()

            for (songID in songs)
                list.add(songID)

            return list
        }

    /**
     * Inserts a song on this Playlist.
     *
     * @param id Global song id.
     */
    fun add(id: Long) {
        if (!songs.contains(id))
            songs.add(id)
    }
}