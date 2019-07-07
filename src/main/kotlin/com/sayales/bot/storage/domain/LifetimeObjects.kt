package com.sayales.bot.storage.domain

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.sayales.bot.ContentType

data class TimeSettingsKey(val contentType: ContentType, val chatId: String)
