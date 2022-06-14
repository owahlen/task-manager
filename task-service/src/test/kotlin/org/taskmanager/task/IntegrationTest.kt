package org.taskmanager.task

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.lang.annotation.Inherited


@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
//@AutoConfigureWireMock(port = 8081) // TODO: for now hardcoded in order to fix create 'issuer-uri' response dynamically based on injected port.
annotation class IntegrationTest