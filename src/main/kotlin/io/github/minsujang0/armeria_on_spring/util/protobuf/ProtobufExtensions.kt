package io.github.minsujang0.armeria_on_spring.util.protobuf

import com.google.protobuf.ListValue
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.Value
import java.time.LocalDate
import java.time.LocalDateTime


val com.google.type.Date.localDate: LocalDate
    get() = LocalDate.of(year, month, day)

val Timestamp.localDateTime: LocalDateTime
    get() = LocalDateTime.ofEpochSecond(seconds, nanos, java.time.ZoneOffset.UTC)

val LocalDate.protobufDate: com.google.type.Date
    get() = com.google.type.Date.newBuilder()
        .setYear(this.year)
        .setMonth(this.monthValue)
        .setDay(this.dayOfMonth)
        .build()

val LocalDateTime.protobufTimestamp: Timestamp
    get() = Timestamp.newBuilder()
        .setSeconds(this.toEpochSecond(java.time.ZoneOffset.UTC))
        .setNanos(this.nano)
        .build()

val Any?.protobufValue: Value
    get() = when (this) {
        is String -> Value.newBuilder().setStringValue(this).build()
        is Number -> Value.newBuilder().setNumberValue(this.toDouble()).build()
        is Boolean -> Value.newBuilder().setBoolValue(this).build()
        is List<*> -> Value.newBuilder().setListValue(
            ListValue.newBuilder().fill(this)
        ).build()

        is Map<*, *> -> {
            Value.newBuilder().setStructValue(Struct.newBuilder().fill(this)).build()
        }

        null -> Value.newBuilder().setNullValueValue(0).build()
        else -> throw IllegalArgumentException("Unsupported type: ${this.javaClass}")
    }

val Value.any: Any?
    get() = when {
        hasStringValue() -> stringValue
        hasNumberValue() -> numberValue
        hasBoolValue() -> boolValue
        hasStructValue() -> structValue.map
        hasListValue() -> listValue.valuesList.map { it.any }
        else -> null
    }

fun ListValue.Builder.fill(list: List<*>): ListValue.Builder {
    addAllValues(list.map { it.protobufValue })
    return this
}

fun Struct.Builder.fill(map: Map<*, *>): Struct.Builder {
    map.forEach { (k, v) ->
        if (k is String) {
            this.putFields(k, v.protobufValue)
        } else {
            throw IllegalArgumentException("Key must be a String: $k")
        }
    }
    return this
}

val Map<String, *>.protobufStruct: Struct
    get() = Struct.newBuilder()
        .putAllFields(this.mapValues { (_, any) -> any.protobufValue }).build()

val Struct.map: Map<String, Any?>
    get() = fieldsMap.mapValues { (_, value) -> value.any }

@Suppress("UNCHECKED_CAST")
val Struct.notNullMap: Map<String, Any>
    get() = map.filterValues { it != null } as Map<String, Any>

