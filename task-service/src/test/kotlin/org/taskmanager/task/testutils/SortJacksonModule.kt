package org.taskmanager.task.testutils

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

import org.springframework.data.domain.Sort;

/**
 * This Jackson module provides support for serializing and deserializing for Spring
 * {@link Sort} object.
 * Based on https://github.com/spring-cloud/spring-cloud-openfeign/blob/main/spring-cloud-openfeign-core/src/main/java/org/springframework/cloud/openfeign/support/SortJacksonModule.java
 *
 * @author Can Bezmen
 * @author Oliver Wahlen
 */
class SortJacksonModule : Module() {

    override fun getModuleName() = "SortModule"

    override fun version() = Version(0, 1, 0, "", null, null)

    override fun setupModule(context: SetupContext) {
        val serializers = SimpleSerializers()
        serializers.addSerializer(Sort::class.java, SortJsonComponent.SortSerializer())
        context.addSerializers(serializers)
        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(Sort::class.java, SortJsonComponent.SortDeserializer())
        context.addDeserializers(deserializers)
    }
}