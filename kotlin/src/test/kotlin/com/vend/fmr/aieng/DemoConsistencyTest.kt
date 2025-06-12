package com.vend.fmr.aieng

import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.web.BaseController
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [Application::class])
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoConsistencyTest {

    @Autowired
    lateinit var allControllers: List<BaseController>

    fun getControllers(): List<BaseController> = allControllers

    fun getExternalDemos(): List<Demo> = Demo.entries.filter { it.external() }
    fun getNonExternalDemos(): List<Demo> = Demo.entries.filterNot { it.external() }

    @ParameterizedTest
    @MethodSource("getExternalDemos")
    fun `external demos should have HTTP URLs`(demo: Demo) {
        assert(demo.route.startsWith("http")) {
            "External demo ${demo.id} should have HTTP URL, but has: ${demo.route}"
        }
    }

    @Test
    fun `should have some external Demos`() = assert(getExternalDemos().isNotEmpty()) { "No External demos" }

    @ParameterizedTest
    @MethodSource("getNonExternalDemos")
    fun `all non-external demos should have correct route pattern`(demo: Demo) {
        val expectedRoute = "/demo/${demo.id}"
        assert(demo.route == expectedRoute) {
            "Demo ${demo.id} should have route '$expectedRoute' but has '${demo.route}'"
        }
    }

    @ParameterizedTest
    @EnumSource(Demo::class)
    fun `all demos should have a controller and the controller should be named correctly`(demo: Demo) {
        val expectedNumberOfControllers = when {
            demo.local() -> 1
            else -> 0
        }
        assert(allControllers.filter { it.demo == demo }.size == expectedNumberOfControllers) {
            "External Demo '${demo.id}' should have not have any controllers, but found ${allControllers.filter { it.demo == demo }.size}"
        }
    }

    @ParameterizedTest
    @MethodSource("getControllers")
    fun `controller names should match expectedControllerName`(controller: BaseController) {
        assert(controller.demo.expectedControllerName() == controller::class.simpleName) {
            "Demo '${controller.demo.id}' should have controller name '${controller.demo.expectedControllerName()}' but has '${controller::class.simpleName}'"
        }
    }


    @ParameterizedTest
    @EnumSource(Demo::class)
    fun `all demos should have non-empty required fields`(demo: Demo) {
        assert(demo.id.isNotBlank()) { "Demo ${demo.id} should have non-empty id" }
        assert(demo.title.isNotBlank()) { "Demo ${demo.id} should have non-empty title" }
        assert(demo.route.isNotBlank()) { "Demo ${demo.id} should have non-empty route" }
        assert(demo.icon.isNotBlank()) { "Demo ${demo.id} should have non-empty icon" }
        assert(demo.emoji.isNotBlank()) { "Demo ${demo.id} should have non-empty emoji" }
    }

    @ParameterizedTest
    @EnumSource(Demo::class)
    fun `all demo routes should be valid`(demo: Demo) {
        if (demo.external()) {
            // External demos should have HTTP URLs
            assert(demo.route.startsWith("http")) {
                "External demo ${demo.id} should have HTTP URL"
            }
        } else {
            // Internal demos should follow /demo/{id} pattern
            assert(demo.route.startsWith("/demo/")) {
                "Internal demo ${demo.id} should have route starting with /demo/"
            }
            assert(demo.route == "/demo/${demo.id}") {
                "Internal demo ${demo.id} should have route '/demo/${demo.id}' but has '${demo.route}'"
            }
        }
    }
}