package com.sayales.bot


import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors


private val logger = LoggerFactory.getLogger("com.sayales.bot.SettingsManagers")


interface ContentTimeSettingsController
{
    fun setContentTime(cmd: String): String
    fun getContentTime(type: ContentType): Long
    fun getInfo(): String
}

interface TimeoutSettingsStorage
{
    fun put(type: ContentType, timeout: Long)
    fun get(type: ContentType) : Long
    fun getInfo(): String
}

@Component
class CommandSettingsController(@Autowired val storage: TimeoutSettingsStorage): ContentTimeSettingsController
{

    override fun setContentTime(cmd: String): String {
        val tokens = cmd.split(" ")
        return when {
            tokens.size == 1 -> tryInfoCommand(tokens)
            tokens.size == 2 -> setContentTime(tokens[0], tokens[1])
            else ->"Wrong command $cmd"
        }
    }

    override fun getContentTime(type: ContentType): Long {
        return storage.get(type) ?: 0
    }

    override fun getInfo(): String {
        return storage.getInfo() ?: "";
    }

    fun setContentTime(typeName: String, timeout: String): String {
        return try {
            storage.put(ContentType.valueOf(typeName.toUpperCase()), timeout.toLong())
            logger.info("Stored settings for $typeName, timeout = $timeout")
            "New timeout for $typeName, timeout = $timeout"
        } catch (e: Exception) {
            logger.error("Invalid command for $typeName, timeout = $timeout", e)
            "Invalid command for $typeName, timeout = $timeout"
        }
    }

    private fun tryInfoCommand(tokens: List<String>) = when {
        tokens[0] == "info" -> getInfo()
        else -> "Wrong command $tokens"
    }
}



@Component
class MemorySettingStorage : TimeoutSettingsStorage{

    private val storage = ConcurrentHashMap<ContentType, Long>()


    override fun put(type: ContentType, timeout: Long) {
        storage[type] = timeout
    }

    override fun get(type: ContentType): Long {
        return storage[type] ?: -1
    }

    override fun getInfo(): String {
        return storage.entries.stream().map{ "${it.key} : ${it.value} msc"}.collect(Collectors.joining("\n"))
    }

}