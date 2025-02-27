package com.example.android.politicalpreparedness.network.models

import androidx.room.*
import com.example.android.politicalpreparedness.election.models.ElectionUi
import com.squareup.moshi.*
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
@Entity(tableName = "election_table")
data class Election(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name")val name: String,
        @ColumnInfo(name = "electionDay")val electionDay: Date,
        @Embedded(prefix = "division_") @Json(name="ocdDivisionId") val division: Division
)

fun Election.toUiModel(): ElectionUi {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US) // Example: June 06, 2025
        return ElectionUi(
                id = id.toString(),
                name = name,
                electionDay = dateFormat.format(electionDay),
                country = division.country.uppercase(),
                state = division.state.uppercase()
        )
}