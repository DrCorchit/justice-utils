package com.drcorchit.utils

import com.drcorchit.utils.json.Result
import com.drcorchit.utils.json.failWithError
import com.drcorchit.utils.json.failWithReason
import com.drcorchit.utils.json.succeed
import java.io.*
import java.net.URL
import java.nio.file.Files

//Class exists only to be used by logger
class IOUtils

private val log = Logger.getLogger(IOUtils::class.java)

fun overwriteFile(path: String, text: String): Result {
    return overwriteFile(path, text.toByteArray())
}

//writes or overwrites the file
fun overwriteFile(path: String, data: ByteArray): Result {
    return try {
        val file = File(path)
        if (!file.exists()) {
            val parent: File = file.parentFile
            if (parent.exists()) {
                if (!parent.isDirectory) {
                    return failWithReason("Parent file of $path is not a directory")
                }
            } else if (!parent.mkdirs()) {
                return failWithReason("Could not create parent directory of $path")
            }
            if (!file.createNewFile()) {
                return failWithReason("Could not create file $path")
            }
        }
        val f = FileOutputStream(file)
        f.write(data)
        f.close()
        succeed()
    } catch (e: Exception) {
        log.error("overwriteFile", "Error while writing file $path", e)
        failWithError(e)
    }
}

//creates the file iff it doesn't exist
fun writeFile(path: String, text: String): Result {
    val file = File(path)
    return if (file.exists()) failWithReason("File already exists") else overwriteFile(path, text)
}

fun loadFile(f: File): String {
    val br = BufferedReader(FileReader(f))
    val output = StringBuilder()
    var s: String?
    while (br.readLine().also { s = it } != null) output.append(s).append("\n")
    br.close()
    return output.toString()
}

fun loadFileFromAnywhere(path: String): Pair<Long, ByteArray> {
    return try {
        loadFileFromAnywhereBaseCase(path)
    } catch (e: Exception) {
        val message = String.format("Error while loading resource %s", path)
        log.error("loadFileFromAnywhere", message, e)
        throw RuntimeException(e)
    }
}

fun getLastModified(path: String): Long {
    val file = File(path)
    if (file.exists()) return file.lastModified()
    val url = URL(path)
    return when (url.host) {
        "civplanet.net", "civpla.net" -> {
            File("html" + url.path).lastModified()
        }
        "civplanet.s3.us-east-2.amazonaws.com" -> {
            client!!.s3.getObjectMetadata("civplanet", url.path.substring(1)).lastModified.time
        }
        else -> {
            if (isS3Url(path)) {
                val pair: Pair<String, String> = parseS3Url(path)
                client!!.s3.getObjectMetadata(pair.first, pair.second).lastModified.time
            } else {
                0
            }
        }
    }
}

private fun loadFileFromAnywhereBaseCase(path: String): Pair<Long, ByteArray> {
    val lastModified: Long
    val contents: ByteArray
    val file = File(path)
    if (file.exists()) return readFile(path)
    val url = URL(path)
    return when (url.host) {
        "civplanet.net", "civpla.net" -> {
            readFile("html" + url.path)
        }
        "civplanet.s3.us-east-2.amazonaws.com" -> {
            //Omit the initial '/'
            client!!.readS3Object("civplanet", url.path.substring(1))
        }
        else -> {
            if (isS3Url(path)) {
                val pair: Pair<String, String> = parseS3Url(path)
                client!!.readS3Object(pair.first, pair.second)
            } else {
                contents = org.apache.commons.io.IOUtils.toByteArray(URL(path))
                lastModified = System.currentTimeMillis()
                Pair(lastModified, contents)
            }
        }
    }
}

fun readFile(path: String): Pair<Long, ByteArray> {
    val file = File(path)
    if (file.exists()) {
        val contents = Files.readAllBytes(file.toPath())
        val lastModified: Long = file.lastModified()
        return Pair(lastModified, contents)
    }
    throw FileNotFoundException(path)
}

fun deleteRecursively(f: File): Boolean {
    if (!f.exists()) return false
    if (f.isDirectory) {
        val files: Array<File> = f.listFiles() ?: return false
        for (file in files) deleteRecursively(file)
    }
    return f.delete()
}

//Get own IP
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