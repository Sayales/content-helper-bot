package com.sayales.bot.processor

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.sayales.bot.BotExecutor
import com.sayales.bot.ContentTimeSettingsController
import com.sayales.bot.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

interface MessageProcessor {
    fun pushMessage(message: Message, botExecutor: BotExecutor);
}

@Component
class CacheBasedMessageProcessor(@Autowired val timeSettingsController: ContentTimeSettingsController) : MessageProcessor {


    private val messageMap = ConcurrentHashMap<Int, ProcessableMessage>()

    //TODO: threadpool
    override fun pushMessage(message: Message, botExecutor: BotExecutor) {
        messageMap[message.messageId] = ProcessableMessage(message, ::deleteMessage)
        val deleteTime = timeSettingsController.getContentTime(getContentType(message))
        if (deleteTime >= 0) {
            Timer().schedule(deleteTime) {
                messageMap[message.messageId]?.let { it.processCallback.invoke(botExecutor, it.message) }
            }
        }
    }

    private fun deleteMessage(botExecutor: BotExecutor, message: Message) {
        val deleteMessage = DeleteMessage()
        deleteMessage.chatId = message.chatId.toString()
        deleteMessage.messageId = message.messageId
        botExecutor.execute(deleteMessage)
    }


    fun getContentType(message: Message) = when {
        message.hasVideo() -> ContentType.VIDEO
        message.hasAnimation() -> ContentType.GIF
        message.hasPhoto() -> ContentType.PHOTO
        message.hasSticker() -> ContentType.STICKER
        else -> ContentType.TEXT
    }
}

class ProcessableMessage(val message: Message, val processCallback: (botExecutor: BotExecutor, message: Message) -> Unit)