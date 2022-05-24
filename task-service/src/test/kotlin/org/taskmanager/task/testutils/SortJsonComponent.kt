package org.taskmanager.task.testutils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.node.ArrayNode
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Order
import java.io.IOException

class SortJsonComponent {

    class SortSerializer : JsonSerializer<Sort>() {

        @Throws(IOException::class)
        override fun serialize(value: Sort, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartArray()
            value.iterator().forEachRemaining { v ->
                try {
                    gen.writeObject(v)
                } catch (e: IOException) {
                    throw EncodeException("Couldn't serialize object $v")
                }
            }
            gen.writeEndArray()
        }

        override fun handledType(): Class<Sort> = Sort::class.java
    }

    class SortDeserializer : JsonDeserializer<Sort>() {

        @Throws(IOException::class)
        override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Sort? {
            val treeNode: TreeNode = jsonParser.getCodec().readTree(jsonParser)
            if (treeNode.isArray()) {
                val arrayNode = treeNode as ArrayNode
                val orders = mutableListOf<Order>()
                for (jsonNode in arrayNode) {
                    val order = Order(
                        Direction.valueOf(jsonNode.get("direction").textValue()),
                        jsonNode.get("property").textValue()
                    )
                    orders.add(order)
                }
                return Sort.by(orders)
            }
            return null
        }

        override fun handledType(): Class<Sort> = Sort::class.java
    }
}