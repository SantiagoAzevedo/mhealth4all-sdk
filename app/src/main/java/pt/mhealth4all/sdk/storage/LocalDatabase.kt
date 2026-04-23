package pt.mhealth4all.sdk.storage

import android.content.Context
import androidx.room.*

@Database(entities = [ResponseEntity::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun responseDao(): ResponseDao

    companion object {
        @Volatile private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "mhealth4all_db"
                ).build().also { instance = it }
            }
    }
}