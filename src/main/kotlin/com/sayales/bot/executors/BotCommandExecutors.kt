package com.sayales.bot.executors

import com.sayales.bot.Bot
import com.sayales.bot.BotExecutor
import com.sayales.bot.ContentTimeSettingsController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.stream.Collectors




interface CommandExecutor {


    fun  execute(message: Message, botExecutor: BotExecutor): Any

    fun getCommand(): String

}


@Component
class TimeCommandExecutor(@Autowired val contentTimeSettingsController: ContentTimeSettingsController): CommandExecutor {

    @Value("\${bot.timeCommand}")
    private val timeCommand: String = ""


    override fun execute(message: Message, botExecutor: BotExecutor): Unit {
        val timeSetResult = contentTimeSettingsController.setContentTime(message.text.replace(timeCommand, "").trim());
        botExecutor.sendMessage(message.chatId.toString(), timeSetResult)
    }
    override fun getCommand(): String {
        return timeCommand
    }

}

@Component
class StartCommandExecutor : CommandExecutor {

    @Value("\${bot.startCommand}")
    private val commandName: String = ""

    override fun execute(message: Message, botExecutor: BotExecutor) {
         botExecutor.sendMessage(message.chatId.toString(), "Hello!")
    }

    override fun getCommand(): String {
        return commandName;
    }

}