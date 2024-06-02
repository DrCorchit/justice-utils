package com.drcorchit.justice.utils

import com.drcorchit.justice.utils.logging.Logger

//Copied from https://memorynotfound.com/detect-os-name-version-java/ with modifications
object OSUtils {
    private val log = Logger.getLogger(OSUtils::class.java)
    val CURRENT_OS: OS

    init {
        CURRENT_OS = try {
            detectOS()
        } catch (e: Exception) {
            log.error("Error while detecting OS", e)
            OS.OTHER
        }
    }

    private fun detectOS(): OS {
        var osName = System.getProperty("os.name") ?: throw RuntimeException("os.name not found")
        osName = osName.lowercase()
        return if (osName.contains("windows")) OS.WINDOWS
        else if (osName.contains("mac os")) OS.MAC
        else if (
               osName.contains("linux")
            || osName.contains("mpe/ix")
            || osName.contains("freebsd")
            || osName.contains("irix")
            || osName.contains("digital unix")
            || osName.contains("unix")
        ) {
            OS.UNIX
        } else if (
               osName.contains("sun os")
            || osName.contains("sunos")
            || osName.contains("solaris")
            || osName.contains("hp-ux")
            || osName.contains("aix")
        ) {
            OS.POSIX_UNIX
        } else {
            OS.OTHER
        }
    }

    enum class OS {
        WINDOWS,
        UNIX,
        POSIX_UNIX,
        MAC,
        OTHER
    }
}