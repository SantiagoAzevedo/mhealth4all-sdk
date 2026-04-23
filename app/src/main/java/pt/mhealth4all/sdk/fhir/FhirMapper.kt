package pt.mhealth4all.sdk.fhir

import pt.mhealth4all.sdk.models.QuestionnaireResponse
import java.text.SimpleDateFormat
import java.util.*

object FhirMapper {

    fun toFhirJson(response: QuestionnaireResponse): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val authored = dateFormat.format(Date(response.timestamp))

        val items = response.answers.entries.joinToString(",\n") { (id, answer) ->
            """
            {
              "linkId": "$id",
              "answer": [{ "valueString": "$answer" }]
            }
            """.trimIndent()
        }

        return """
        {
          "resourceType": "QuestionnaireResponse",
          "id": "${UUID.randomUUID()}",
          "status": "completed",
          "subject": { "reference": "Patient/${response.patientId}" },
          "authored": "$authored",
          "item": [$items]
        }
        """.trimIndent()
    }
}