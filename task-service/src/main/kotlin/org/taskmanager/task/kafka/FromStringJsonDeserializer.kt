package org.taskmanager.task.kafka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException

/**
 * Unwrap a String in JSON that in turn contains JSON
 */
class FromStringJsonDeserializer<T> : StdDeserializer<T>, ContextualDeserializer {
    /**
     * Required by library to instantiate base instance
     */
    constructor() : super(Any::class.java)
    constructor(type: JavaType) : super(type)

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        val value: String = p.valueAsString
        return (p.codec as ObjectMapper).readValue(value, _valueType)
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
        return FromStringJsonDeserializer<T>(property.type)
    }
}