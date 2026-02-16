package com.github.dotenvloader.parser

import org.junit.Assert.*
import org.junit.Test

class DotEnvParserTest {

    @Test
    fun `parse basic key-value pairs`() {
        val result = DotEnvParser.parse("KEY=value\nANOTHER=123")
        assertEquals("value", result["KEY"])
        assertEquals("123", result["ANOTHER"])
    }

    @Test
    fun `parse skips comments and empty lines`() {
        val content = """
            # This is a comment
            KEY=value

            # Another comment
            OTHER=test
        """.trimIndent()
        val result = DotEnvParser.parse(content)
        assertEquals(2, result.size)
        assertEquals("value", result["KEY"])
        assertEquals("test", result["OTHER"])
    }

    @Test
    fun `parse double quoted values`() {
        val result = DotEnvParser.parse("""KEY="hello world"""")
        assertEquals("hello world", result["KEY"])
    }

    @Test
    fun `parse single quoted values`() {
        val result = DotEnvParser.parse("KEY='hello world'")
        assertEquals("hello world", result["KEY"])
    }

    @Test
    fun `parse inline comments for unquoted values`() {
        val result = DotEnvParser.parse("KEY=value # this is a comment")
        assertEquals("value", result["KEY"])
    }

    @Test
    fun `parse inline comments not stripped from double quoted values`() {
        val result = DotEnvParser.parse("""KEY="value # not a comment"""")
        assertEquals("value # not a comment", result["KEY"])
    }

    @Test
    fun `parse inline comments not stripped from single quoted values`() {
        val result = DotEnvParser.parse("KEY='value # not a comment'")
        assertEquals("value # not a comment", result["KEY"])
    }

    @Test
    fun `parse empty value`() {
        val result = DotEnvParser.parse("KEY=")
        assertEquals("", result["KEY"])
    }

    @Test
    fun `parse export prefix`() {
        val result = DotEnvParser.parse("export KEY=value")
        assertEquals("value", result["KEY"])
    }

    @Test
    fun `parse escape sequences in double quotes`() {
        val result = DotEnvParser.parse("""KEY="line1\nline2"""")
        assertEquals("line1\nline2", result["KEY"])
    }

    @Test
    fun `parse tab escape in double quotes`() {
        val result = DotEnvParser.parse("""KEY="col1\tcol2"""")
        assertEquals("col1\tcol2", result["KEY"])
    }

    @Test
    fun `parse escaped backslash in double quotes`() {
        val result = DotEnvParser.parse("""KEY="path\\to\\file"""")
        assertEquals("path\\to\\file", result["KEY"])
    }

    @Test
    fun `parse escaped quote in double quotes`() {
        val result = DotEnvParser.parse("""KEY="say \"hello\""""")
        assertEquals("say \"hello\"", result["KEY"])
    }

    @Test
    fun `parse multiline double quoted value`() {
        val content = "KEY=\"line1\nline2\nline3\""
        val result = DotEnvParser.parse(content)
        assertEquals("line1\nline2\nline3", result["KEY"])
    }

    @Test
    fun `parse value with equals sign`() {
        val result = DotEnvParser.parse("KEY=abc=def")
        assertEquals("abc=def", result["KEY"])
    }

    @Test
    fun `later duplicate key wins`() {
        val content = "KEY=first\nKEY=second"
        val result = DotEnvParser.parse(content)
        assertEquals("second", result["KEY"])
    }

    @Test
    fun `parse empty content returns empty map`() {
        assertEquals(emptyMap<String, String>(), DotEnvParser.parse(""))
    }

    @Test
    fun `parse trims whitespace from keys and unquoted values`() {
        val result = DotEnvParser.parse("  KEY  =  value  ")
        assertEquals("value", result["KEY"])
    }

    @Test
    fun `single quoted values are not escape-processed`() {
        val result = DotEnvParser.parse("KEY='hello\\nworld'")
        assertEquals("hello\\nworld", result["KEY"])
    }

    @Test
    fun `lines without equals sign are skipped`() {
        val content = "INVALID_LINE\nKEY=value"
        val result = DotEnvParser.parse(content)
        assertEquals(1, result.size)
        assertEquals("value", result["KEY"])
    }

    @Test
    fun `parse multiline value followed by another key`() {
        val content = "MULTI=\"line1\nline2\"\nNEXT=after"
        val result = DotEnvParser.parse(content)
        assertEquals("line1\nline2", result["MULTI"])
        assertEquals("after", result["NEXT"])
    }

    @Test
    fun `parse double quoted empty value`() {
        val result = DotEnvParser.parse("""KEY="""""")
        assertEquals("", result["KEY"])
    }

    @Test
    fun `parse single quoted empty value`() {
        val result = DotEnvParser.parse("KEY=''")
        assertEquals("", result["KEY"])
    }
}
