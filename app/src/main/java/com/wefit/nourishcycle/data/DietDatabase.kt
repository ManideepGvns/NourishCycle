package com.wefit.nourishcycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [CompletionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DietDatabase : RoomDatabase() {
    abstract fun dietDao(): DietDao

    companion object {
        @Volatile
        private var INSTANCE: DietDatabase? = null

        /**
         * Lazy singleton — initialization deferred to first access.
         * Room.databaseBuilder runs on whatever coroutine context calls this;
         * callers must ensure they call from Dispatchers.IO (done in ViewModel).
         */
        fun getInstance(context: Context): DietDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DietDatabase::class.java,
                    "nourishcycle_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
