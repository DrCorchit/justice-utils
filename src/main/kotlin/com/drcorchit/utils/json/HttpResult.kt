package com.drcorchit.utils.json

import com.drcorchit.utils.exceptions.HttpResponseException
import jakarta.servlet.http.HttpServletResponse

typealias HttpResult = Pair<Result, Int>

fun Result.toHttpResult(): HttpResult {
    val code = if (this.success) OK else INTERNAL_ERROR
    return toHttpResult(code)
}

fun Result.toHttpResult(code: Int): HttpResult {
    return Pair(this, code)
}

fun HttpResult.getResult(): Result {
    return this.first
}

fun HttpResult.getCode(): Int {
    return this.second;
}

fun HttpResult.getSuccess(): Boolean {
    return this.getCode() < BAD_REQUEST
}

fun HttpResult.asException(): HttpResponseException {
    when (val res = this.getResult()) {
        is SuccessfulResult -> throw IllegalStateException("Cannot convert SuccessfulResult to an exception.")
        is ExceptionResult -> throw HttpResponseException(res.reason, res.error, getCode())
        is FailedResult -> throw HttpResponseException(res.reason, null, getCode())
    }
}

val OK: Int = HttpServletResponse.SC_OK
val BAD_REQUEST: Int = HttpServletResponse.SC_BAD_REQUEST
val UNAUTHORIZED: Int = HttpServletResponse.SC_UNAUTHORIZED
val FORBIDDEN: Int = HttpServletResponse.SC_FORBIDDEN
val NOT_FOUND: Int = HttpServletResponse.SC_NOT_FOUND
val INTERNAL_ERROR: Int = HttpServletResponse.SC_INTERNAL_SERVER_ERROR

private val SUCCESS = Pair(succeed(), OK)

fun badRequest(message: String): HttpResult {
    return failWithReason(message).toHttpResult(BAD_REQUEST)
}

fun unauthorized(message: String): HttpResult {
    return failWithReason(message).toHttpResult(UNAUTHORIZED)
}

fun forbidden(message: String): HttpResult {
    return failWithReason(message).toHttpResult(FORBIDDEN)
}

fun notFound(message: String): HttpResult {
    return failWithReason(message).toHttpResult(NOT_FOUND)
}

fun userNotFound(username: String, game: String): HttpResult {
    return forbidden("User $username was not found in game $game")
}

fun missingRequestParameter(parameter: String): HttpResult {
    val reason = String.format("Request is missing parameter: \"%s\"", parameter)
    return failWithReason(reason).toHttpResult(BAD_REQUEST)
}

fun missingRequestParameters(parameters: List<String>): HttpResult {
    val reason = String.format("Request is missing parameters: \"%s\"", parameters)
    return failWithReason(reason).toHttpResult(BAD_REQUEST)
}

fun missingPostBody(): HttpResult {
    val reason = "Request body is missing."
    return failWithReason(reason).toHttpResult(BAD_REQUEST)
}

fun internalError(e: Exception): HttpResult {
    return failWithError(e).toHttpResult(INTERNAL_ERROR)
}

fun ok(): HttpResult {
    return SUCCESS
}