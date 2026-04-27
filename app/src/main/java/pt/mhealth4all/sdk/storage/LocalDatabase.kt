package pt.mhealth4all.sdk.storage

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SupportFactory
import pt.mhealth4all.sdk.security.EncryptionManager

@Database(entities = [ResponseEntity::class], version = 2, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun responseDao(): ResponseDao

    companion object {
        @Volatile private var instance: LocalDatabase? = null

        // Migração da versão 1 para a 2 (adiciona coluna fhirJson)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE responses ADD COLUMN fhirJson TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context, encryptionEnabled: Boolean = true): LocalDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context, encryptionEnabled).also { instance = it }
            }

        private fun buildDatabase(context: Context, encryptionEnabled: Boolean): LocalDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java,
                "mhealth4all_db"
            ).addMigrations(MIGRATION_1_2)

            if (encryptionEnabled) {
                val passphrase = EncryptionManager.getDatabasePassphrase()
                val factory = SupportFactory(passphrase)
                builder.openHelperFactory(factory)
            }

            return builder.build()
        }
    }
}