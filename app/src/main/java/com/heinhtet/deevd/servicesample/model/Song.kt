package com.heinhtet.deevd.miscy.model

import android.net.Uri

/**
 * Created by Hein Htet on 5/5/18.
 */

/**
 * Represents a single audio file on the Android system.
 *
 * It's a simple data container, filled with setters/getters.
 *
 * Only mandatory fields are:
 * - id (which is a unique Android identified for a media file
 * anywhere on the system)
 * - filePath (full path for the file on the filesystem).
 */
data class Song
/**
 * Creates a new Song, with specified `songID` and `filePath`.
 *
 * @note It's a unique Android identifier for a media file
 * anywhere on the system.
 */
(
        /**
         * Identifier for the song on the Android system.
         * (so we can locate the file anywhere)
         */
        val id: Long,
        /**
         * Full path for the music file within the filesystem.
         */
        val filePath: String) {

    // optional metadata

    var title: String = ""
    var artist = ""
    var album = ""
    var year = -1
    var genre: String? = ""
    var trackNumber = -1
    var alblumId: Long = 0
    var artwork: Uri? = null

    /**
     * Returns the duration of the song, in miliseconds.
     */
    /**
     * Sets the duration of the song, in miliseconds.
     */
    var duration: Long = -1
    val durationSeconds: Long
        get() = duration / 1000
    val durationMinutes: Long
        get() = durationSeconds / 60
}
