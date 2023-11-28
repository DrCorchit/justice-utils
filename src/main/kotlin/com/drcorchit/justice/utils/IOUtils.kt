package com.drcorchit.justice.utils

import com.drcorchit.justice.utils.aws.AWSClient
import com.drcorchit.justice.utils.aws.AWSUtils
import com.drcorchit.justice.utils.json.Result
import com.drcorchit.justice.utils.json.TimestampedBytes
import com.drcorchit.justice.utils.logging.Logger
import java.io.*
import java.net.URL
import java.nio.file.Files

object IOUtils {
    private val log = Logger.getLogger(IOUtils::class.java)

    @JvmStatic
    fun overwriteFile(path: String, text: String): Result {
        return overwriteFile(path, text.toByteArray())
    }

    @JvmStatic
    //writes or overwrites the file
    fun overwriteFile(path: String, data: ByteArray): Result {
        return try {
            val file = File(path)
            if (!file.exists()) {
                val parent: File = file.parentFile
                if (parent.exists()) {
                    if (!parent.isDirectory) {
                        return Result.failWithReason("Parent file of $path is not a directory")
                    }
                } else if (!parent.mkdirs()) {
                    return Result.failWithReason("Could not create parent directory of $path")
                }
                if (!file.createNewFile()) {
                    return Result.failWithReason("Could not create file $path")
                }
            }
            val f = FileOutputStream(file)
            f.write(data)
            f.close()
            Result.succeed()
        } catch (e: Exception) {
            log.error("overwriteFile", "Error while writing file $path", e)
            Result.failWithError(e)
        }
    }

    @JvmStatic
    //creates the file iff it doesn't exist
    fun writeFile(path: String, text: String): Result {
        val file = File(path)
        return if (file.exists()) Result.failWithReason("File already exists") else overwriteFile(path, text)
    }

    @JvmStatic
    fun loadFile(f: File): String {
        return String(Files.readAllBytes(f.toPath()))
    }

    @JvmStatic
    fun getLastModified(path: String, client: AWSClient? = null): Long {
        val file = File(path)
        if (file.exists()) return file.lastModified()
        if (AWSUtils.isS3Url(path) && client != null) {
            val pair: Pair<String, String> = AWSUtils.parseS3Url(path)
            client.s3.getObjectMetadata(pair.first, pair.second).lastModified.time
        }
        return 0
    }

    @JvmStatic
    fun readFile(path: String): TimestampedBytes {
        val file = File(path)
        if (file.exists()) {
            val contents = Files.readAllBytes(file.toPath())
            val lastModified: Long = file.lastModified()
            return contents to lastModified
        }
        throw FileNotFoundException(path)
    }

    @JvmStatic
    fun readUrl(url: String): TimestampedBytes? {
        return try {
            val contents = org.apache.commons.io.IOUtils.toByteArray(URL(url))
            contents to System.currentTimeMillis()
        } catch (e: Exception) {
            log.error("readUrl", "Unable to read url.", e)
            null
        }
    }

    @JvmStatic
    fun deleteRecursively(f: File): Boolean {
        if (!f.exists()) return false
        if (f.isDirectory) {
            val files: Array<File> = f.listFiles() ?: return false
            for (file in files) deleteRecursively(file)
        }
        return f.delete()
    }

    @JvmStatic
    fun showIP(): String {
        val ip: URL
        val temp: String
        return try {
            ip = URL("http://checkip.amazonaws.com")
            val `in` = BufferedReader(InputStreamReader(ip.openStream()))
            temp = `in`.readLine()
            temp
        } catch (e: IOException) {
            //Probably caused by no internet.
            log.warn("showIP", "Unable to retrieve IP due to $e. Check internet connection.")
            "127.0.0.1"
        }
    }
}