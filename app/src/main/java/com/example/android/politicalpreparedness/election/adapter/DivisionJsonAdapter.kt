package com.example.android.politicalpreparedness.election.adapter

import com.example.android.politicalpreparedness.network.models.Division
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class DivisionJsonAdapter {

    @ToJson
    fun toJson(division: Division): String {
        return division.id
    }

    @FromJson
    fun fromJson(ocdId: String): Division {
        val components = ocdId.split("/")
        val map = components.drop(1) // Drop the 'ocd-division' prefix
            .associate {
                val (key, value) = it.split(":")
                key to value
            }
        return Division(
            id = ocdId,
            country = map["country"] ?: "",
            state = map["state"] ?: ""
        )
    }
}