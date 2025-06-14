package io.github.minsujang0.armeria_on_spring.config

import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.server.tomcat.TomcatService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import io.github.minsujang0.armeria_on_spring.grpc.GreeterCompatGrpcService
import io.github.minsujang0.armeria_on_spring.grpc.GreeterNew2GrpcService
import io.github.minsujang0.armeria_on_spring.grpc.GreeterNewGrpcService
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.GrpcService
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class ArmeriaServerConfiguration {
    @Bean
    @Profile("!local")
    fun tomcatService(applicationContext: ServletWebServerApplicationContext): TomcatService {
        return TomcatService.of(getConnector(applicationContext))
    }

    @Bean
    @Profile("local")
    fun localTomcatService(): TomcatService {
        return TomcatService.of(Tomcat())
    }

    @Bean
    fun armeriaServerConfigurator(
        tomcatService: TomcatService,
        greeterNewGrpcService: GreeterNewGrpcService,
        greeterCompatGrpcService: GreeterCompatGrpcService,
        greeterNew2GrpcService: GreeterNew2GrpcService,
    ): ArmeriaServerConfigurator {
        return ArmeriaServerConfigurator { sb: ServerBuilder ->
            sb.serviceUnder("/", tomcatService)
            sb.decorator(LoggingService.newDecorator())
            sb.accessLogWriter(AccessLogWriter.combined(), false)
            sb.service(
                GrpcService(
                    greeterNewGrpcService,
                    greeterNew2GrpcService,
                    useBlocking = true,
                    useHttpJsonTranscoding = false,
                    useUnframedRequests = false,
                )
            )
            sb.service(
                GrpcService(
                    greeterCompatGrpcService,
                    useBlocking = true,
                    useHttpJsonTranscoding = true,
                    useUnframedRequests = false,
                )
            )
            sb.verboseResponses(true)
        }
    }

    fun getConnector(applicationContext: ServletWebServerApplicationContext): Connector {
        val container = applicationContext.webServer as TomcatWebServer
        container.start()
        return container.tomcat.connector
    }
}