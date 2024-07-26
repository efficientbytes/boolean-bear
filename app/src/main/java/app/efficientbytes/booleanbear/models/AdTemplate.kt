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
        completionMessage = "Your 15-minute ad-free session has concluded.",
    ),

    TEMPLATE_15(
        templateName = "Template15",
        templateId = "t15",
        pauseTime = BuildConfig.t15PauseTime,
        adsToShow = BuildConfig.t15AdsToShow,
        completionMessage = "Your 15-minute ad-free session has concluded.",
    ),

    TEMPLATE_30(
        templateName = "Template30",
        templateId = "t30",
        pauseTime = BuildConfig.t30PauseTime,
        adsToShow = BuildConfig.t30AdsToShow,
        completionMessage = "Your 30-minute ad-free session has concluded.",
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
            "t10" -> TEMPLATE_10
            "t15" -> TEMPLATE_15
            "t30" -> TEMPLATE_30
            "t60" -> TEMPLATE_60
            else -> TEMPLATE_15
        }

    }
}