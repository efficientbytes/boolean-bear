package app.efficientbytes.booleanbear.ui.models

import android.text.InputType

enum class EDIT_PROFILE_FIELD
    (
    val index: Int,
    val description: String,
    val additionalMessage: String,
    val hintMessage: String,
    val regexPattern: String,
    val fieldType: String,
    val inputType: Int = InputType.TYPE_CLASS_TEXT,
    val enabled: Boolean = true,
    val required: Boolean = true,
    val toolbarTile: String,
) {

    FIRST_NAME(
        1,
        "This is how your first name in certificates, event registrations will look like. Please enter your first name carefully - you can only change it a limited number of times.",
        "",
        "First name",
        "",
        "TEXT",
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        toolbarTile = "First name"
    ),
    LAST_NAME(
        2,
        "This is how your last name in certificates, event registrations will look like. Please enter your last name carefully - you can only change it a limited number of times.",
        "It is optional to provide.",
        "Last name",
        "",
        "TEXT",
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        required = false,
        toolbarTile = "Last name"
    ),
    EMAIL_ADDRESS(
        3,
        "Protecting your privacy, we only use your email to enhance your experience and keep you updated with important news and exclusive offers. Ensure the security of your account by using your own email address.",
        "",
        "Email address",
        "",
        "EMAIL",
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        toolbarTile = "Email address"
    ),
    PHONE_NUMBER(
        4,
        "Your phone number has been securely saved for account verification and communication purposes. Please note that phone numbers cannot be modified for security reasons.",
        "Cannot modify phone number.",
        "Phone number with country dial code",
        "",
        "PHONE",
        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PHONETIC,
        false,
        toolbarTile = "Phone number"
    ),
    PROFESSION(
        5,
        "Please select your current profession from the options below. This helps us tailor your experience and provide relevant information as the app develops further.",
        "",
        "",
        "",
        "DROP_DOWN",
        InputType.TYPE_CLASS_TEXT,
        toolbarTile = "Profession"
    ),
    UNIVERSITY_NAME(
        6,
        "Please provide the name of the university you are currently attending, have graduated from, or plan to attend in the near future. This information will help us personalize your experience and keep you updated on any android now events happening at your campus.",
        "It is optional to provide.",
        "University name",
        "",
        "AUTO",
        InputType.TYPE_CLASS_TEXT,
        required = false,
        toolbarTile = "University name"
    ),
    LINKED_IN_USER_NAME(
        7,
        "Please share your LinkedIn profile URL. This will help us connect with you professionally.",
        "Example - https://www.linkedin.com/in/username/ . It is optional to provide.",
        "LinkedIn username",
        "",
        "TEXT",
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        required = false,
        toolbarTile = "LinkedIn username"
    ),
    GIT_HUB_USER_NAME(
        8,
        "Please share your GitHub profile URL. This will help us connect with you professionally.",
        "Example - https://github.com/username/ . It is optional to provide.",
        "GitHub username",
        "",
        "TEXT",
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
        required = false,
        toolbarTile = "GitHub username"
    ),
    DEFAULT(9, "", "", "", "", "", toolbarTile = "");

    companion object {

        fun getField(index: Int) = when (index) {
            1 -> FIRST_NAME
            2 -> LAST_NAME
            3 -> EMAIL_ADDRESS
            4 -> PHONE_NUMBER
            5 -> PROFESSION
            6 -> UNIVERSITY_NAME
            7 -> LINKED_IN_USER_NAME
            8 -> GIT_HUB_USER_NAME
            else -> DEFAULT
        }
    }

}

