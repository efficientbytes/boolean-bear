package app.efficientbytes.androidnow.ui.models

data class ShortsCoursesFeedItem(
    val courseId: String,
    val courseType: String,
    val title: String,
    val instructorName: String,
    val runTime: Long,
    val listViewThumbnail: String,
    val uploadedOn: Long
)