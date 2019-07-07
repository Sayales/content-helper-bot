package com.sayales.bot

import com.sayales.app.SecuredValue
import com.sayales.bot.executors.CommandExecutor
import com.sayales.bot.processor.MessageProcessor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.ChatMember
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.Serializable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

interface BotExecutor
{
    @Throws(TelegramApiException::class)
    fun <T : Serializable, Method : BotApiMethod<T>> execute(method: Method): T

    fun checkGroupAdmin(message: Message) : Boolean

    fun sendMessage(chatId: String, msg: String) {
        if (msg.isEmpty())
            return
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(false)
        sendMessage.chatId = chatId
        sendMessage.text = msg
        execute(sendMessage)
    }
}

interface CommandManager {
    fun execute(message: Message)
}

//TODO: Webhook maybe
//TODO: Safe secret storage
@Component
class Bot(@Autowired propertyOptionsProvider: OptionsProvider,
          @Autowired executors: List<CommandExecutor>,
          @Autowired val messageProcessor: MessageProcessor) :
    TelegramLongPollingBot(propertyOptionsProvider.getOptions()), BotExecutor, CommandManager {

    private val logger = LoggerFactory.getLogger(Bot::class.java)

    private lateinit var executor: ExecutorService

    @Value("\${bot.username}")
    private val botName: String = ""

    @SecuredValue("\${bot.token}", "BOT_TOKEN_KEY")
    private val token: String = ""

    @Value("\${bot.poolSize}")
    private val threads: Int = 1

    private val executorsMap = HashMap<String, CommandExecutor>()

    init {
        executors.forEach { executorsMap[it.getCommand()] = it }
    }

    @PostConstruct
    fun postConstruct(){
        executor = Executors.newFixedThreadPool(threads)
        TelegramBotsApi().registerBot(this)
    }

    override fun getBotUsername(): String {
       return  botName
    }

    override fun getBotToken(): String {
        return token
    }

    override fun onUpdateReceived(update: Update?) {
        executor.execute {doUpdateReceived(update)}
    }

    private fun doUpdateReceived(update: Update?) {
        logger.info("Update $update received")
        if (update?.hasMessage() == true) {
            try {
                val message = update.message
                when {
                    message.isCommand -> execute(message)
                    else -> messageProcessor.pushMessage(message, this)
                }
            } catch (e: Exception) {
                logger.error("Error!", e)
                sendMessage(
                    update.message.chatId.toString(),
                    e.message ?: "On update exception ${e::class.java.simpleName}"
                );
            }

        }

    }

    @Throws(UnknownCommandException::class)
    override fun execute(message: Message) {
        if (!checkGroupAdmin(message))
            return
        val cmdList = message.text.split(" ")
        if (cmdList.isEmpty()) {
            throw UnknownCommandException("Empty command test")
        }
        val command = cmdList[0]
        this.executorsMap[command]?.execute(message, this) ?: throw UnknownCommandException("Unknown command")
    }

    override fun checkGroupAdmin(message: Message): Boolean {
        if (message.isUserMessage)
            return true
        val chatAdmins = getChatAdmins(message.chatId.toString())
        return chatAdmins.any { it.user.id == message.from.id }
    }

    fun getChatAdmins(chatId: String):  ArrayList<ChatMember>  {
        val getChatAdmins = GetChatAdministrators()
        getChatAdmins.chatId = chatId
        return execute(getChatAdmins)
    }

}

class UnknownCommandException(msg: String) : Exception(msg)



enum class ContentType {
    TEXT,
    VIDEO,
    VIDEO_NOTE,
    GIF,
    STICKER,
    PHOTO
}


