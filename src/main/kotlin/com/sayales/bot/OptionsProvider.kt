package com.sayales.bot

import com.sayales.app.SecuredValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.generics.BotOptions
import java.net.Authenticator
import java.net.PasswordAuthentication


interface OptionsProvider {

    fun getOptions(): DefaultBotOptions
}

@Component
class PropertyOptionsProvider : OptionsProvider {

    @Value("\${proxy.enable}")
    private  val isProxyEnabled: Boolean = false

    @Value("\${proxy.host}")
    private  val proxyHost: String = ""

    @Value("\${proxy.port}")
    private val proxyPort: Int = 0

    @Value("\${proxy.username}")
    private val proxyUser: String = ""

    @SecuredValue("\${proxy.secret}", "BOT_TOKEN_KEY")
    private val proxySecret: String = ""


    override fun getOptions(): DefaultBotOptions {

        val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)
        if(!isProxyEnabled)
            return botOptions
        Authenticator.setDefault(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(proxyUser, proxySecret.toCharArray());
            }
        })
        botOptions.proxyHost = proxyHost
        botOptions.proxyPort = proxyPort
        botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5
        return botOptions
    }

}