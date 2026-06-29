package com.telen.namebattle.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "shortlist_entries",
    primaryKeys = ["parent_id", "first_name_id"],
    foreignKeys = [
        ForeignKey(
            entity = ParentEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FirstNameEntity::class,
            parentColumns = ["id"],
            childColumns = ["first_name_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parent_id"), Index("first_name_id")]
)
data class ShortlistEntryEntity(
    @ColumnInfo(name = "parent_id") val parentId: Long,
    @ColumnInfo(name = "first_name_id") val firstNameId: Long
)
