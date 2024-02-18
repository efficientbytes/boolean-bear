package app.efficientbytes.efficientbytes.repositories.models

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
    }

    sealed class Status {
        data object Loading : Status()
        data object Success : Status()
        data object Failed : Status()
    }

    val hasFailed: Boolean get() = this.status == Status.Failed
    val isSuccessful: Boolean get() = !hasFailed && this.status == Status.Success
}