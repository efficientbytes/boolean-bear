package app.efficientbytes.booleanbear.models

enum class VideoQualityType(
    val width: Int,
    val height: Int,
    val label: String
) {

    AUTO(1920, 1080, label = "Auto"),
    _480P(854, 480, "480p"),
    _720P(1280, 720, "720p"),
    _1080P(1920, 1080, "1080p")
}