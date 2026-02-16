package com.github.dotenvloader.parser

import java.io.File

object DotEnvParser {

    fun parse(file: File): Map<String, String> {
        if (!file.exists() || !file.isFile) return emptyMap()
        return parse(file.readText())
    }

    fun parse(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            i++

            if (line.isEmpty() || line.startsWith("#")) continue

            val effectiveLine = if (line.startsWith("export ")) {
                line.removePrefix("export ").trimStart()
            } else {
                line
            }

            val equalsIndex = effectiveLine.indexOf('=')
            if (equalsIndex == -1) continue

            val key = effectiveLine.substring(0, equalsIndex).trim()
            if (key.isEmpty()) continue

            val rawValue = effectiveLine.substring(equalsIndex + 1)

            when {
                rawValue.trimStart().startsWith("\"") -> {
                    val afterQuote = rawValue.trimStart().substring(1)
                    val (value, newIndex) = parseDoubleQuoted(afterQuote, lines, i)
                    i = newIndex
                    result[key] = value
                }
                rawValue.trimStart().startsWith("'") -> {
                    val afterQuote = rawValue.trimStart().substring(1)
                    result[key] = parseSingleQuoted(afterQuote)
                }
                else -> {
                    result[key] = parseUnquoted(rawValue)
                }
            }
        }

        return result
    }

    private fun parseDoubleQuoted(
        afterQuote: String,
        lines: List<String>,
        currentIndex: Int,
    ): Pair<String, Int> {
        val sb = StringBuilder()
        var remaining = afterQuote
        var lineIndex = currentIndex

        while (true) {
            var j = 0
            while (j < remaining.length) {
                val ch = remaining[j]
                when {
                    ch == '"' -> {
                        return Pair(sb.toString(), lineIndex)
                    }
                    ch == '\\' && j + 1 < remaining.length -> {
                        when (remaining[j + 1]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            else -> {
                                sb.append('\\')
                                sb.append(remaining[j + 1])
                            }
                        }
                        j += 2
                    }
                    else -> {
                        sb.append(ch)
                        j++
                    }
                }
            }

            // Value continues on the next line
            if (lineIndex < lines.size) {
                sb.append('\n')
                remaining = lines[lineIndex]
                lineIndex++
            } else {
                // Unterminated quote — return what we have
                break
            }
        }

        return Pair(sb.toString(), lineIndex)
    }

    private fun parseSingleQuoted(afterQuote: String): String {
        val closingIndex = afterQuote.indexOf('\'')
        return if (closingIndex == -1) {
            afterQuote
        } else {
            afterQuote.substring(0, closingIndex)
        }
    }

    private fun parseUnquoted(rawValue: String): String {
        val trimmed = rawValue.trim()
        // Strip inline comment: look for " #" or "\t#" pattern
        val commentIndex = trimmed.indexOf(" #")
        val tabCommentIndex = trimmed.indexOf("\t#")
        val effectiveCommentIndex = when {
            commentIndex >= 0 && tabCommentIndex >= 0 -> minOf(commentIndex, tabCommentIndex)
            commentIndex >= 0 -> commentIndex
            tabCommentIndex >= 0 -> tabCommentIndex
            else -> -1
        }
        return if (effectiveCommentIndex >= 0) {
            trimmed.substring(0, effectiveCommentIndex).trimEnd()
        } else {
            trimmed
        }
    }
}
