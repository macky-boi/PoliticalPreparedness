package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import java.util.Date



val jsonAdapter = moshi.adapter(Date::class.java)
val electionResponseSample = Election(
        id = 2000,
        name = "VIP Test Election",
        electionDay = jsonAdapter.fromJson("2025-06-06")!!,
        division = Division(id = "ocd-division/country:us/state:il", country = "us", state = "il"))


// JSON sample
//{
//    "elections": [
//    {
//        "id": "2000",
//        "name": "VIP Test Election",
//        "electionDay": "2025-06-06",
//        "ocdDivisionId": "ocd-division/country:us"
//    },
//    {
//        "id": "9045",
//        "name": "Illinois Consolidated Primary Election",
//        "electionDay": "2025-02-25",
//        "ocdDivisionId": "ocd-division/country:us/state:il"
//    },
//    {
//        "id": "9156",
//        "name": "California Special Primary Election",
//        "electionDay": "2025-02-25",
//        "ocdDivisionId": "ocd-division/country:us/state:ca"
//    }
//    ],
//    "kind": "civicinfo#electionsQueryResponse"
//}

