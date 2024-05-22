package app.efficientbytes.booleanbear.ui.models

enum class PASSWORD_MANAGE_MODE
    (
    val index: Int,
    val toolbarTitle: String,
    val prompt: String,
    val buttonText: String
) {

    CREATE(
        1,
        "  Create Password",
        "Create your password*",
        "Continue",
    ),
    UPDATE(
        2,
        "  Update Password",
        "Create a new password*",
        "Update",
    ),
    RESET(
        3,
        "  Reset Password",
        "Reset your password*",
        "Reset",
    ),
    DEFAULT(-1, "", "", "");

    companion object {

        fun getField(index: Int) = when (index) {
            1 -> CREATE
            2 -> UPDATE
            3 -> RESET
            else -> DEFAULT
        }
    }

}

