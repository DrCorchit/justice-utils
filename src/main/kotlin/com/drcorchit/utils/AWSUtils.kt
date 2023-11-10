package com.drcorchit.utils

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion
import com.drcorchit.utils.json.Result
import com.drcorchit.utils.json.failWithError
import com.drcorchit.utils.json.prettyPrint
import com.drcorchit.utils.json.succeed
import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.function.Consumer
import java.util.stream.Collectors

private val log = Logger.getLogger(AWSClient::class.java)

class AWSClient constructor(
    val creds: AWSCredentials,
    val defaultBucketName: String
) {
    val s3: AmazonS3
    val database: DynamoDB

    init {
        val provider: AWSCredentialsProvider = AWSStaticCredentialsProvider(creds)
        s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion(Regions.US_EAST_2).build()

        val bucketExists: Boolean = try {
            s3.doesBucketExistV2(defaultBucketName)
        } catch (e: Exception) {
            log.error("init", "Unable to initialize AWS client", e)
            false
        }

        if (!bucketExists) {
            log.warn("init", "Could not confirm existence of bucket $defaultBucketName")
        }

        val db = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(provider)
            .withRegion(Regions.US_EAST_2).build()
        database = DynamoDB(db)
    }

    fun listObjects(bucket: String, prefix: String): List<S3ObjectSummary> {
        val req = ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(prefix)
        val output = ArrayList<S3ObjectSummary>()
        var result: ListObjectsV2Result
        do {
            result = s3.listObjectsV2(bucket, prefix)
            output.addAll(result.objectSummaries)
            req.continuationToken = result.continuationToken
        } while (result.isTruncated)
        return output
    }

    fun doesS3ObjectExist(bucketName: String, path: String): Boolean {
        return try {
            s3.doesObjectExist(bucketName, path)
        } catch (e: java.lang.Exception) {
            log.error("doesObjectExistInS3", "Error while checking if s3 object exists", e)
            false
        }
    }

    fun getObjectMetadata(bucketName: String, path: String): ObjectMetadata? {
        return s3.getObjectMetadata(bucketName, path)
    }

    fun readS3Object(bucketName: String, path: String): Pair<Long, ByteArray> {
        val obj = s3.getObject(bucketName, path)
        val lastModified = obj.objectMetadata.lastModified.time
        val bytes = org.apache.commons.io.IOUtils.toByteArray(obj.objectContent)
        return Pair(lastModified, bytes)
    }

    fun writeS3Object(bucket: String, key: String, info: JsonObject): Result {
        return try {
            s3.putObject(bucket, key, info.prettyPrint())
            succeed()
        } catch (e: java.lang.Exception) {
            failWithError(e)
        }
    }

    fun deleteS3Prefix(bucket: String, prefix: String): Result {
        return try {
            val keys = listObjects(bucket, prefix).stream()
                .map { obj: S3ObjectSummary -> obj.key }
                .map { key: String? ->
                    KeyVersion(
                        key
                    )
                }.collect(Collectors.toList())
            val request = DeleteObjectsRequest(bucket).withKeys(keys)
            s3.deleteObjects(request)
            succeed()
        } catch (e: java.lang.Exception) {
            failWithError(e)
        }
    }

    fun putItem(tableName: String, primaryKey: String, properties: JsonObject) {
        val item = Item().withPrimaryKey(PrimaryKey("userID", primaryKey))
        properties.entrySet().forEach(Consumer { (key, value): Map.Entry<String?, JsonElement> ->
            item.withJSON(
                key,
                value.toString()
            )
        })
        database.getTable(tableName).putItem(item)
    }

    fun loadItem(tableName: String?, id: String?): JsonObject {
        val output = database.getTable(tableName).getItem(PrimaryKey("userID", id))
        return dynamoDBItemToJson(output)
    }
}

fun parseS3Url(url: String): Pair<String, String> {
    if (url.startsWith("s3://")) {
        val remain = url.substring(5)
        val slashPos = remain.indexOf("/")
        return Pair(remain.substring(0, slashPos), remain.substring(slashPos + 1))
    }
    throw IllegalArgumentException()
}

fun isS3Url(url: String): Boolean {
    return url.matches("[sS]3://\\w+(/\\w+)+".toRegex())
}

fun dynamoDBItemToJson(item: Item): JsonObject {
    return objectToJson(item.asMap()).asJsonObject
}

private fun objectToJson(input: Any?): JsonElement {
    if (input == null) return JsonNull.INSTANCE
    when (input) {
        is Map<*, *> -> {
            val output = JsonObject()
            input.forEach { (key, value) -> output.add(key as String, objectToJson(value!!)) }
        }
        is List<*> -> {
            val output = JsonArray()
            input.forEach { output.add(objectToJson(it)) }
        }
        is String -> JsonPrimitive(input)
        is BigInteger -> JsonPrimitive(input)
        is BigDecimal -> JsonPrimitive(input)
        is Boolean -> JsonPrimitive(input)
    }
    throw IllegalArgumentException("Unknown JSON component: ${input.javaClass}")
}