package com.telen.namebattle.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.telen.namebattle.data.local.dao.FirstNameDao
import com.telen.namebattle.data.local.dao.SessionDao
import com.telen.namebattle.data.local.entity.FirstNameEntity
import com.telen.namebattle.data.local.entity.ParentEntity
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.data.local.entity.ShortlistEntryEntity

@Database(
    entities = [
        FirstNameEntity::class,
        SessionEntity::class,
        ParentEntity::class,
        ShortlistEntryEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class NameBattleDatabase : RoomDatabase() {

    abstract fun firstNameDao(): FirstNameDao
    abstract fun sessionDao(): SessionDao

    companion object {
        fun create(context: Context): NameBattleDatabase =
            Room.databaseBuilder(context, NameBattleDatabase::class.java, "namebattle.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
