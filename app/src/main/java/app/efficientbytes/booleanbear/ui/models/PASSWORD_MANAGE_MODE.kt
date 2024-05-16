package app.efficientbytes.booleanbear.ui.models

enum class PASSWORD_MANAGE_MODE
    (
    val index: Int,
    val title: String,
    val buttonText: String
) {

    CREATE(
        1,
        "Create Password",
        "Continue",
    ),
    UPDATE(
        2,
        "Update Password",
        "Update",
    ),
    DEFAULT(-1, "", "");

    companion object {

        fun getField(index: Int) = when (index) {
            1 -> CREATE
            2 -> UPDATE
            else -> DEFAULT
        }
    }

}

