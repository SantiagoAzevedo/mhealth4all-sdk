package pt.mhealth4all.sdk.storage

import android.content.Context
import androidx.room.*
import net.sqlcipher.database.SupportFactory
import pt.mhealth4all.sdk.security.EncryptionManager

@Database(entities = [ResponseEntity::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun responseDao(): ResponseDao

    companion object {
        @Volatile private var instance: LocalDatabase? = null

        fun getInstance(context: Context, encryptionEnabled: Boolean = true): LocalDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context, encryptionEnabled).also { instance = it }
            }

        private fun buildDatabase(context: Context, encryptionEnabled: Boolean): LocalDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java,
                "mhealth4all_db"
            )

            if (encryptionEnabled) {
                val passphrase = EncryptionManager.getDatabasePassphrase()
                val factory = SupportFactory(passphrase)
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }
    }
}