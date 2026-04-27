package pt.mhealth4all.sdk.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "responses")
data class ResponseEntity(
    @PrimaryKey val id: String,
    val questionnaireId: String,
    val patientId: String,
    val answersJson: String,
    val fhirJson: String,       // ← JSON FHIR pronto a enviar
    val timestamp: Long,
    val synced: Boolean = false
)