package com.heinhtet.deevd.miscy.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Created by Hein Htet on 5/11/18.
 */

data class AlbumModel(var albumId: Long, var albumName: String, var artistName: String, var albumImg: String?, var noOfSong: Int)