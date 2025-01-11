package dev.sitar.dns.transports

import dev.sitar.dns.proto.Message
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kiso.common.logger
import kiso.log.error
import kiso.log.trace
import kiso.log.warn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public enum class DohRequest {
    Get, Post
}

private val DNS_MESSAGE = ContentType("application", "dns-message")

private val LOG = logger("transport(https)")

public class CommonHttpDnsTransport(
    public val client: HttpClient,
    public val request: DohRequest,
    public val timeout: Long,
    override val preferredPort: Int = APPLICATION_DNS_PORT
) : DnsTransport {
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun send(to: DnsServer, message: Message): Message? {
        val data = Buffer()

        Message.Factory.marshall(data, message)

        LOG.trace { "tx doh $request for ${message.header.id}" }

        val rsp = withTimeoutOrNull(timeout) {
            when (request) {
                DohRequest.Get -> {
                    val payload = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(data.readByteArray())

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

                        setBody(data.readByteArray())
                    }
                }
            }
        }

        if (rsp == null) {
            LOG.warn { "missing rx for ${message.header.id}" }
            return null
        }

        try {
            require(rsp.contentType() == DNS_MESSAGE)

            val payload = Buffer()
            payload.write(rsp.readRawBytes())

            val msg = Message.Factory.unmarshall(payload)

            require(msg.header.id == message.header.id)
            require(msg.header.qr)

            return msg
        } catch (e: Exception) {
            LOG.error { "failed to parse doh rx for ${message.header.id}" }
            return null
        }
    }
}