package com.shyptsolution.branchinternational

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Utils {
    fun notify(msg:String,context:Context){
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show()
    }

    fun convertToBeautifulDateFormat(dateTimeString: String): String {
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val dff = SimpleDateFormat("dd-MMM-yyyy, hh:mm aa")
        format.timeZone = TimeZone.getTimeZone("UTC")
        return dff.format(format.parse(dateTimeString)!!)
    }

    fun getDateOnly(dateTime: String):String{
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val dff = SimpleDateFormat("dd-MMM")
        format.timeZone = TimeZone.getTimeZone("UTC")
        return dff.format(format.parse(dateTime)!!)
    }

    fun getTimeOnly(dateTime: String):String{
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val dff = SimpleDateFormat("hh:mm aa")
        format.timeZone = TimeZone.getTimeZone("UTC")
        return dff.format(format.parse(dateTime)!!)
    }

    fun convertDateTimeToMilliseconds(dateTime: String): Long {
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val utcDateTime = utcFormat.parse(dateTime)
        return utcDateTime!!.time
    }
}