package app.efficientbytes.booleanbear.models

enum class AdTemplates(val templateName: String,val templateId: String,val pauseTime: Long,val adsToShow: Int) {

    TEMPLATE_20(templateName = "Template20", templateId =  "t20", pauseTime =  20, adsToShow =  3),
    TEMPLATE_40(templateName = "Template40", templateId =  "t40", pauseTime =  40, adsToShow =  5),
    TEMPLATE_60(templateName = "Template60", templateId = "t60", pauseTime = 60, adsToShow =  7);

    companion object {

        fun getPauseTimeFor(templateId: String) = when (templateId) {
            "t20" -> TEMPLATE_20
            "t40" -> TEMPLATE_40
            "t60" -> TEMPLATE_60
            else -> TEMPLATE_40
        }

    }
}