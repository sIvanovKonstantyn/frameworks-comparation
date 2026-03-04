package com.example.task;

import com.example.task.db.JdbcFactory;
import com.example.task.event.KafkaFactory;
import com.example.task.tracing.TracingFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import org.pragmatica.http.routing.RequestContext;
import org.pragmatica.http.routing.RequestRouter;
import org.pragmatica.http.routing.Route;
import org.pragmatica.json.JsonMapper;
import org.pragmatica.lang.Option;
import org.pragmatica.lang.Result;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.pragmatica.http.routing.HttpMethod.valueOf;
import static org.pragmatica.http.routing.RequestContext.RequestContextImpl.requestContext;

public class App {
    private static final JsonMapper JSON = JsonMapper.defaultJsonMapper();
    private static final int PORT = 8080;
    private static final int MAX_CONTENT_LENGTH = 1048576;
    private static final int BOSS_THREADS = 1;

    static void main(String[] args) throws InterruptedException {
        ApplicationConfiguration.load(args)
            .onFailure(cause -> {
                System.err.println(cause.message());
                System.exit(1);
            })
            .onSuccess(config -> {
                var tracer = TracingFactory.create(config.serviceName(), config.zipkinUrl());
                var jdbc = JdbcFactory.create(config, tracer);
                var eventPublisher = KafkaFactory.createPublisher(config.kafkaBootstrapServers(), config.kafkaTopic(), tracer);
                var taskSlice = TaskSlice.taskSlice(jdbc, eventPublisher);

                var router = RequestRouter.with(
                    Route.post("/api/v1/tasks")
                         .withBody(TaskSlice.CreateTaskRequest.class)
                         .to(body -> traceEndpoint(tracer, "POST /api/v1/tasks", () -> taskSlice.createTask(body).map(r -> r)))
                         .asJson(),
                    Route.get("/api/v1/tasks")
                         .to((rq) -> traceEndpoint(tracer, "GET /api/v1/tasks", () -> taskSlice.getAllTasks().map(r -> r)))
                         .asJson()
                );

                router.print();

                try {
                    startServer(router, System.currentTimeMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
    }

    private static void startServer(RequestRouter router, long startTime) throws InterruptedException {
        var bossGroup = new MultiThreadIoEventLoopGroup(BOSS_THREADS, NioIoHandler.newFactory());
        var workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(createInitializer(router));

            var channel = bootstrap.bind(PORT).sync().channel();
            var startupTime = System.currentTimeMillis() - startTime;
            System.out.println("Server started on http://localhost:" + PORT);
            System.out.println("Application started in " + startupTime + "ms");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static ChannelInitializer<SocketChannel> createInitializer(RequestRouter router) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                  .addLast(new HttpServerCodec())
                  .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                  .addLast(new TaskHandler(router));
            }
        };
    }

    static class TaskHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private static final String ERROR_NOT_FOUND = "Not Found";
        private static final String ERROR_TASK_CREATION_FAILED = "Failed to create task";
        private static final String ERROR_SERIALIZATION_FAILED = "{\"error\":\"Serialization failed\"}";
        
        private final RequestRouter router;

        TaskHandler(RequestRouter router) {
            this.router = router;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            resolveRoute(request)
                .onEmpty(() -> sendNotFound(ctx))
                .onPresent(route -> handleRoute(ctx, route, buildContext(request, route)));
        }

        private Option<Route<?>> resolveRoute(FullHttpRequest request) {
            var method = valueOf(request.method().name());
            return router.findRoute(method, request.uri());
        }

        private RequestContext buildContext(FullHttpRequest request, Route<?> route) {
            return requestContext(
                request,
                route,
                org.pragmatica.http.routing.JsonCodecAdapter.defaultCodec(),
                java.util.UUID.randomUUID().toString()
            );
        }

        private void handleRoute(ChannelHandlerContext ctx, Route<?> route, RequestContext context) {
            route.handler()
                .handle(context)
                .onResult(handlerResult -> processHandlerResult(ctx, handlerResult));
        }

        private void processHandlerResult(ChannelHandlerContext ctx, Result<?> handlerResult) {
            if (handlerResult.isSuccess()) {
                var domainResult = handlerResult.unwrap();
                if (domainResult instanceof Result<?> result) {
                    processDomainResult(ctx, result);
                } else {
                    sendSuccess(ctx, domainResult);
                }
            } else {
                handlerResult.onFailure(cause -> sendClientError(ctx, cause.message()));
            }
        }

        private void processDomainResult(ChannelHandlerContext ctx, Result<?> domainResult) {
            domainResult
                .onSuccess(payload -> sendSuccess(ctx, payload))
                .onFailure(cause -> sendServerError(ctx, ERROR_TASK_CREATION_FAILED));
        }

        private void sendNotFound(ChannelHandlerContext ctx) {
            sendJsonResponse(ctx, NOT_FOUND, error(ERROR_NOT_FOUND));
        }

        private void sendSuccess(ChannelHandlerContext ctx, Object payload) {
            sendJsonResponse(ctx, OK, payload);
        }

        private void sendClientError(ChannelHandlerContext ctx, String message) {
            sendJsonResponse(ctx, BAD_REQUEST, error(message));
        }

        private void sendServerError(ChannelHandlerContext ctx, String message) {
            sendJsonResponse(ctx, INTERNAL_SERVER_ERROR, error(message));
        }

        private ErrorResponse error(String message) {
            return new ErrorResponse(message);
        }

        private void sendJsonResponse(ChannelHandlerContext ctx, HttpResponseStatus status, Object obj) {
            JSON.writeAsString(obj)
                .onSuccess(json -> sendResponse(ctx, status, json))
                .onFailure(cause -> sendResponse(ctx, INTERNAL_SERVER_ERROR, ERROR_SERIALIZATION_FAILED));
        }

        private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
            var response = new DefaultFullHttpResponse(HTTP_1_1, status,
                ctx.alloc().buffer().writeBytes(content.getBytes(StandardCharsets.UTF_8)));
            response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
                .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    record ErrorResponse(String error) {}
    
    private static <R> org.pragmatica.lang.Promise<R> traceEndpoint(brave.Tracer tracer, String endpoint, java.util.function.Supplier<org.pragmatica.lang.Promise<R>> supplier) {
        var span = tracer.nextSpan().name(endpoint).start();
        try (var scope = tracer.withSpanInScope(span)) {
            return supplier.get().onResult(result -> span.finish());
        }
    }
}
