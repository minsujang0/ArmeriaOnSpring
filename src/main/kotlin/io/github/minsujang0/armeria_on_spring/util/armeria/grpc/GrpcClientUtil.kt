package io.github.minsujang0.armeria_on_spring.util.armeria.grpc

import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.minsujang0.armeria_on_spring.util.armeria.web.USER_ID_HEADER


inline fun <reified T : Any> ProtoClient(httpAddr: String): T {
    return GrpcClients.builder(httpAddr)
        .decorator { delegate, ctx, req ->
            // X-User-Id 헤더를 propagate한다
            ctx.request()?.headers()?.get(USER_ID_HEADER)?.let {
                ctx.addAdditionalRequestHeader(USER_ID_HEADER, it)
            }
            delegate.execute(ctx, req)
        }
        .build(T::class.java)
} 