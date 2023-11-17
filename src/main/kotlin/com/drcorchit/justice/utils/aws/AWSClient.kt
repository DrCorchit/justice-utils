package com.drcorchit.justice.utils.aws

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
import com.drcorchit.justice.utils.Logger
import com.drcorchit.justice.utils.json.JsonUtils.prettyPrint
import com.drcorchit.justice.utils.json.Result
import com.drcorchit.justice.utils.json.TimestampedBytes
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.stream.Collectors

class AWSClient(
    val creds: AWSCredentials,
    val regions: Regions,
    val defaultBucketName: String
) {
    val s3: AmazonS3
    val database: DynamoDB

    init {
        val provider: AWSCredentialsProvider = AWSStaticCredentialsProvider(creds)
        s3 = AmazonS3ClientBuilder.standard().withCredentials(provider).withRegion(regions).build()

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
            .withRegion(regions).build()
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

    fun getObjectMetadata(bucketName: String, path: String): ObjectMetadata {
        return s3.getObjectMetadata(bucketName, path)
    }

    fun readS3Object(bucketName: String, path: String): TimestampedBytes {
        val obj = s3.getObject(bucketName, path)
        val lastModified = obj.objectMetadata.lastModified.time
        val bytes = org.apache.commons.io.IOUtils.toByteArray(obj.objectContent)
        return bytes to lastModified
    }

    fun writeS3Object(bucket: String, key: String, info: JsonObject): Result {
        return try {
            s3.putObject(bucket, key, info.prettyPrint())
            Result.succeed()
        } catch (e: java.lang.Exception) {
            Result.failWithError(e)
        }
    }

    fun deleteS3Prefix(bucket: String, prefix: String): Result {
        return try {
            val keys = listObjects(bucket, prefix).stream()
                .map { obj: S3ObjectSummary -> obj.key }
                .map { DeleteObjectsRequest.KeyVersion(it) }
                .collect(Collectors.toList())
            val request = DeleteObjectsRequest(bucket).withKeys(keys)
            s3.deleteObjects(request)
            Result.succeed()
        } catch (e: java.lang.Exception) {
            Result.failWithError(e)
        }
    }

    fun putItem(tableName: String, primaryKey: String, primaryValue: String, properties: JsonObject) {
        val item = Item().withPrimaryKey(PrimaryKey(primaryKey, primaryValue))
        properties.entrySet().forEach { (key, value): Map.Entry<String, JsonElement> ->
            item.withJSON(key, value.toString())
        }
        database.getTable(tableName).putItem(item)
    }

    fun getItem(tableName: String, primaryKey: String, primaryValue: String): JsonObject {
        val output = database.getTable(tableName).getItem(PrimaryKey(primaryKey, primaryValue))
        return AWSUtils.dynamoDBItemToJson(output)
    }

    companion object {
        private val log = Logger.getLogger(AWSClient::class.java)
    }
}