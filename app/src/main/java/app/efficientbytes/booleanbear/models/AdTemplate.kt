package app.efficientbytes.booleanbear.models

import app.efficientbytes.booleanbear.BuildConfig

enum class AdTemplate(
    val templateName: String,
    val templateId: String,
    val pauseTime: Long,
    val adsToShow: Int,
    val completionMessage: String,
) {

    TEMPLATE_20(
        templateName = "Template20",
        templateId = "t20",
        pauseTime = BuildConfig.t20PauseTime,
        adsToShow = BuildConfig.t20AdsToShow,
        completionMessage = "Your 20-minute ad-free session has concluded.",
    ),
    TEMPLATE_40(
        templateName = "Template40",
        templateId = "t40",
        pauseTime = BuildConfig.t40PauseTime,
        adsToShow = BuildConfig.t40AdsToShow,
        completionMessage = "Your 40-minute ad-free session has concluded.",
    ),
    TEMPLATE_60(
        templateName = "Template60",
        templateId = "t60",
        pauseTime = BuildConfig.t60PauseTime,
        adsToShow = BuildConfig.t60AdsToShow,
        completionMessage = "Your 1 hour ad-free session has concluded.",
    );

    companion object {

        fun getPauseTimeFor(templateId: String) = when (templateId) {
            "t20" -> TEMPLATE_20
            "t40" -> TEMPLATE_40
            "t60" -> TEMPLATE_60
            else -> TEMPLATE_40
        }

    }
}