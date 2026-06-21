package com.jinatra.finatra.util

/** CSV helpers shared by export/import (PRD 6.12). */
object CsvUtil {
    /**
     * Parse a single CSV [line] into its fields (RFC 4180 style on one line).
     *
     * Handles double-quoted fields, commas inside quotes, and the `""` escape for a literal quote.
     * Surrounding quote characters are consumed (not included in the output); a trailing empty field
     * after a final comma is preserved. Assumes a single physical line — embedded newlines inside a
     * quoted field are not supported by this single-line parser.
     */
    fun splitLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                // Escaped quote inside a quoted field: "" -> literal "; skip the second quote.
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> { sb.append('"'); i++ }
                // A lone quote toggles in/out of quoted mode and is not emitted.
                c == '"' -> inQuotes = !inQuotes
                // Unquoted comma ends the current field; quoted commas fall through to append.
                c == ',' && !inQuotes -> { out.add(sb.toString()); sb.setLength(0) }
                else -> sb.append(c)
            }
            i++
        }
        out.add(sb.toString())
        return out
    }
}
