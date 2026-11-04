package com.example.netlib.websoketForkTest

import com.example.netlib.data.remote.PBApiService
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.webSoket.ClosedEvent
import com.example.netlib.domain.model.webSoket.HelloEvent
import com.example.netlib.domain.model.webSoket.PongEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WsSmokeTest {

    @Test
    fun smoke() = runBlocking {
        // 1. Авторизация через REST — возьми токен.
        //    Подставь свой реальный метод из PBApi. Ниже — вариант,
        //    как если бы он назывался authWithPassword(identity, password).
        val auth = PBApiService.instance.authorization(
            data = AuthRequest(
            identity = "demo@hvost.local",
            password = "Demo2027!",)
        )
        val token = auth.token
        println("token = ${token.take(20)}...")

        // 2. Подключаемся к WS
        val rt = PBApiService.realtime
        val collected = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { rt.events.collect { collected += it } }

        rt.connect(token)

        // 3. Ждём hello (до 5 с)
        waitFor(collected, timeoutMs = 5_000) { it is HelloEvent }
        assertTrue("должен был прийти HelloEvent, получено: $collected",
            collected.any { it is HelloEvent })

        // 4. Проверяем ping → pong
        rt.ping()
        waitFor(collected, timeoutMs = 3_000) { it is PongEvent }
        assertTrue("должен был прийти PongEvent, получено: $collected",
            collected.any { it is PongEvent })

        // 5. Никаких ClosedEvent быть не должно
        assertFalse("неожиданно закрылось: ${collected.filterIsInstance<ClosedEvent>()}",
            collected.any { it is ClosedEvent })

        rt.disconnect()
        job.cancel()
    }

    private suspend fun waitFor(
        list: MutableList<Any>,
        timeoutMs: Long,
        predicate: (Any) -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (list.any(predicate)) return
            delay(50)
        }
    }
}