package app.efficientbytes.booleanbear.models

import app.efficientbytes.booleanbear.BuildConfig

enum class AdTemplate(
    val templateName: String,
    val templateId: String,
    val pauseTime: Long,
    val adsToShow: Int,
    val completionMessage: String,
) {

    TEMPLATE_10(
        templateName = "Template10",
        templateId = "t10",
        pauseTime = BuildConfig.t10PauseTime,
        adsToShow = BuildConfig.t10AdsToShow,
        completionMessage = "Your 10-minute ad-free session has concluded.",
    );

    companion object {

        fun getPauseTimeFor(templateId: String) = when (templateId) {
            "t10" -> TEMPLATE_10
            else -> TEMPLATE_10
        }

    }
}