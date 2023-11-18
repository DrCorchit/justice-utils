package com.drcorchit.justice.utils.json

import com.drcorchit.justice.utils.exceptions.HttpResponseException
import com.drcorchit.justice.utils.json.Http.Companion.INTERNAL_ERROR
import com.drcorchit.justice.utils.json.Http.Companion.OK
import com.google.gson.JsonObject

typealias HttpResult = Pair<Result, Int>

fun Result.toHttpResult(): HttpResult {
    val code = if (this.success) OK else INTERNAL_ERROR
    return toHttpResult(code)
}

fun HttpResult.getResult(): Result {
    return this.first
}

fun HttpResult.getHttpCode(): Int {
    return this.second
}

fun HttpResult.and(other: HttpResult): HttpResult {
    return if (success) {
        if (other.success) {
            //Return whichever result has more useful info
            if (getResult() is JsonResult) this else other
        } else other
    } else this
}

fun HttpResult.serialize(): JsonObject {
    val output = getResult().serialize()
    output.addProperty("httpCode", getHttpCode())
    return output
}

val HttpResult.success: Boolean
    get() = getResult().success

fun HttpResult.asException(): HttpResponseException {
    when (val res = this.getResult()) {
        is SuccessfulResult -> throw IllegalStateException("Cannot convert SuccessfulResult to an exception.")
        is ExceptionResult -> throw HttpResponseException(res.reason, res.error, getHttpCode())
        is FailedResult -> throw HttpResponseException(res.reason, null, getHttpCode())
    }
}

class Http {
    companion object {
        const val OK = 200
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val INTERNAL_ERROR = 500

        private val SUCCESS = Result.succeed() to OK

        @JvmStatic
        fun badRequest(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(BAD_REQUEST)
        }

        @JvmStatic
        fun unauthorized(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(UNAUTHORIZED)
        }

        @JvmStatic
        fun forbidden(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(FORBIDDEN)
        }

        @JvmStatic
        fun notFound(message: String): HttpResult {
            return Result.failWithReason(message).toHttpResult(NOT_FOUND)
        }

        @JvmStatic
        fun userNotFound(username: String, game: String): HttpResult {
            return forbidden("User $username was not found in game $game")
        }

        @JvmStatic
        fun missingRequestParameter(parameter: String): HttpResult {
            val reason = String.format("Request is missing parameter: \"%s\"", parameter)
            return Result.failWithReason(reason).toHttpResult(BAD_REQUEST)
        }

        @JvmStatic
        fun missingRequestParameters(parameters: List<String>): HttpResult {
            val reason = String.format("Request is missing parameters: \"%s\"", parameters)
            return Result.failWithReason(reason).toHttpResult(BAD_REQUEST)
        }

        @JvmStatic
        fun missingPostBody(): HttpResult {
            val reason = "Request body is missing."
            return Result.failWithReason(reason).toHttpResult(BAD_REQUEST)
        }

        @JvmStatic
        fun internalError(e: Exception): HttpResult {
            return Result.failWithError(e).toHttpResult(INTERNAL_ERROR)
        }

        @JvmStatic
        fun ok(): HttpResult {
            return SUCCESS
        }

        @JvmStatic
        fun ok(info: JsonObject): HttpResult {
            return Result.succeedWithInfo(info).toHttpResult()
        }

        @JvmStatic
        fun requireParams(message: String, body: JsonObject, vararg params: String): Unit {
            val missing = params.filter { !body.has(it) }
            check(missing.isEmpty()) { String.format(message, missing) }
        }
    }
}