package com.drcorchit.justice.utils.exceptions

import com.drcorchit.justice.utils.json.Http.Companion.INTERNAL_ERROR
import com.drcorchit.justice.utils.json.HttpResult
import com.drcorchit.justice.utils.json.Result

class HttpResponseException(message: String?, e: Exception?, val responseCode: Int) :
    RuntimeException(message, e) {
    constructor(e: Exception) : this(null, e, INTERNAL_ERROR) {}
    constructor(e: Exception, responseCode: Int) : this(null, e, responseCode) {}
    constructor(message: String, responseCode: Int) : this(message, null, responseCode) {}

    fun toHttpResult(): HttpResult {
        val cause = cause
        return if (cause is Exception) {
            Result.failWithError(cause)
        } else {
            Result.failWithReason(message?: "Unknown Failure")
        }.toHttpResult(responseCode)
    }
}