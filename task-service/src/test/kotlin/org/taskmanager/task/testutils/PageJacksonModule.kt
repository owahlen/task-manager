package org.taskmanager.task.testutils

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * This Jackson module provides support to deserialize Spring [Page] objects.
 * Based on https://github.com/spring-cloud/spring-cloud-openfeign/blob/main/spring-cloud-openfeign-core/src/main/java/org/springframework/cloud/openfeign/support/PageJacksonModule.java
 *
 * @author Pascal Büttiker
 * @author Olga Maciaszek-Sharma
 * @author Pedro Mendes
 * @author Nikita Konev
 * @author Oliver Wahlen
 */
class PageJacksonModule : Module() {

    override fun getModuleName() = "PageJacksonModule"

    override fun version() = Version(0, 1, 0, "", null, null)

    override fun setupModule(context: SetupContext) =
        context.setMixInAnnotations(Page::class.java, PageMixIn::class.java)

    @JsonDeserialize(`as` = SimplePageImpl::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface PageMixIn

    internal class SimplePageImpl<T>(
        @JsonProperty("content") content: MutableList<T>,
        @JsonProperty("number") number: Int,
        @JsonProperty("size") size: Int,
        @JsonProperty("totalElements") @JsonAlias("total-elements", "total_elements", "totalelements", "TotalElements") totalElements: Long,
        @JsonProperty("sort") sort: Sort?
    ) : Page<T> {
        private var delegate: Page<T>

        init {
            if (size > 0) {
                val pageRequest: PageRequest
                if (sort != null) {
                    pageRequest = PageRequest.of(number, size, sort)
                } else {
                    pageRequest = PageRequest.of(number, size)
                }
                delegate = PageImpl(content, pageRequest, totalElements)
            } else {
                delegate = PageImpl(content)
            }
        }

        @JsonProperty
        override fun getTotalPages() = delegate.getTotalPages()

        @JsonProperty
        override fun getTotalElements() = delegate.getTotalElements()

        @JsonProperty
        override fun getNumber() = delegate.getNumber()

        @JsonProperty
        override fun getSize() = delegate.getSize()

        @JsonProperty
        override fun getNumberOfElements() = delegate.getNumberOfElements()

        @JsonProperty
        override fun getContent() = delegate.getContent()

        @JsonProperty
        override fun hasContent() = delegate.hasContent()

        @JsonIgnore
        override fun getSort() = delegate.getSort()

        @JsonProperty
        override fun isFirst() = delegate.isFirst()

        @JsonProperty
        override fun isLast() = delegate.isLast()

        @JsonIgnore
        override fun hasNext() = delegate.hasNext()

        @JsonIgnore
        override fun hasPrevious() = delegate.hasPrevious()

        @JsonIgnore
        override fun nextPageable() = delegate.nextPageable()

        @JsonIgnore
        override fun previousPageable() = delegate.previousPageable()

        @JsonIgnore
        override fun <U : Any?> map(converter: Function<in T, out U>) = delegate.map(converter)

        @JsonIgnore
        override fun iterator() = delegate.iterator()

        @JsonIgnore
        override fun getPageable() = delegate.getPageable()

        @JsonIgnore
        override fun isEmpty() = delegate.isEmpty()

        override fun hashCode() = delegate.hashCode()

        override fun equals(other: Any?) = delegate.equals(other)

        override fun toString() = delegate.toString()
    }
}