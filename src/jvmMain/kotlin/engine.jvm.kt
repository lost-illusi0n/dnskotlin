package dev.sitar.dns

import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import java.net.http.HttpClient

internal actual val DEFAULT_ENGINE: HttpClientEngine
    get() = Java.create { protocolVersion = HttpClient.Version.HTTP_2 }