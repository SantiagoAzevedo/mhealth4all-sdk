package pt.mhealth4all.sdk

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.mhealth4all.sdk.fhir.FhirMapper
import pt.mhealth4all.sdk.models.Question
import pt.mhealth4all.sdk.models.QuestionType
import pt.mhealth4all.sdk.models.QuestionnaireResponse
import pt.mhealth4all.sdk.storage.LocalDatabase
import pt.mhealth4all.sdk.storage.ResponseEntity
import pt.mhealth4all.sdk.sync.FhirSyncService
import java.util.UUID

class MHealth4All private constructor(
    private val context: Context,
    private val serverUrl: String,
    private val encryptionEnabled: Boolean
) {
    private val db = LocalDatabase.getInstance(context, encryptionEnabled)
    private val syncService = FhirSyncService(serverUrl)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun isEncrypted(): Boolean = encryptionEnabled

    // Carrega um questionário pelo ID
    fun loadQuestionnaire(id: String): List<Question> {
        return when (id) {
            "eq5d-5l" -> listOf(
                Question("mobility", "Tens problemas a andar?", QuestionType.YES_NO),
                Question("pain", "Nível de dor (1-10):", QuestionType.SCALE),
                Question("notes", "Observações:", QuestionType.TEXT)
            )
            else -> emptyList()
        }
    }

    // Guarda respostas localmente e converte para FHIR
    fun submitResponse(
        questionnaireId: String,
        patientId: String,
        answers: Map<String, String>,
        onSuccess: (fhirJson: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        scope.launch {
            try {
                val response = QuestionnaireResponse(
                    questionnaireId = questionnaireId,
                    patientId = patientId,
                    answers = answers
                )

                // Converte para FHIR
                val fhirJson = FhirMapper.toFhirJson(response)

                // Guarda localmente com o JSON FHIR já gerado
                db.responseDao().insert(
                    ResponseEntity(
                        id = UUID.randomUUID().toString(),
                        questionnaireId = response.questionnaireId,
                        patientId = response.patientId,
                        answersJson = answers.toString(),
                        fhirJson = fhirJson,
                        timestamp = response.timestamp,
                        synced = false
                    )
                )

                onSuccess(fhirJson)

            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // Sincroniza respostas pendentes com o servidor FHIR real
    fun syncPending(onComplete: (syncedCount: Int) -> Unit) {
        scope.launch {
            val pending = db.responseDao().getPending()
            var count = 0

            pending.forEach { entity ->
                // POST real para o servidor FHIR
                val success = syncService.postResource(
                    resourceType = "QuestionnaireResponse",
                    fhirJson = entity.fhirJson
                )

                if (success) {
                    db.responseDao().markSynced(entity.id)
                    count++
                } else {
                    // Mantém synced = false para tentar novamente
                    db.responseDao().markFailed(entity.id)
                }
            }

            onComplete(count)
        }
    }

    class Builder(private val context: Context) {
        private var serverUrl: String = ""
        private var encryptionEnabled: Boolean = true

        fun serverUrl(url: String) = apply { this.serverUrl = url }
        fun enableEncryption(enabled: Boolean) = apply { this.encryptionEnabled = enabled }

        fun build() = MHealth4All(context, serverUrl, encryptionEnabled)
    }
}