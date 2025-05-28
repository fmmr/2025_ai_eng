package com.vend.fmr.aieng

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class])
@ActiveProfiles("test")
class ApplicationTest {

    @Test
    fun contextLoads() {
        // This test will pass if the Spring context loads successfully
        // No assertions needed - context loading failure will fail the test
    }
}