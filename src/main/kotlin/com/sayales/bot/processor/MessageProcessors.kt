package com.sayales.bot.processor

import com.sayales.bot.BotExecutor
import com.sayales.bot.ContentTimeSettingsService
import com.sayales.bot.ContentType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface MessageProcessor {
    fun pushMessage(message: Message, botExecutor: BotExecutor);
}

@Component
class CacheBasedMessageProcessor(@Autowired val timeSettingsService: ContentTimeSettingsService) : MessageProcessor {


    private val logger = LoggerFactory.getLogger(MessageProcessor::class.java)

    private val messageMap = ConcurrentHashMap<Int, ProcessableMessage>()

    private val scheduler = Executors.newScheduledThreadPool(50);


    override fun pushMessage(message: Message, botExecutor: BotExecutor) {
        messageMap[message.messageId] = ProcessableMessage(message, ::deleteMessage)
        val liveTime = timeSettingsService.getContentTime(getContentType(message), message.chatId.toString())
        if (liveTime >= 0) {
            scheduler.schedule({
                messageMap[message.messageId]?.let {
                    it.processCallback.invoke(botExecutor, it.message)
                }
            }, liveTime, TimeUnit.SECONDS);
        }
    }

    private fun deleteMessage(botExecutor: BotExecutor, message: Message) {
        logger.info("Delete message $message")
        val deleteMessage = DeleteMessage()
        deleteMessage.chatId = message.chatId.toString()
        deleteMessage.messageId = message.messageId
        botExecutor.execute(deleteMessage)
    }


    fun getContentType(message: Message) = when {
        message.hasVideo() -> ContentType.VIDEO
        message.hasPhoto() -> ContentType.PHOTO
        message.hasAnimation() -> ContentType.GIF
        message.hasVideoNote() -> ContentType.VIDEO_NOTE
        message.hasSticker() -> ContentType.STICKER
        else -> ContentType.TEXT
    }
}

class ProcessableMessage(val message: Message, val processCallback: (botExecutor: BotExecutor, message: Message) -> Unit)