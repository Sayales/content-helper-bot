package com.sayales.bot.storage.dao

interface KeyValueStorage<K, V> {

    fun save(key: K, value: V)

    fun load(key: K) : V?

}