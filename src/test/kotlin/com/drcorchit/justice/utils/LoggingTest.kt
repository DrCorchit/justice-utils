package com.drcorchit.justice.utils

import com.drcorchit.justice.utils.logging.HasUri
import com.drcorchit.justice.utils.logging.Logger
import com.drcorchit.justice.utils.logging.Uri
import com.drcorchit.justice.utils.logging.UriLogger
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class LoggingTest {
	@Test
	fun testLoggerCallsite() {
		val logger = Logger.getLogger(LoggingTest::class.java)
		Assertions.assertEquals("testLoggerCallsite:14", logger.getCallsite())
	}

	@Test
	fun testUriLoggerCallsite() {
		val hasUri = object : HasUri {
			override val parent = null
			override val uri = Uri.parse("a.b.c.test")
		}
		val logger = UriLogger.getLogger(hasUri)
		Assertions.assertEquals("a.b.c.test.testUriLoggerCallsite:24", logger.getCallsite())
	}
}