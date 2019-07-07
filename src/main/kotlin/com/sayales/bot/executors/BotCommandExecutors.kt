package com.sayales.bot.executors

import com.sayales.bot.BotExecutor
import com.sayales.bot.ContentTimeSettingsService
import com.sayales.bot.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.stream.Collectors
import java.util.stream.Stream


abstract class CommandExecutor(val command: Commands) {

    abstract fun  execute(message: Message, botExecutor: BotExecutor): Any

    fun getCommand(): String {
       return command.cmd
    }

}


@Component
class TimeCommandExecutor(@Autowired val contentTimeSettingsService: ContentTimeSettingsService): CommandExecutor(Commands.Time) {


    override fun execute(message: Message, botExecutor: BotExecutor) {
        if (!botExecutor.checkGroupAdmin(message))
            return
        val timeSetResult = contentTimeSettingsService.setContentTime(message.text
            .replace(command.cmd, "").trim(), message.chatId.toString());
        botExecutor.sendMessage(message.chatId.toString(), timeSetResult)
    }

}

@Component
class StartCommandExecutor : CommandExecutor(Commands.Start) {

    override fun execute(message: Message, botExecutor: BotExecutor) {
         botExecutor.sendMessage(message.chatId.toString(), Stream.of(*Commands.values()).map{it.cmd}.collect(Collectors.joining("\n")))
    }

}

//TODO: info problem
@Component
class InfoCommandExecutor : CommandExecutor(Commands.Info) {
    override fun execute(message: Message, botExecutor: BotExecutor) {
        botExecutor.sendMessage(message.chatId.toString(),  Stream.of(*Commands.values()).map{it.desc()}.collect(Collectors.joining("\n")))
    }
}


enum class Commands(val cmd: String) {
    Time("/time"){
        override fun desc(): String {
            return "Set lifetime in seconds for content: \n  $cmd ${Stream.of(*ContentType.values()).map{it.name.toLowerCase()}.collect(Collectors.joining(" "))} 500"
        }

    },
    Info("/info") {
        override fun desc(): String {
            return cmd
        }
    },
    Start("/start") {
        override fun desc(): String {
            return cmd
        }
    };

    abstract fun desc(): String
}


