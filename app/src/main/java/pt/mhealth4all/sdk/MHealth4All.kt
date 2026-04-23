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
import java.util.UUID

class MHealth4All private constructor(
    private val context: Context,
    private val serverUrl: String
) {
    private val db = LocalDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

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

                // Guarda na base de dados local
                db.responseDao().insert(
                    ResponseEntity(
                        id = UUID.randomUUID().toString(),
                        questionnaireId = response.questionnaireId,
                        patientId = response.patientId,
                        answersJson = answers.toString(),
                        timestamp = response.timestamp,
                        synced = false
                    )
                )

                // Converte para FHIR
                val fhirJson = FhirMapper.toFhirJson(response)
                onSuccess(fhirJson)

            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // Sincroniza respostas pendentes com o servidor
    fun syncPending(onComplete: (syncedCount: Int) -> Unit) {
        scope.launch {
            val pending = db.responseDao().getPending()
            var count = 0
            pending.forEach { entity ->
                // Futuramente: POST real para o serverUrl
                // Por agora simula sucesso
                db.responseDao().markSynced(entity.id)
                count++
            }
            onComplete(count)
        }
    }

    // Builder
    class Builder(private val context: Context) {
        private var serverUrl: String = ""

        fun serverUrl(url: String) = apply { this.serverUrl = url }

        fun build() = MHealth4All(context, serverUrl)
    }
}