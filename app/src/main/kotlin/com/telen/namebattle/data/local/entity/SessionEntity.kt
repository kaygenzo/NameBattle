package com.telen.namebattle.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "battle_state_json") val battleStateJson: String? = null
)
