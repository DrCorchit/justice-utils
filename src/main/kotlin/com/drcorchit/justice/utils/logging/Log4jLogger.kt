package com.drcorchit.justice.utils.logging

import org.apache.logging.log4j.LogManager

class Log4jLogger(clazz: Class<*>) : Logger() {
	private val logger = LogManager.getLogger(clazz)

	override fun log(info: LogInfo) {
		if (info.error == null) {
			logger.log(info.level, info.logString)
		} else {
			logger.log(info.level, info.logString, info.error)
		}
	}
}