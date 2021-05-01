
@file:JvmName("HttpRequestExtensions")


package com.junzizhidao.channel

import io.netty.handler.codec.http.*


internal fun HttpRequest.isKeepAlive() = HttpHeaders.isKeepAlive(this);