package com.sayales.bot


import com.sayales.bot.storage.ao.TimeoutSettingsAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


private val logger = LoggerFactory.getLogger("com.sayales.bot.SettingsManagers")


interface ContentTimeSettingsService {
    fun setContentTime(cmd: String, chatId: String): String
    fun getContentTime(type: ContentType, chatId: String): Long
    fun getInfo(chatId: String): String
}


@Service
class CommandSettingsService(@Autowired val propertySettingAO: TimeoutSettingsAO) : ContentTimeSettingsService {

    override fun setContentTime(cmd: String, chatId: String): String {
        val tokens = cmd.split(" ")
        return when {
            tokens.size == 1 -> tryInfoCommand(tokens, chatId)
            tokens.size == 2 -> setContentTime(tokens[0], chatId, tokens[1])
            else -> "Wrong command $cmd"
        }
    }

    override fun getContentTime(type: ContentType, chatId: String): Long {
        return propertySettingAO.get(type, chatId)
    }

    override fun getInfo(chatId: String): String {
        return propertySettingAO.getInfo(chatId);
    }

    fun setContentTime(typeName: String, chatId: String, timeout: String): String {
        return try {
            propertySettingAO.put(ContentType.valueOf(typeName.toUpperCase()), chatId, timeout.toLong())
            logger.info("Stored settings for $typeName, timeout = $timeout")
            "New timeout for $typeName, timeout = $timeout"
        } catch (e: Exception) {
            logger.error("Invalid command for $typeName, timeout = $timeout", e)
            "Invalid command for $typeName, timeout = $timeout"
        }
    }

    private fun tryInfoCommand(tokens: List<String>, chatId: String) = when {
        tokens[0] == "info" -> getInfo(chatId)
        else -> "Wrong command $tokens"
    }
}


