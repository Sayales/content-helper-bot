package com.sayales.app

import com.sayales.bot.Bot
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.*
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import java.net.Authenticator
import java.net.PasswordAuthentication


@SpringBootConfiguration
@ComponentScan(basePackages = arrayOf("com.sayales.*"))
class App

@Configuration
@PropertySources(PropertySource("classpath:commands.properties"), PropertySource("classpath:proxy.properties"), PropertySource("classpath:bot.properties"))
class Config


public fun main(args: Array<String>) {
    ApiContextInitializer.init()
    SpringApplication.run(App::class.java, *args);
}


