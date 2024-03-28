package app.efficientbytes.booleanbear.enums

enum class COURSE_CONTENT_TYPE(private val contentType: String) {

    CONCEPT_LEARNING("Concept learning"),
    IMPLEMENTATION("Implementation"),
    PROJECTS("Projects"),
    SERIES("Series"),
    SYSTEM_DESIGN("System designs"),
    ALGORITHMS("Algorithms"),
    INSTALLATION("Installation setup"),
    PREP_TALK("Prep Talk");

    fun getContentType() = this.contentType

    companion object {

        fun findContentType(content: String): COURSE_CONTENT_TYPE {
            return when (content) {
                CONCEPT_LEARNING.getContentType() -> {
                    CONCEPT_LEARNING
                }

                IMPLEMENTATION.getContentType() -> {
                    IMPLEMENTATION
                }

                PROJECTS.getContentType() -> {
                    PROJECTS
                }

                SYSTEM_DESIGN.getContentType() -> {
                    SYSTEM_DESIGN
                }

                SERIES.getContentType() -> {
                    SERIES
                }

                ALGORITHMS.getContentType() -> {
                    ALGORITHMS
                }

                INSTALLATION.getContentType() -> {
                    INSTALLATION
                }

                PREP_TALK.getContentType() -> {
                    PREP_TALK
                }

                else -> PROJECTS
            }
        }
    }

}