package app.efficientbytes.booleanbear.services.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestSupport(
    var title: String,
    var description: String,
    var category: Int,
    var completePhoneNumber: String? = null,
    var message: String? = null,
    var userAccountId: String? = null,
)

object SingletonRequestSupport {

    private var requestSupport: RequestSupport? = null
    fun getInstance() = requestSupport

    fun setInstance(requestSupport: RequestSupport) {
        this.requestSupport = requestSupport
    }
}