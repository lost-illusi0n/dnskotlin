package dev.sitar.dns

import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*

internal actual val DEFAULT_ENGINE: HttpClientEngine
    get() = Curl.create()