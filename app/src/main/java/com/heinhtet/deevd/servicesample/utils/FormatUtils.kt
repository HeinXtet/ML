package com.heinhtet.deevd.servicesample.utils

/**
 * Created by Hein Htet on 9/23/18.
 */
object FormatUtils{


    fun formatMusicTime(duration: Long): String {
        var time = ""
        val minute = duration / 60000
        val seconds = duration % 60000
        val second = Math.round((seconds.toInt() / 1000).toFloat()).toLong()
        if (minute < 10) {
            time += "0"
        }
        time += minute.toString() + ":"
        if (second < 10) {
            time += "0"
        }
        time += second
        return time
    }
}