package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.JsonClass
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class Address (
        val line1: String,
        val line2: String? = null,
        val city: String,
        val state: String,
        val zip: String
) {
    fun toFormattedString(): String {
        var output = line1.plus("\n")
        if (!line2.isNullOrEmpty()) output = output.plus(line2).plus("\n")
        output = output.plus("$city, $state $zip")
        return output
    }
    fun toApiServiceString(): String {
        return listOf(line1, line2, city, state, zip)
            .filterNot {
                it.isNullOrBlank()
            }
            .joinToString(",")
    }
}