# gArmeria: Spring Boot에서 Armeria gRPC로의 점진적 전환 샘플 프로젝트

본 프로젝트는 기존의 일반적인 Spring Boot 웹 애플리케이션을 어떻게 Armeria 기반의 고성능 gRPC 서버로 점진적으로 전환하거나 통합할 수 있는지에 대한 청사진을 제시하는 포트폴리오 샘플입니다.

## 🚀 프로젝트 목표

많은 기업들이 이미 수많은 Spring Boot 기반의 애플리케이션을 운영하고 있습니다. 마이크로서비스 아키텍처(MSA)로 전환하거나 서비스 간 통신 성능을 극대화하기 위해 gRPC 도입을 고려할 때, "기존의 잘 동작하는 코드를 어떻게 gRPC와 자연스럽게 융합시킬 수 있을까?"라는 큰 과제에 직면하게 됩니다.

이 프로젝트는 다음과 같은 해법을 제시하며, **자연스러운 gRPC 서버 개발 플로우**를 구축하는 데 주안점을 둡니다.

1.  **점진적 도입**: 기존 Spring Boot의 `ApplicationContext`와 Bean들을 그대로 활용하면서 Armeria 서버를 통합하여, 전체 애플리케이션을 한 번에 재작성할 필요 없이 새로운 gRPC 서비스를 추가하거나 기존 REST API를 gRPC로 전환할 수 있습니다.
2.  **개발 경험의 통일성**: Armeria를 처음 접하는 개발자도 기존 Spring 개발 방식처럼 편안하게 gRPC 서비스를 개발할 수 있도록 표준화된 유틸리티와 워크플로우를 제공합니다.
3.  **MSA를 위한 강력한 기반**: MSA 환경에서 필수적인 서비스 간 오류 전파와 추적을 위한 강력하고 체계적인 예외 처리 매커니즘을 내장합니다.

---

## 🏛️ 핵심 아키텍처 및 특징

-   **하이브리드 서버**: 하나의 프로세스에서 Spring Boot의 내장 Tomcat(또는 Netty)과 Armeria 서버가 함께 실행됩니다. 기존 REST API는 Spring Webflux/MVC를 통해, 새로운 gRPC 서비스는 Armeria를 통해 제공됩니다.
-   **Spring Bean 재사용**: Armeria의 gRPC 서비스(`BindableService`) 구현체는 Spring의 `@Component`로 등록된 기존 서비스(`@Service`)나 레포지토리(`@Repository`) Bean들을 `@Autowired`하여 그대로 사용할 수 있습니다.
-   **표준화된 개발 워크플로우**: 아래에 설명된 커스텀 유틸리티 스위트를 통해 gRPC 서비스 개발, 예외 처리, 클라이언트 생성을 표준화하여 생산성을 높입니다.

---

## ✨ 표준화된 gRPC 개발 워크플로우 유틸리티

본 프로젝트는 안정적이고 효율적인 gRPC 서비스 개발을 위해 직접 구현한 유틸리티 스위트를 포함합니다.

### 1. 컨트롤러/서비스 로직 감싸기 (`ControllerUtil.kt`)

모든 gRPC 서비스 메소드는 예외를 자동으로 처리하기 위해 `withEventLoopCatching` 또는 `returnCatching`으로 감싸야 합니다.

-   **Suspending 함수 (Coroutine 기반):**
    `withEventLoopCatching`을 사용하여 Armeria의 이벤트 루프에서 비동기 코드를 안전하게 실행합니다.

    ```kotlin
    // ExampleService.kt
    override suspend fun getUser(request: GetUserRequest): GetUserResponse {
        return withEventLoopCatching { // Coroutine-based
            val user = userService.find(request.id) // userService is an injected Spring Bean
            user.toResponse()
        }
    }
    ```

### 2. 예외 포착 및 변환 (`GrpcExceptionCatch.kt`)

`...Catching` 헬퍼 함수들은 내부적으로 `blockingCatch` 또는 `suspendingCatch`를 호출합니다. 이 함수들은 `try-catch` 블록으로 비즈니스 로직을 감싸고, 어떤 `Throwable`이든 포착하여 `onCatch` 함수로 전달합니다. `onCatch` 함수는 예외의 발생 원인(Context)에 따라 적절한 `GrpcException`으로 변환 후 다시 던집니다.

#### `GrpcException`의 종류와 용도

-   **`GrpcServerException` (자동 생성):** 컨트롤러 메소드 내에서 직접 발생한 예외(e.g., `IllegalStateException`)를 감쌀 때 사용됩니다.
-   **`InternalException` (수동 생성):** 서버 내부의 다른 레이어(e.g., Service)에서 발생한 예외를 개발자가 **의도적으로** 감싸서 상위 레이어로 전달할 때 사용합니다.
-   **`GrpcClientCallException` (자동 생성):** 다운스트림 gRPC 서비스 호출 시 발생한 `StatusRuntimeException`을 감쌀 때 사용됩니다.
-   **`GrpcErrorException` (수동 생성):** 특정 오류를 즉시 반환하고 싶을 때 개발자가 직접 사용할 수 있습니다.

### 3. 서버 예외 처리 및 전파 (`GrpcExceptionHandler.kt`)

`onCatch`에서 던져진 `GrpcException`은 Armeria 서버에 의해 포착되어, `googleRpcStatusExceptionHandler`에 의해 gRPC 응답의 트레일러(metadata)에 에러 정보가 기록됩니다.

**[중요] 서버 설정:**
이 과정을 단순화하기 위해 `GrpcService` 팩토리 함수를 제공합니다. 이 함수는 예외 핸들러, Protobuf 리플렉션 등 유용한 옵션들을 기본적으로 활성화합니다.

```kotlin
// ArmeriaServerConfiguration.kt
serverBuilder.service(
    GrpcService(
        myGrpcService, // This can be a Spring Bean
        useBlocking = true,
        useHttpJsonTranscoding = true,
        useUnframedRequests = true
    )
)
```

### 4. 연쇄 호출에서 전파된 오류 처리

`errorMapCatch` 같은 헬퍼를 사용해 다운스트림 서비스에서 전파된 오류를 현재 서비스의 컨텍스트에 맞는 오류로 손쉽게 변환할 수 있습니다. 이 매커니즘을 통해, 연쇄 호출의 가장 끝에서 발생한 구체적인 오류가 모든 호출 스택에 걸쳐 컨텍스트를 잃지 않고 명확하게 전파됩니다.

---

## 🛠️ 기타 유틸리티

### Protobuf <-> Kotlin/Java 타입 변환 (`ProtobufExtensions.kt`)

`com.google.protobuf.Value`, `Struct`, `Timestamp` 등과 표준 타입 간의 변환을 쉽게 해주는 확장 함수들을 제공합니다.

### HTTP 헤더 처리 (`HttpHeaderUtil.kt`, `GrpcClientUtil.kt`)

-   `userIdFromHeader`: `BindableService` 컨텍스트 내에서 `X-User-Id` 헤더를 쉽게 추출합니다.
-   `ProtoClient`: gRPC 클라이언트 생성 시, 현재 요청의 `X-User-Id` 헤더를 다음 gRPC 요청에 자동으로 전파(propagate)하는 데코레이터를 포함합니다. 