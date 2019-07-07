package com.sayales.bot.storage.dao

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.sayales.bot.storage.domain.TimeSettingsKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

interface TimeoutSettingsDAO {
    fun put(key: TimeSettingsKey, timeout: Long)
    fun get(key: TimeSettingsKey): Long?

}

@Component
class PropertyTimeoutSettingsDAO(@Autowired val lifetimeDynamoDBStorage: KeyValueStorage<TimeSettingsKey, Long>) : TimeoutSettingsDAO {

    override fun put(key: TimeSettingsKey, timeout: Long) {
        lifetimeDynamoDBStorage.save(key, timeout)
    }

    override fun get(key: TimeSettingsKey): Long? {
        return lifetimeDynamoDBStorage.load(key)
    }

}

@Component
class LifetimeDynamoDBStorage(@Autowired val amazonDynamoDB: AmazonDynamoDB,
                              @Value("\${dynamo.lifesettings.table}") val table: String,
                              @Value("\${dynamo.lifesettings.id}") val idName: String,
                              @Value("\${dynamo.lifesettings.value}") val valueName: String) :
    DynamoDBKeyValueStorage<TimeSettingsKey, Long>(amazonDynamoDB) {

    override fun mapItem(key: TimeSettingsKey, value: Long): Item {
        return Item()
            .withPrimaryKey(idName, key.toString())
            .withLong(valueName, value)
    }

    override fun mapKey(key: TimeSettingsKey): String {
        return key.toString()
    }

    override fun extractValue(item: Item?): Long? {
       return item?.get(valueName)?.toString()?.toLong()
    }


    override fun tableName(): String {
        return table
    }

    override fun idName(): String {
        return idName
    }



}


@Component
class LifetimePropertyStorage(@Value("\${storage.properties.path}") val propPath: String,
                              @Value("\${storage.properties.delimiter}") val keyDelimiter: String) :
    PropertyKeyValueStorage<TimeSettingsKey, Long>(
        propPath,
        { it.contentType.name + keyDelimiter + it.chatId },
        { it?.toLong() },
        Long::toString
    )