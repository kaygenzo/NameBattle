package com.telen.namebattle.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "first_names",
    indices = [
        Index("first_letter"),
        Index("name_lower"),
        Index("gender")
    ]
)
data class FirstNameEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "name_lower") val nameLower: String,
    @ColumnInfo(name = "first_letter") val firstLetter: String,     // single uppercase char
    @ColumnInfo(name = "gender") val gender: String,           // "BOY", "GIRL"
    @ColumnInfo(name = "births_1900") val births1900: Int,          // total since 1900
    @ColumnInfo(name = "births_1980") val births1980: Int,
    @ColumnInfo(name = "births_2000") val births2000: Int,
    @ColumnInfo(name = "births_2010") val births2010: Int,
    @ColumnInfo(name = "total_births") val totalBirths: Int = 0,     // INSEE total (since 1900)
    @ColumnInfo(name = "peak_year") val peakYear: Int = 0,        // year of max births
    @ColumnInfo(name = "first_year") val firstYear: Int = 0,       // first year recorded
    @ColumnInfo(name = "origin") val origin: String? = null,   // etymology, from Firebase
    @ColumnInfo(name = "meaning") val meaning: String? = null,
    @ColumnInfo(name = "has_meaning") val hasMeaning: Boolean = false,
    // user-typed free spelling, hidden from search/top
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false
)
