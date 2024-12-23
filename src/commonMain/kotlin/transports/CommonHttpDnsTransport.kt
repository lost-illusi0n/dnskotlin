package dev.sitar.dns.transports

import dev.sitar.dns.Message
import dev.sitar.kio.buffers.DefaultBufferPool
import dev.sitar.kio.buffers.asBuffer
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public enum class DohRequest {
    Get, Post
}

private val DNS_MESSAGE = ContentType("application", "dns-message")

public class CommonHttpDnsTransport(
    public val client: HttpClient,
    public val request: DohRequest,
    public val timeout: Long
) : DnsTransport {
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun send(to: DnsServer, message: Message): Message? {
        val data = DefaultBufferPool.acquire(12)
        Message.Factory.marshall(data, message)

        val rsp = withTimeoutOrNull(timeout) {
            when (request) {
                DohRequest.Get -> {
                    val (arr, start, n) = data.fullSlice()

                    val payload = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(arr, start, start + n)

                    client.get {
                        url {
                            url(to.host)

                            set(port = to.port, path = "dns-query")
                            parameter("dns", payload)
                        }

                        accept(DNS_MESSAGE)
                    }
                }

                DohRequest.Post -> {
                    client.post {
                        url {
                            url(to.host)

                            set(port = to.port, path = "dns-query")
                        }
                        contentType(DNS_MESSAGE)
                        accept(DNS_MESSAGE)

                        setBody(data.toByteArray())
                    }
                }
            }
        }

        DefaultBufferPool.recycle(data)

        if (rsp == null) return null

        require(rsp.contentType() == DNS_MESSAGE)

        val payload = rsp.readRawBytes().asBuffer()

        return Message.Factory.unmarshall(payload)
    }
}