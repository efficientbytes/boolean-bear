package app.efficientbytes.booleanbear.repositories.models

data class DataStatus<out T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null
) {

    companion object {

        fun <T> loading(): DataStatus<T> {
            return DataStatus(Status.Loading)
        }

        fun <T> success(data: T): DataStatus<T> {
            return DataStatus(
                status = Status.Success,
                data = data
            )
        }

        fun <T> failed(error: String): DataStatus<T> {
            return DataStatus(
                status = Status.Failed,
                message = error
            )
        }

        fun <T> emptyResult(): DataStatus<T> {
            return DataStatus(Status.EmptyResult)
        }

        fun <T> timeOut(): DataStatus<T> {
            return DataStatus(Status.TimeOut)
        }

        fun <T> noInternet(): DataStatus<T> {
            return DataStatus(Status.NoInternet)
        }

        fun <T> unAuthorized(): DataStatus<T> {
            return DataStatus(Status.UnAuthorized)
        }

        fun <T> unknownException(exceptionMessage: String): DataStatus<T> {
            return DataStatus(status = Status.UnKnownException, message = exceptionMessage)
        }
    }

    sealed class Status {
        data object Loading : Status()
        data object Success : Status()
        data object Failed : Status()

        data object EmptyResult : Status()
        data object TimeOut : Status()
        data object NoInternet : Status()
        data object UnAuthorized : Status()
        data object UnKnownException : Status()
    }

    val hasFailed: Boolean get() = this.status == Status.Failed
    val isSuccessful: Boolean get() = !hasFailed && this.status == Status.Success
}