package com.junzizhidao.channel

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener

internal fun ChannelFuture.scheduleClose(keepAlive: Boolean) {
    if (!keepAlive) {
        addListener(ChannelFutureListener.CLOSE);
    }
}