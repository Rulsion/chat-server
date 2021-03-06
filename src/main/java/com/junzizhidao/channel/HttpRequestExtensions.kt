@file:JvmName("HttpResponseExtensions")

package com.junzizhidao.channel



import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponse



internal fun HttpResponse.setContentLength(length: Long) {
    HttpHeaders.setContentLength(this, length);
    this.headers().remove(HttpHeaders.Names.TRANSFER_ENCODING);
}

internal fun HttpResponse.setTransferEncodingChunked() {
    HttpHeaders.setTransferEncodingChunked(this)
    this.headers().remove(HttpHeaders.Names.CONTENT_RANGE);
}

internal fun HttpResponse.setContentRange(first: Long, count: Long, totalLength: Long) {
    this.headers().add(HttpHeaders.Names.CONTENT_RANGE, "bytes $first-${first + count - 1}/$totalLength");
    this.setContentLength(count);
}

internal fun HttpResponse.setKeepAlive(keepAlive: Boolean) = HttpHeaders.setKeepAlive(this, keepAlive);