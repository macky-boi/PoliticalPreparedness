package com.example.android.politicalpreparedness.election.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateJsonAdapter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @ToJson
    private fun toJson(date: Date): String {
        return dateFormat.format(date)
    }

    @FromJson
    fun fromJson(dateString: String): Date {
        return dateFormat.parse(dateString) ?: throw IllegalArgumentException("Invalid date format")
    }
}