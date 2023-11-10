package com.drcorchit.justice.utils.json

import com.drcorchit.justice.utils.exceptions.HttpResponseException
import jakarta.servlet.http.HttpServletResponse

typealias HttpResult = Pair<Result, Int>

fun Result.toHttpResult(): HttpResult {
    val code = if (this.success) HttpServletResponse.SC_OK else HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    return toHttpResult(code)
}

fun HttpResult.getResult(): Result {
    return this.first
}

fun HttpResult.getHttpCode(): Int {
    return this.second
}

fun HttpResult.getSuccess(): Boolean {
    return this.getHttpCode() < HttpServletResponse.SC_BAD_REQUEST
}

fun HttpResult.asException(): HttpResponseException {
    when (val res = this.getResult()) {
        is SuccessfulResult -> throw IllegalStateException("Cannot convert SuccessfulResult to an exception.")
        is ExceptionResult -> throw HttpResponseException(res.reason, res.error, getHttpCode())
        is FailedResult -> throw HttpResponseException(res.reason, null, getHttpCode())
    }
}

class Http {
    companion object {

        private val SUCCESS = Result.succeed() to HttpServletResponse.SC_OK

        @JvmStatic
        fun badRequest(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(HttpServletResponse.SC_BAD_REQUEST)
        }

        @JvmStatic
        fun unauthorized(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(HttpServletResponse.SC_UNAUTHORIZED)
        }

        @JvmStatic
        fun forbidden(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(HttpServletResponse.SC_FORBIDDEN)
        }

        @JvmStatic
        fun notFound(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(HttpServletResponse.SC_NOT_FOUND)
        }

        @JvmStatic
        fun userNotFound(username: String, game: String): HttpResult {
            return forbidden("User $username was not found in game $game")
        }

        @JvmStatic
        fun missingRequestParameter(parameter: String): HttpResult {
            val reason = String.format("Request is missing parameter: \"%s\"", parameter)
            return Result.failWithReason(reason).toHttpResult(HttpServletResponse.SC_BAD_REQUEST)
        }

        @JvmStatic
        fun missingRequestParameters(parameters: List<String>): HttpResult {
            val reason = String.format("Request is missing parameters: \"%s\"", parameters)
            return Result.failWithReason(reason).toHttpResult(HttpServletResponse.SC_BAD_REQUEST)
        }

        @JvmStatic
        fun missingPostBody(): HttpResult {
            val reason = "Request body is missing."
            return Result.failWithReason(reason).toHttpResult(HttpServletResponse.SC_BAD_REQUEST)
        }

        @JvmStatic
        fun internalError(e: Exception): HttpResult {
            return Result.failWithError(e).toHttpResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        }

        @JvmStatic
        fun ok(): HttpResult {
            return SUCCESS
        }
    }
}