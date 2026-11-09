package pk.paksoft.pos.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton
import pk.paksoft.pos.core.common.AppError
import pk.paksoft.pos.core.common.AppResult
import pk.paksoft.pos.core.network.dto.DataEnvelope
import pk.paksoft.pos.core.network.dto.ErrorBodyDto
import pk.paksoft.pos.core.network.dto.ErrorEnvelopeDto

/** Turns every response and every Ktor exception into an [AppResult] (docs/API.md 1.5 and 1.6). */
@Singleton
class ApiCaller(private val json: Json, private val events: SessionEvents) {

    suspend fun <T> call(serializer: KSerializer<T>, request: suspend () -> HttpResponse): AppResult<T> = guarded {
        val response = request()
        if (response.isSuccess()) {
            val envelope = json.decodeFromString(DataEnvelope.serializer(serializer), response.bodyAsText())
            AppResult.Success(envelope.data)
        } else {
            AppResult.Failure(mapError(response))
        }
    }

    suspend fun callUnit(request: suspend () -> HttpResponse): AppResult<Unit> = guarded {
        val response = request()
        if (response.isSuccess()) AppResult.Success(Unit) else AppResult.Failure(mapError(response))
    }

    private fun HttpResponse.isSuccess() = status.value in 200..299

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        AppResult.Failure(AppError.Timeout)
    } catch (e: ConnectTimeoutException) {
        AppResult.Failure(AppError.Timeout)
    } catch (e: SocketTimeoutException) {
        AppResult.Failure(AppError.Timeout)
    } catch (e: IOException) {
        AppResult.Failure(AppError.Offline)
    } catch (e: NoActiveShopException) {
        AppResult.Failure(AppError.Unexpected(e))
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.Unexpected(e))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unexpected(e))
    }

    private suspend fun mapError(response: HttpResponse): AppError {
        val status = response.status.value
        val body = response.bodyAsText()
        val error = try {
            json.decodeFromString(ErrorEnvelopeDto.serializer(), body).error
        } catch (e: SerializationException) {
            return AppError.Server(status, null)
        } catch (e: IllegalArgumentException) {
            return AppError.Server(status, null)
        }
        return error.toAppError(status)
    }

    private fun ErrorBodyDto.toAppError(status: Int): AppError = when (code) {
        "unauthenticated" -> {
            events.emitUnauthenticated()
            AppError.Unauthenticated
        }
        "forbidden", "account_inactive", "role_not_supported" -> AppError.Forbidden(code, message)
        "not_found" -> AppError.NotFound(message)
        "validation_failed" -> AppError.Validation(message, fields ?: emptyMap())
        "conflict" -> AppError.Conflict(message, details?.toString())
        "bad_request", "too_many_requests", "server_error", "maintenance" -> AppError.Server(status, message)
        else -> AppError.Business(code, message, details?.toString())
    }
}
