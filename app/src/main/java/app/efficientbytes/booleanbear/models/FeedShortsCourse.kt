package app.efficientbytes.booleanbear.models

data class FeedShortsCourse(
    val courseId: String,
    val courseType: String,
    val title: String,
    val instructorName: String,
    val runTime: Long,
    val listViewThumbnail: String,
    val uploadedOn: Long
)