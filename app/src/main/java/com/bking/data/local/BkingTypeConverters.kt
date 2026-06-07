package com.bking.data.local

import androidx.room.TypeConverter
import java.time.Instant

class BkingTypeConverters {
    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}

