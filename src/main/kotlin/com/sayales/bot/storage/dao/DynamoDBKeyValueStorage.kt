package com.sayales.bot.storage.dao

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import org.springframework.beans.factory.FactoryBean
import org.springframework.stereotype.Component

/**
 * Базовый класс для работы с DynamoDB.
 * DynamoDB поддерживает только скалярные ключи
 */
abstract class DynamoDBKeyValueStorage<K,V>(private val amazonDynamoDB: AmazonDynamoDB) : KeyValueStorage<K,V> {

    private val dynamoDB = DynamoDB(amazonDynamoDB)

    override fun save(key: K, value: V) {
        val table = dynamoDB.getTable(tableName())
        table.deleteItem(idName(), mapKey(key))
        table.putItem(mapItem(key, value))
    }

    override fun load(key: K): V? {
        val table = dynamoDB.getTable(tableName())
        return extractValue(table.getItem(idName(), mapKey(key)))
    }


    protected abstract fun tableName(): String

    protected abstract fun idName(): String

    protected abstract fun mapItem(key: K, value: V) : Item

    protected abstract fun mapKey(key: K) : String

    protected abstract fun extractValue(item: Item?) : V?
}

@Component
class AmazonDynamoDBFactory() : FactoryBean<AmazonDynamoDB> {


    override fun getObject(): AmazonDynamoDB? {
        return AmazonDynamoDBAsyncClientBuilder.standard().withRegion(System.getenv("AUTODYNE_AWS_DEFAULT_REGION"))
            .withCredentials(AWSStaticCredentialsProvider(object : AWSCredentials {
                override fun getAWSAccessKeyId(): String {
                    return System.getenv("AUTODYNE_AWS_ACCESS_KEY_ID")
                }

                override fun getAWSSecretKey(): String {
                    return System.getenv("AUTODYNE_AWS_SECRET_ACCESS_KEY")
                }
            })).build()
    }

    override fun getObjectType(): Class<*>? {
       return AmazonDynamoDB::class.java
    }

}