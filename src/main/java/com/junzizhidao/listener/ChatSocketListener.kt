package com.junzizhidao.listener

import com.junzizhidao.handler.HttpHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler

class ChatSocketListener(port: Int) {
    private var netHost = "localhost"

    private val port = port
    private val host: String
        get() = netHost

    constructor(host: String, port: Int) : this(port) {
        this.netHost = host
    }

    fun run() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()

        try {
            val bootStrap: ServerBootstrap = ServerBootstrap().apply {
                group(bossGroup, workerGroup)
                channel(NioServerSocketChannel::class.java)
                childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        with(ch.pipeline()) {
                            addLast(HttpServerCodec())
                            addLast(HttpObjectAggregator(1048576))
                            addLast(ChunkedWriteHandler())
                            addLast(HttpHandler())
                        }
                    }
                })
                option(ChannelOption.SO_BACKLOG, 128)
                childOption(ChannelOption.SO_KEEPALIVE, true)
            }

            println("starting server...")

            val future: ChannelFuture = bootStrap.bind(host, port).sync()

            println("server started listening on $host:$port")

            future.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

}