package com.heinhtet.deevd.serviceeample.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.heinhtet.deevd.miscy.model.Song
import java.util.*
import com.heinhtet.deevd.miscy.model.AlbumModel
import com.heinhtet.deevd.miscy.model.ArtistModel
import kotlin.collections.ArrayList


/**
 * Created by Hein Htet on 5/5/18.
 */

/**
 * Global interface to all the songs this application can see.
 *
 *
 * Tasks:
 * - Scans for songs on the device
 * (both internal and external memories)
 * - Has query functions to songs and their attributes.
 *
 *
 * Thanks:
 *
 *
 * - Showing me how to get a music's full PATH:
 * http://stackoverflow.com/a/21333187
 *
 *
 * - Teaching me the queries to get Playlists
 * and their songs:
 * http://stackoverflow.com/q/11292125
 */
class SongHelper {


    val TAG = "SongHelper"
    /**
     * Big list with all the Songs found.
     */
    var song = ArrayList<Song>()

    /**
     * Big list with all the Playlists found.
     */
    var playlists = ArrayList<PlayList>()

    var artist = ArrayList<ArtistModel>()

    /**
     * Maps song's genre IDs to song's genre names.
     *
     * @note It's only available after calling `scanSongs`.
     */
    private var genreIdToGenreNameMap: HashMap<String, String>? = null

    /**
     * Maps song's IDs to song genre IDs.
     *
     * @note It's only available after calling `scanSongs`.
     */
    private var songIdToGenreIdMap: HashMap<String, String>? = null

    /**
     * Flag that tells if successfully scanned all songs.
     */
    /**
     * Tells if we've successfully scanned all songs on
     * the device.
     *
     *
     * This will return `false` both while we're scanning
     * for songs and if some error happened while scanning.
     */
    var isInitialized: Boolean = false
        private set

    /**
     * Flag that tells if we're scanning songs right now.
     */
    /**
     * Tells if we're currently scanning songs on the device.
     */
    var isScanning: Boolean = false
        private set

    /**
     * Returns an alphabetically sorted list with all the
     * artists of the scanned songs.
     *
     * @note This method might take a while depending on how
     * many songs you have.
     */
    // Making them alphabetically sorted
    val artists: ArrayList<String>
        get() {
            val artists = ArrayList<String>()

            for (song in song) {
                val artist = song.artist

                if (artist != null && !artists.contains(artist))
                    artists.add(artist)
            }
            artists.sort()
            return artists
        }

    fun getArtist(): List<ArtistModel> {
        var artists = ArrayList<ArtistModel>()
        for (song in song) {
            var artist = ArtistModel(song.alblumId, song.artist, song.trackNumber.toString(), song.album)
            if (artists != null && !artists.contains(artist))
                artists.add(artist)
        }
        artists.distinct()
        return artists
    }

    /**
     * Returns an alphabetically sorted list with all the
     * albums of the scanned songs.
     *
     * @note This method might take a while depending on how
     * many songs you have.
     */
    // Making them alphabetically sorted
    val albums: ArrayList<String>
        get() {

            val albums = ArrayList<String>()

            for (song in song) {
                val album = song.album

                if (album != null && !albums.contains(album))
                    albums.add(album)
            }
            Collections.sort(albums)

            return albums
        }

    /**
     * Returns an alphabetically sorted list with all
     * existing genres on the scanned songs.
     */
    val genres: ArrayList<String>
        get() {

            val genres = ArrayList<String>()

            for (genre in genreIdToGenreNameMap!!.values)
                genres.add(genre)

            Collections.sort(genres)

            return genres
        }

    /**
     * Returns a list with all years your songs have.
     *
     * @note It is a list of Strings. To access the
     * years, do a `Integer.parseInt(string)`.
     */
    // Making them alphabetically sorted
    val years: ArrayList<String>
        get() {

            val years = ArrayList<String>()

            for (song in song) {
                val year = Integer.toString(song.year)

                if (Integer.parseInt(year) > 0 && !years.contains(year))
                    years.add(year)
            }
            Collections.sort(years)

            return years
        }

    val playlistNames: ArrayList<String>
        get() {

            val names = ArrayList<String>()

            for (playlist in playlists)
                names.add(playlist.name)

            return names
        }

    /**
     * Scans the device for songs.
     *
     *
     * This function takes a lot of time to execute and
     * blocks the program UI.
     * So you should call it on a separate thread and
     * query `isInitialized` when needed.
     *
     *
     * Inside it, we make a lot of queries to the system's
     * databases - getting songs, genres and playlists.
     *
     * @param c         The current Activity's Context.
     * @param fromWhere Where should we scan for songs.
     *
     *
     * Accepted values to `fromWhere` are:
     * - "internal" To scan for songs on the phone's memory.
     * - "external" To scan for songs on the SD card.
     * - "both"     To scan for songs anywhere.
     * @note If you call this function twice, it rescans
     * the songs, refreshing internal lists.
     * It doesn't add up songs.
     */
    fun scanSongs(c: Context, fromWhere: String) {

        // This is a rather complex function that interacts with
        // the underlying Android database.
        // Grab some coffee and stick to the comments.

        // Not implemented yet.
        if (fromWhere === "both")
            throw RuntimeException("Can't scan from both locations - not implemented")

        // Checking for flags so we don't get called twice
        // Fucking Java that doesn't allow local static variables.
        if (isScanning)
            return
        isScanning = true

        // The URIs that tells where we should scan for files.
        // There are separate URIs for music, genres and playlists. Go figure...
        //
        // Remember - internal is the phone memory, external is for the SD card.
        val musicUri = if (fromWhere === "internal")
            android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        else
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val genreUri = if (fromWhere === "internal")
            android.provider.MediaStore.Audio.Genres.INTERNAL_CONTENT_URI
        else
            android.provider.MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val playlistUri = if (fromWhere === "internal")
            android.provider.MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI
        else
            android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI

        // Gives us access to query for files on the system.
        val resolver = c.contentResolver

        // We use this thing to iterate through the results
        // of a SQLite database query.
        var cursor: Cursor?

        // OK, this is where we start.
        //
        // First, before even touching the songs, we'll save all the
        // music genres (like "Rock", "Jazz" and such).
        // That's because Android doesn't allow getting a song genre
        // from the song file itself.
        //
        // To get the genres, we make queries to the system's SQLite
        // database. It involves genre IDs, music IDs and such.
        //
        // We're creating two maps:
        //
        // 1. Genre ID -> Genre Names
        // 2. Song ID -> Genre ID
        //
        // This way, we have a connection from a Song ID to a Genre Name.
        //
        // Then we finally get the songs!
        // We make queries to the database, getting all possible song
        // metadata - like artist, album and such.


        // These are the columns from the system databases.
        // They're the information I want to get from songs.
        val GENRE_ID = MediaStore.Audio.Genres._ID
        val GENRE_NAME = MediaStore.Audio.Genres.NAME
        val SONG_ID = MediaStore.Audio.Media._ID
        val SONG_TITLE = MediaStore.Audio.Media.TITLE
        val SONG_ARTIST = MediaStore.Audio.Media.ARTIST
        val SONG_ALBUM = MediaStore.Audio.Media.ALBUM
        val SONG_YEAR = MediaStore.Audio.Media.YEAR
        val SONG_TRACK_NO = MediaStore.Audio.Media.TRACK
        val SONG_FILEPATH = MediaStore.Audio.Media.DATA
        val SONG_DURATION = MediaStore.Audio.Media.DURATION
        val SONG_ALBLUMID = MediaStore.Audio.Media.ALBUM_ID

        // Creating the map  "Genre IDs" -> "Genre Names"
        genreIdToGenreNameMap = HashMap()

        // This is what we'll ask of the genres
        val genreColumns = arrayOf(GENRE_ID, GENRE_NAME)

        // Actually querying the genres database
        cursor = resolver.query(genreUri, genreColumns, null, null, null)

        // Iterating through the results and filling the map.
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            genreIdToGenreNameMap!![cursor.getString(0)] = cursor.getString(1)
            cursor.moveToNext()
        }

        cursor.close()

        // Map from Songs IDs to Genre IDs
        songIdToGenreIdMap = HashMap()

        // UPDATE URI HERE
        if (fromWhere === "both")
            throw RuntimeException("Can't scan from both locations - not implemented")

        // For each genre, we'll query the databases to get
        // all songs's IDs that have it as a genre.
        for (genreID in genreIdToGenreNameMap!!.keys) {

            val uri = MediaStore.Audio.Genres.Members.getContentUri(fromWhere,
                    java.lang.Long.parseLong(genreID))

            cursor = resolver.query(uri, arrayOf(SONG_ID), null, null, null)

            // Iterating through the results, populating the map
            cursor!!.moveToFirst()
            while (!cursor.isAfterLast) {

                val currentSongID = cursor.getLong(cursor.getColumnIndex(SONG_ID))

                songIdToGenreIdMap!![java.lang.Long.toString(currentSongID)] = genreID
                cursor.moveToNext()
            }
            cursor.close()
        }

        // Finished getting the Genres.
        // Let's go get dem songzz.

        // Columns I'll retrieve from the song table
        val columns = arrayOf(SONG_ID, SONG_TITLE, SONG_ARTIST, SONG_ALBUM, SONG_YEAR, SONG_TRACK_NO, SONG_FILEPATH, SONG_DURATION, SONG_ALBLUMID)

        // Thing that limits results to only show music files.
        //
        // It's a SQL "WHERE" clause - it becomes `WHERE IS_MUSIC=1`.
        //
        // (note: using `IS_MUSIC!=0` takes a fuckload of time)
        val musicsOnly = MediaStore.Audio.Media.IS_MUSIC + "=1"

        // Actually querying the system
        cursor = resolver.query(musicUri, columns, musicsOnly, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            // NOTE: I tried to use MediaMetadataRetriever, but it was too slow.
            //       Even with 10 songs, it took like 13 seconds,
            //       No way I'm releasing it this way - I have like 4.260 songs!

            do {
                // Creating a song from the values on the row
                var s = Song(cursor.getLong(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_FILEPATH)))

                s.title = (cursor.getString(cursor.getColumnIndex(SONG_TITLE)) as String)
                s.artist = (cursor.getString(cursor.getColumnIndex(SONG_ARTIST)))
                s.album = (cursor.getString(cursor.getColumnIndex(SONG_ALBUM)))
                s.year = (cursor.getInt(cursor.getColumnIndex(SONG_YEAR)))
                s.trackNumber = (cursor.getInt(cursor.getColumnIndex(SONG_TRACK_NO)))
                s.duration = (cursor.getLong(cursor.getColumnIndex(SONG_DURATION)))
                s.alblumId = cursor.getLong(cursor.getColumnIndex(SONG_ALBLUMID))

                // Using the previously created genre maps
                // to fill the current song genre.
                val currentGenreID = songIdToGenreIdMap!![java.lang.Long.toString(s.id)]
                val currentGenreName = genreIdToGenreNameMap!![currentGenreID]
                s.genre = currentGenreName
                // Adding the song to the global list
                song.add(s)
            } while (cursor.moveToNext())
        } else {
            // What do I do if I can't find any songs?
        }
        cursor!!.close()

        // Alright, now I'll get all the Playlists.
        // First I grab all playlist IDs and Names and then for each
        // one of those, getting all songs inside them.

        // As you know, the columns for the database.
        val PLAYLIST_ID = MediaStore.Audio.Playlists._ID
        val PLAYLIST_NAME = MediaStore.Audio.Playlists.NAME
        val PLAYLIST_SONG_ID = MediaStore.Audio.Playlists.Members.AUDIO_ID

        // This is what I'll get for all playlists.
        val playlistColumns = arrayOf(PLAYLIST_ID, PLAYLIST_NAME)

        // The actual query - takes a while.
        cursor = resolver.query(playlistUri, playlistColumns, null, null, null)

        // Going through all playlists, creating my class and populating
        // it with all the song IDs they have.
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            val playlist = PlayList(cursor.getLong(cursor.getColumnIndex(PLAYLIST_ID)),
                    cursor.getString(cursor.getColumnIndex(PLAYLIST_NAME)))

            // For each playlist, get all song IDs
            val currentUri = MediaStore.Audio.Playlists.Members.getContentUri(fromWhere, playlist.id)

            val cursor2 = resolver.query(currentUri,
                    arrayOf(PLAYLIST_SONG_ID),
                    musicsOnly, null, null)

            // Adding each song's ID to it
            cursor2!!.moveToFirst()
            while (!cursor2.isAfterLast) {
                playlist.add(cursor2.getLong(cursor2.getColumnIndex(PLAYLIST_SONG_ID)))
                cursor2.moveToNext()
            }

            playlists.add(playlist)
            cursor2.close()
            cursor.moveToNext()
        }

        // Finally, let's sort the song list alphabetically
        // based on the song title.
        song.sortWith(Comparator { a, b -> a.title.compareTo(b.title) })

        isInitialized = true
        isScanning = false
    }

    fun destroy() {
        song.clear()
    }

    /**
     * Returns a list of Songs belonging to a specified artist.
     */
    fun getSongsByArtist(desiredArtist: String): ArrayList<Song> {
        val songsByArtist = ArrayList<Song>()

        for (song in song) {
            val currentArtist = song.artist

            if (currentArtist == desiredArtist)
                songsByArtist.add(song)
        }

        // Sorting resulting list by Album
        songsByArtist.sortWith(Comparator { a, b -> a.album.compareTo(b.album) })

        return songsByArtist
    }


    /**
     * Returns a list of album names belonging to a specified artist.
     */

    data class Albums(var alblumId: Long, var alblumName: String, var artist: String)

    fun getAlbumsByArtist(desiredArtist: String): ArrayList<Albums> {
        val albumsByArtist = ArrayList<Albums>()
        for (song in song) {
            val currentArtist = song.artist
            val currentAlbum = song.album
            val alblumId = song.alblumId
            if (desiredArtist == currentArtist) {
                if (!albumsByArtist.contains(Albums(alblumId, currentAlbum, currentArtist))) {
                    albumsByArtist.add(Albums(alblumId, currentAlbum, currentArtist))
                }
            }

        }

        // Sorting alphabetically
        albumsByArtist.sortWith(Comparator { a, b -> a.alblumName.compareTo(b.alblumName) })

        return albumsByArtist
    }

    /**
     * Returns a new list with all songs.
     *
     * @note This is different than accessing `songs` directly
     * because it duplicates it - you can then mess with
     * it without worrying about changing the original.
     */
    fun getSongs(): ArrayList<Song> {
        val list = ArrayList<Song>()

        for (song in song)
            list.add(song)
        return list
    }


//    fun filter(text: String, type: String) {
//        var searchTv = text
//        itemList.clear()
//        TYPE = type
//        if (text.isEmpty()) {
//            itemList.addAll(tempList)
//        } else {
//            searchTv = text.toLowerCase()
//            tempList.forEach {
//                if ( it.name.toLowerCase().contains(searchTv)) {
//                    itemList.add(it)
//                }
//            }
//        }
//        mCallback.getItemCount(itemList.size, TYPE)
//        notifyDataSetChanged()
//
//    }

    /**
     * Returns a list of Songs belonging to a specified album.
     */
    fun getSongsByAlbum(desiredAlbum: String): ArrayList<Song> {
        val songsByAlbum = ArrayList<Song>()

        for (song in song) {
            val currentAlbum = song.album

            if (currentAlbum == desiredAlbum)
                songsByAlbum.add(song)
        }

        return songsByAlbum
    }

    /**
     * Returns a list with all songs that have the same `genre.`
     */
    fun getSongsByGenre(genreName: String): ArrayList<Song> {

        val currentSongs = ArrayList<Song>()

        for (song in song) {

            val currentSongGenre = song.genre
            if (currentSongGenre === genreName)
                currentSongs.add(song)
        }

        return currentSongs
    }

    /**
     * Returns a list with all songs composed at `year`.
     */
    fun getSongsByYear(year: Int): ArrayList<Song> {

        val currentSongs = ArrayList<Song>()

        for (song in song) {

            val currentYear = song.year

            if (currentYear == year)
                currentSongs.add(song)
        }

        return currentSongs
    }

    fun getSongById(id: Long): Song? {

        var currentSong: Song? = null

        for (song in song)
            if (song.id === id) {
                currentSong = song
                break
            }

        return currentSong
    }

    fun getSongsByPlaylist(playlistName: String): ArrayList<Song> {

        var songIDs: ArrayList<Long>? = null

        for (playlist in playlists)
            if (playlist.name.equals(playlistName)) {
                songIDs = playlist.songIds
                break
            }

        val currentSongs = ArrayList<Song>()

        for (songID in songIDs!!)
            currentSongs.add(getSongById(songID)!!)

        return currentSongs
    }

    /**
     * Creates a new Playlist.
     *
     * @param c          Activity on which we're creating.
     * @param fromWhere  "internal" or "external".
     * @param name       Playlist name.
     * @param songsToAdd List of song IDs to place on it.
     */
    fun newPlaylist(c: Context, fromWhere: String, name: String, songsToAdd: ArrayList<Song>) {

        val resolver = c.contentResolver

        val playlistUri = if (fromWhere === "internal")
            android.provider.MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI
        else
            android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI

        // CHECK IF PLAYLIST EXISTS!

        // Setting the new playlists' values
        val values = ContentValues()
        values.put(MediaStore.Audio.Playlists.NAME, name)
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis())

        // Actually inserting the new playlist.
        val newPlaylistUri = resolver.insert(playlistUri, values)


        // Getting the new Playlist ID
        val PLAYLIST_ID = MediaStore.Audio.Playlists._ID
        val PLAYLIST_NAME = MediaStore.Audio.Playlists.NAME

        // This is what I'll get for all playlists.
        val playlistColumns = arrayOf(PLAYLIST_ID, PLAYLIST_NAME)

        // The actual query - takes a while.
        val cursor = resolver.query(playlistUri, playlistColumns, null, null, null)

        var playlistID: Long = 0

        // Going through all playlists, creating my class and populating
        // it with all the song IDs they have.
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            if (name == cursor.getString(cursor.getColumnIndex(PLAYLIST_NAME)))
                playlistID = cursor.getLong(cursor.getColumnIndex(PLAYLIST_ID))
            cursor.moveToNext()
        }

        // Now, to it's songs
        val songUri = Uri.withAppendedPath(newPlaylistUri, MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY)
        var songOrder = 1

        for (song in songsToAdd) {

            val songValues = ContentValues()

            songValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, song.id)
            songValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, songOrder)

            resolver.insert(songUri, songValues)
            songOrder++
        }

        // Finally, we're updating our internal list of Playlists
//        val newPlaylist = PlayList(playlistID, name)
//
//        for (song in songsToAdd)
//            newPlaylist.add(song.id)
//
//        playlists.add(newPlaylist)
    }

    fun getRealPathFromURI(contentUri: Uri, context: Context): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }


    fun convertDuration(duration: Long): String? {
        var out: String? = null
        var hours: Long = 0
        try {
            hours = duration / 3600000
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return out
        }

        val remaining_minutes = (duration - hours * 3600000) / 60000
        var minutes = remaining_minutes.toString()
        if (minutes == "0") {
            minutes = "00"
        }
        val remaining_seconds = duration - hours * 3600000 - remaining_minutes * 60000
        var seconds = remaining_seconds.toString()
        if (seconds.length < 2) {
            seconds = "00"
        } else {
            seconds = seconds.substring(0, 2)
        }

        if (hours > 0) {
            out = hours.toString() + ":" + minutes + ":" + seconds
        } else {
            out = "$minutes:$seconds"
        }

        return out

    }

    fun getAllArtist(context: Context): ArrayList<ArtistModel> {

        var artist = ArrayList<ArtistModel>()
        val mProjection = arrayOf(MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

        val artistCursor = context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                mProjection,
                null, null,
                MediaStore.Audio.Artists.ARTIST + " ASC")

        if (artistCursor != null && artistCursor.moveToFirst()) {
            do {
                var id = artistCursor.getLong(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID))
                var name = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
                var noOfTrack = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                var noOfAlblum = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                artist.add(ArtistModel(id, name, noOfTrack, noOfAlblum))

            } while (artistCursor.moveToNext())
        }

        return artist
    }


    fun getAlbumsLists(context: Context): ArrayList<AlbumModel> {
        val where: String? = null
        var albumList = ArrayList<AlbumModel>()
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val _id = MediaStore.Audio.Albums._ID
        val album_name = MediaStore.Audio.Albums.ALBUM
        val artist = MediaStore.Audio.Albums.ARTIST
        val albumart = MediaStore.Audio.Albums.ALBUM_ART
        val tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS
        val columns = arrayOf(_id, album_name, artist, albumart, tracks)
        val cursor = context.contentResolver.query(uri, columns, where, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(_id))
                val name = cursor.getString(cursor.getColumnIndex(album_name))
                val artist2 = cursor.getString(cursor.getColumnIndex(artist))
                val artPath = cursor.getString(cursor.getColumnIndex(albumart))
                val nr = Integer.parseInt(cursor.getString(cursor.getColumnIndex(tracks)))
                albumList.add(AlbumModel(id, name, artist2, artPath, nr))

            } while (cursor.moveToNext())
        }
        cursor!!.close()
        return albumList
    }
}