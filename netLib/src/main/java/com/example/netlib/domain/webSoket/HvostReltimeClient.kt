package com.example.netlib.domain.webSoket

import kotlinx.coroutines.flow.Flow

interface HvostRealtimeClient {

    val events: Flow<Any>

    val isConnected: Boolean

    /** Подключиться и передать токен (?token=...). Вызывается после входа. */
    suspend fun connect(token: String)

    /** Закрыть соединение. Вызывается при выходе из аккаунта. */
    suspend fun disconnect()

    /** Проверка связи: клиент шлёт {"type":"ping"}, ждёт "pong". */
    suspend fun ping()

    /** Подписка на живые слоты врача/даты. */
    suspend fun subscribeSlots(doctor: String, date: String)

    /**
     * Отписка.
     * Без аргументов — отписаться от всех.
     * Только doctor — от всех дат этого врача.
     * Только date — от всех врачей этой даты.
     * Оба — конкретная пара.
     */
    suspend fun unsubscribeSlots(doctor: String? = null, date: String? = null)

    /** Демонстрация очереди по предстоящей записи. */
    suspend fun requestQueueDemo(appointmentId: String)
}
