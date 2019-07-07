package com.sayales.bot.storage.ao

import com.google.common.cache.CacheBuilder
import com.sayales.bot.ContentType
import com.sayales.bot.storage.dao.PropertyTimeoutSettingsDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

interface TimeoutSettingsAO {
    fun put(type: ContentType, chatId: String, timeout: Long)
    fun get(type: ContentType, chatId: String): Long
    fun getInfo(chatId: String): String
}


@Component
class GuavaCacheSettingsAO(@Value("\${storage.cache.size}") val cacheSize: Long,
                           @Value("\${storage.cache.lifetime}") val lifetime: Long,
                           @Autowired val propertyTimeoutSettingsDAO: PropertyTimeoutSettingsDAO) : TimeoutSettingsAO {

    private val cachedStorage =
        CacheBuilder.newBuilder().maximumSize(cacheSize).expireAfterAccess(lifetime, TimeUnit.MILLISECONDS)
            .build<TimeSettingsKey, Long>()


    override fun put(type: ContentType, chatId: String, timeout: Long) {
        TimeSettingsKey(type, chatId).let {
            propertyTimeoutSettingsDAO.put(it, timeout)
            cachedStorage.put(it, timeout)
        }
    }

    override fun get(type: ContentType, chatId: String): Long {
        return TimeSettingsKey(type, chatId).let { cachedStorage.get(it) {propertyTimeoutSettingsDAO.get(it) ?: -1} }
    }


    override fun getInfo(chatId: String): String {
        return Stream.of(*ContentType.values()).map { type -> "Lifetime of $type =" + get(type, chatId).toString() }
            .collect(Collectors.joining("\n"))
    }


}

inline fun <T> lock(lock: Lock,action: () -> T): T {
    lock.lock()
    val res: T
    try {
       res = action()
    }
    finally {
        lock.unlock()
    }
    return res
}

data class TimeSettingsKey(val contentType: ContentType, val chatId: String)