package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestSupport(
    var title: String,
    var description: String,
    var category: Int,
    var prefix: String,
    var phoneNumber: String,
    var completePhoneNumber: String,
    var userAccountId: String? = null,
)

object SingletonRequestSupport {

    private var requestSupport: RequestSupport? = null
    fun getInstance() = requestSupport

    fun setInstance(requestSupport: RequestSupport) {
        this.requestSupport = requestSupport
    }
}

@JsonClass(generateAdapter = true)
data class RequestSupportResponse(
    val ticketId: String,
    val message: String
)
