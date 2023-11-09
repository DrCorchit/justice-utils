package com.drcorchit.utils.exceptions

import com.drcorchit.utils.json.*

class HttpResponseException(message: String?, e: Exception?, val responseCode: Int) :
    RuntimeException(message, e) {
    constructor(e: Exception) : this(null, e, INTERNAL_ERROR) {}
    constructor(e: Exception, responseCode: Int) : this(null, e, responseCode) {}
    constructor(message: String, responseCode: Int) : this(message, null, responseCode) {}

    fun toHttpResult(): HttpResult {
        val cause = cause
        return if (cause is Exception) {
            failWithError(cause)
        } else {
            failWithReason(message?: "Unknown Failure")
        }.toHttpResult(responseCode)
    }
}