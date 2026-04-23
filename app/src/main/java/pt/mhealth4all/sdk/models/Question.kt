package pt.mhealth4all.sdk.models

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val minValue: Int = 1,
    val maxValue: Int = 10
)