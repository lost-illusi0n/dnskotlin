package dev.sitar.dns.records.data

import dev.sitar.kio.buffers.SequentialWriter

public sealed class ResourceData {
    public abstract fun marshall(output: SequentialWriter)
}