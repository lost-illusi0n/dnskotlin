package dev.sitar.dns.records.data

import dev.sitar.dns.records.decompressName
import dev.sitar.kio.Slice
import dev.sitar.kio.buffers.SequentialReader
import dev.sitar.kio.buffers.SequentialWriter
import dev.sitar.kio.buffers.writeBytes

public sealed class ResourceData {
    public abstract fun marshall(output: SequentialWriter)
}