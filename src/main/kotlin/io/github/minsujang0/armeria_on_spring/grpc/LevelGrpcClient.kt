package io.github.minsujang0.armeria_on_spring.grpc

import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.ProtoClient
import io.github.minsujang0.level_service.v1.LevelServiceGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LevelGrpcClient {
    @Bean
    fun levelGrpcStub(): LevelServiceGrpc.LevelServiceBlockingV2Stub {
        return ProtoClient<LevelServiceGrpc.LevelServiceBlockingV2Stub>("http://level-service:8080")
    }
}