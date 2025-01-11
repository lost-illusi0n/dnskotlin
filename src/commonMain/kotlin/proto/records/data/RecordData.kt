package dev.sitar.dns.proto.records.data

import kotlinx.io.Sink

public sealed class ResourceData {
    public abstract fun marshall(output: Sink)
}