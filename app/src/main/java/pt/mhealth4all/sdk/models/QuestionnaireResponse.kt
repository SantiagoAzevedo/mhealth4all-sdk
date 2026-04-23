package pt.mhealth4all.sdk.models

data class QuestionnaireResponse(
    val questionnaireId: String,
    val patientId: String,
    val answers: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)