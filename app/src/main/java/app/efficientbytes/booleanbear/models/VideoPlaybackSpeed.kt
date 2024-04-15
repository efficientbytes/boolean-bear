package app.efficientbytes.booleanbear.models

enum class VideoPlaybackSpeed(
    val speed: Float,
    val label: String
) {

    x0_5(0.5F, "0.5x"),
    x0_75(0.75F, "0.75x"),
    x1(1F, "1x"),
    x1_25(1.25F, "1.25x"),
    x1_5(1.5F, "1.5x"),
    x1_75(1.75F, "1.75x"),

}