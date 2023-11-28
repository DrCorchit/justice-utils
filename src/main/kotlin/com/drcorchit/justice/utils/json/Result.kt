package com.drcorchit.justice.utils.json

import com.google.gson.JsonObject

//A simple object which creates json objects with fields:
//"success" (boolean) if the operation succeeded
//"reason" (string) why the operation failed, if applicable
//optionally "info" if the operation succeeded and more info is needed
//optionally "error" if the operation failed due to a specific exception
sealed class Result(val success: Boolean) {

    abstract fun serialize(): JsonObject

    fun and(other: Result): Result {
        return if (success) {
            if (other.success) {
                //Return whichever result has more useful info
                if (this is JsonResult) this else other
            } else other
        } else this
    }

    fun toHttpResult(httpCode: Int): HttpResult {
        return this to httpCode
    }

    override fun toString(): String {
        return serialize().toString()
    }

    companion object {
        private val SUCCESS: SuccessfulResult = SuccessfulResult()

        @JvmStatic
        fun succeed(): SuccessfulResult {
            return SUCCESS
        }

        @JvmStatic
        fun succeedWithInfo(info: JsonObject): JsonResult {
            return JsonResult(info)
        }

        @JvmStatic
        fun failWithReason(reason: String): FailedResult {
            return FailedResult(reason)
        }

        @JvmStatic
        fun failWithError(e: Exception): ExceptionResult {
            return ExceptionResult(e)
        }

        @JvmStatic
        fun failWithErrorAndReason(reason: String, e: Exception): ExceptionResult {
            return ExceptionResult(reason, e)
        }
    }
}

open class SuccessfulResult : Result(true) {
    override fun serialize(): JsonObject {
        val output = JsonObject()
        output.addProperty("success", success)
        return output
    }
}

class JsonResult constructor(val info: JsonObject) : SuccessfulResult() {
    override fun serialize(): JsonObject {
        val output = super.serialize()
        output.add("info", info)
        return output
    }
}

open class FailedResult(val reason: String) : Result(false) {
    override fun serialize(): JsonObject {
        val output = JsonObject()
        output.addProperty("success", success)
        output.addProperty("reason", reason)
        return output
    }
}

class ExceptionResult(reason: String, val error: Exception) : FailedResult(reason) {
    constructor(error: Exception) : this(error.message!!, error)

    override fun serialize(): JsonObject {
        val output = super.serialize()
        output.addProperty("error", error.stackTraceToString())
        return output
    }
}