package pt.mhealth4all.sdk.sync

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class FhirSyncService(private val serverUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/fhir+json; charset=utf-8".toMediaType()

    // Envia um recurso FHIR para o servidor
    // Devolve true se sucesso, false se falhou
    fun postResource(resourceType: String, fhirJson: String): Boolean {
        return try {
            val url = "$serverUrl/$resourceType"
            val body = fhirJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Accept", "application/fhir+json")
                .addHeader("Content-Type", "application/fhir+json")
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            response.close()
            success

        } catch (e: Exception) {
            println("Erro ao enviar para FHIR: ${e.message}")
            false
        }
    }
}