package com.pinterest.ktlint.ruleset.experimental

import com.pinterest.ktlint.test.KtLintAssertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FunctionNamingRuleTest {
    private val functionNamingRuleAssertThat =
        KtLintAssertThat.assertThatRule { FunctionNamingRule() }

    @Test
    fun `Given a valid function name then do not emit`() {
        val code =
            """
            fun foo1() = "foo"
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a factory method then do not emit`() {
        val code =
            """
            interface Foo
            class FooImpl : Foo
            fun Foo(): Foo = FooImpl()
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @DisplayName("Given a method with name between backticks")
    @Nested
    inner class BackTickedFunction {
        @Test
        fun `Given a function not annotated with a Test annotation then do emit`() {
            val code =
                """
                fun `Some name`() {}
                """.trimIndent()
            functionNamingRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 5, "Function name should start with a lowercase letter (except factory methods) and use camel case")
        }

        @Test
        fun `Given a function annotated with a Test annotation then do not emit`() {
            val code =
                """
                @Test
                fun `Some descriptive test name`() {}
                """.trimIndent()
            functionNamingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @ParameterizedTest(name = ": {0}")
    @ValueSource(
        strings = [
            "Foo",
            "Foo_Bar",
        ],
    )
    fun `Given an invalid function name then do emit`(functionName: String) {
        val code =
            """
            fun $functionName() = "foo"
            """.trimIndent()
        functionNamingRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 5, "Function name should start with a lowercase letter (except factory methods) and use camel case")
    }
}
