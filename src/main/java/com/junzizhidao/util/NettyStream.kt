package com.junzizhidao.util

import com.junzizhidao.channel.isKeepAlive
import com.junzizhidao.channel.scheduleClose
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultFileRegion
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.HttpChunkedInput
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.stream.ChunkedStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal class NettyStream(val request: HttpRequest, val context: ChannelHandlerContext) : OutputStream() {
    private val buffer = context.alloc().buffer(8192)

    private var lastContentWritten = false;

    init {

    }

    override fun write(b: Int) {
        require(!lastContentWritten) { "You can't write after the last chunk was written" };

        buffer.writeByte(b);
        if (buffer.writableBytes() == 0) {
            flush();
        }
    }

    override tailrec fun write(b: ByteArray, off: Int, len: Int) {
        require(!lastContentWritten) { "You can't write after the last chunk was written" };

        val toWrite = Math.min(len, buffer.writableBytes());
        if (toWrite > 0) {
            buffer.writeBytes(b, off, toWrite);
            if (buffer.writableBytes() == 0) {
                flush();
            }
            if (toWrite < len) {
                write(b, off + toWrite, len - toWrite);
            }
        }
    }

    fun write(fileName: String) {
        write(File(fileName));
    }

    fun write(file: File) {
        write(file, 0, file.length());
    }

    fun write(file: File, position: Long, count: Long) {
        flush();
        context.write(DefaultFileRegion(file, position, count));
    }

    fun write(stream: InputStream) {
        flush();
        context.write(HttpChunkedInput(ChunkedStream(stream.buffered())));
        lastContentWritten = true;
    }

    override fun flush() {
        if (!lastContentWritten && buffer.readableBytes() > 0) {
            context.writeAndFlush(DefaultHttpContent(buffer.copy()));
            buffer.writerIndex(0);
        }

    }

    override fun close() {
        flush();
        finish();
        buffer.release();
    }

    private fun finish() {
        if (!lastContentWritten) {
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).scheduleClose(request.isKeepAlive());
            lastContentWritten = true;
        } else if (!request.isKeepAlive()) {
            context.close();
        }
    }
}