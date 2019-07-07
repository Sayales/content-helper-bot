package com.sayales.bot.storage.dao

import com.sayales.bot.ContentType
import com.sayales.bot.storage.ao.TimeSettingsKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock

interface TimeoutSettingsDAO {
    fun put(key: TimeSettingsKey, timeout: Long)
    fun get(key: TimeSettingsKey): Long?

}

@Component
class PropertyTimeoutSettingsDAO(@Autowired val lifetimePropertyStorage: KeyValueStorage<TimeSettingsKey, Long>) : TimeoutSettingsDAO {

    override fun put(key: TimeSettingsKey, timeout: Long) {
        lifetimePropertyStorage.save(key, timeout)
    }

    override fun get(key: TimeSettingsKey): Long? {
        return lifetimePropertyStorage.load(key)
    }

}