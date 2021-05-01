package com.junzizhidao.server

import com.junzizhidao.listener.ChatSocketListener

fun main(args: Array<String>) {
    ChatSocketListener(8080).run();
}