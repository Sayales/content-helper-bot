package com.sayales.bot.storage.dao

import com.sayales.app.SecuredValue
import com.sayales.bot.storage.ao.TimeSettingsKey
import com.sayales.bot.storage.ao.lock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.math.log


abstract class PropertyKeyValueStorage<K, V>(
    private val propPath: String,
    private val keyMapper: (K) -> String,
    private val valueExtractor: (String?) -> V?,
    private val valueMapper: (V) -> String
) : KeyValueStorage<K, V> {

    private val logger = LoggerFactory.getLogger(javaClass)


    private val storeLock = ReentrantLock()

    private val propertyStorage = Properties()

    private lateinit var outputStream: OutputStream

    private lateinit var inputStream: InputStream

    @PostConstruct
    fun initProperties() {
        val path = Paths.get(propPath);
        inputStream = getInputStream(path)
        propertyStorage.load(inputStream)
        outputStream = Files.newOutputStream(path)
    }


    override fun load(key: K): V? {
            return try {
                val value = valueExtractor.invoke(propertyStorage.getProperty(keyMapper.invoke(key)))
                logger.info("Load value $key = $value")
                value
            } catch (e: Exception) {
                logger.error("Fail to load property for key = $key")
                null
            }
    }

    override fun save(key: K, value: V) {
        try {
            propertyStorage.setProperty(keyMapper.invoke(key), valueMapper.invoke(value))
            lock(storeLock) {
                propertyStorage.store(outputStream, null)
                logger.info("Store property $key = $value")
            }
        } catch (e: Exception) {

            logger.error("Fail to store property $key = $value", e)
            throw e
        }
    }

    private fun getInputStream(p: Path): InputStream =
        if (!Files.exists(p)) Files.newInputStream(Files.createFile(p)) else Files.newInputStream(p)


    @PreDestroy
    fun destroy() {
        inputStream.close()
        outputStream.close()
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