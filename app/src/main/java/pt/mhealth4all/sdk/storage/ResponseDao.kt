package pt.mhealth4all.sdk.storage

import androidx.room.*

@Dao
interface ResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(response: ResponseEntity)

    @Query("SELECT * FROM responses WHERE synced = 0")
    suspend fun getPending(): List<ResponseEntity>

    @Query("UPDATE responses SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM responses")
    suspend fun getAll(): List<ResponseEntity>
}