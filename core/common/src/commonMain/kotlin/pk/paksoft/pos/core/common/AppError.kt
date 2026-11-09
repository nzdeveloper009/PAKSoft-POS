package pk.paksoft.pos.core.common

/** Every failure the layers above `core:network` see; no exceptions cross a layer boundary. */
sealed interface AppError {
    data object Offline : AppError
    data object Timeout : AppError

    /** HTTP 401: the token is missing or revoked; the session must end. */
    data object Unauthenticated : AppError
    data class Forbidden(val code: String, val message: String) : AppError
    data class NotFound(val message: String) : AppError
    data class Validation(val message: String, val fields: Map<String, List<String>>) : AppError
    data class Conflict(val message: String, val detailsJson: String?) : AppError

    /** A business-rule rejection such as `insufficient_stock` or `totals_mismatch`. */
    data class Business(val code: String, val message: String, val detailsJson: String?) : AppError

    /** [message] is null when the body was not a JSON error envelope (for example an HTML 404 page). */
    data class Server(val status: Int, val message: String?) : AppError
    data class Unexpected(val cause: Throwable?) : AppError
}
