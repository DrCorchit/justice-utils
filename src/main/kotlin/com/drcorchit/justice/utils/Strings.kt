package com.drcorchit.justice.utils

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

private val log = Logger.getLogger(Strings::class.java)

class Strings {
    companion object {
        //singleton instance is used only for logging
        const val KILOBYTE = 1024
        const val MEGABYTE = KILOBYTE * 1024
        const val GIGABYTE = MEGABYTE * 1024
        val SUFFIXES = ImmutableList.of("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")

        //Static member must be private because SimpleDateFormat is not thread safe
        val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss:SSSS")

        val RULER =
            "1       10        20        30        40        50        60        70        80        90       100"

        //Turns a string into a key that can retrieve game elements from game mechanic parents
        //Several classes depend on this, not just game elements and mechanics
        @JvmStatic
        fun String.normalize(): String {
            //Convert spaces/dashes/hyphens to underscore _, then remove all non-[a-zA-Z0-9_] characters
            //Leading, trailing, and repeated underscores are also removed
            return this.replace("[_ —–-]+".toRegex(), " ").trim()
                .replace("\\W+".toRegex(), "")
                .replace(" ", "_")
                .lowercase(Locale.getDefault())
        }

        @JvmStatic
        @Deprecated("Retained for use by CivPlanet server", ReplaceWith("Strings.normalize()"))
        fun String.normalizeType(): String {
            //Same as above normalization but keeps colons.
            return this.replace("[_ —–-]+".toRegex(), " ").trim()
                .replace("[^a-zA-Z0-9_:]".toRegex(), "")
                .replace(" ", "_")
                .lowercase(Locale.getDefault())
        }

        @JvmStatic
        fun capitalizeWord(input: String, always: Boolean): String {
            return when (input) {
                "a", "and", "the" -> {
                    //do not capitalize unless forced
                    if (!always) input else input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1)
                        .lowercase(Locale.getDefault())
                }
                else -> input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1)
                    .lowercase(Locale.getDefault())
            }
        }

        @JvmStatic
        fun capitalizeWords(text: String): String {
            val parts = text.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val words = ArrayList<String>()
            for (part in parts) {
                words.add(capitalizeWord(part, false))
            }
            return java.lang.String.join(" ", words)
        }

        @JvmStatic
        fun toString(n: Number, precision: Int): String {
            val triples = ArrayList<String?>()
            var whole = n.toLong()
            val fraction = ((n.toDouble() - whole) * 10.0.pow(precision.toDouble())).toLong()
            do {
                val remain = whole % 1000
                whole /= 1000
                val triple = if (whole > 0) String.format("%03d", remain) else remain.toString()
                triples.add(triple)
            } while (whole > 0)
            triples.reverse()
            var output = java.lang.String.join(",", triples)
            if (fraction != 0L) output += ".$fraction"
            return output
        }

        @JvmStatic
        fun prettifySnakeCase(field: String): String {
            val parts = field.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val output = StringBuilder()
            for (part in parts) {
                if (output.isNotEmpty()) {
                    output.append(" ")
                }
                output.append(capitalizeWord(part, true))
            }
            return output.toString()
        }

        @JvmStatic
        fun snakeToCamelCase(field: String, capitalizeFirst: Boolean): String {
            val parts = field.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val output = StringBuilder()
            for (part in parts) {
                if (output.isEmpty()) {
                    if (capitalizeFirst) {
                        output.append(capitalizeWord(part, true))
                    } else {
                        output.append(part)
                    }
                } else {
                    output.append(capitalizeWord(part, true))
                }
            }
            return output.toString()
        }

        //converts a string to something appropriate for use as a web url
        @JvmStatic
        fun makeStringHtmlLink(input: String): String {
            //Change all spaces to underscores and remove most non-word characters
            //Change back slashes to forward slashes
            //Keep these characters: a-z A-Z 0-9 : / _ . -
            return input.replace(" ".toRegex(), "_").replace("\\\\".toRegex(), "\\/")
                .replace("[^a-zA-Z0-9:/_.-]".toRegex(), "")
        }

        @JvmStatic
        fun containsProfanity(input: String, profanities: ImmutableSet<String>): Boolean {
            //remove all nonletter/space characters
            val processedInput = input.normalize()
                //condense all single letters together -- catches stuff like f u c k or f.u.c.k.
                .replace("(?<=\\b\\w)_(?=\\w\\b)".toRegex(), "")
            for (word in processedInput.split("_")) if (profanities.contains(word)) return true
            return false
        }

        @JvmStatic
        fun String.wordWrap(maxLen: Int): String {
            if (length < maxLen) return this
            val output = StringBuilder()
            val lines = split("\n")
            for (line in lines) {
                var remain = line
                while (remain.isNotEmpty()) {
                    remain = if (remain.length < maxLen) {
                        output.append(remain)
                        ""
                    } else {
                        val pos = remain.lastIndexOf(' ', maxLen)
                        if (pos == -1) {
                            output.append(remain, 0, maxLen)
                            remain.substring(maxLen)
                        } else {
                            output.append(remain, 0, pos)
                            remain.substring(pos + 1)
                        }
                    }
                    output.append("\n")
                }
            }
            return output.toString()
        }

        @JvmStatic
        fun String.whitelist(): String {
            //blacklist the 5 html reserved characters
            //and allow minimal characters for writing
            return blacklistHtml().replace(Regex("[^a-zA-Z0-9 &;:,.!?%]"), "")
        }

        /**
         * Blacklists html reserved characters.
         * The string should be safe to display
         * via browser after this.
         *
         * "	&#34;	&quot;	quotation mark
         * '	&#39;	&apos;	apostrophe
         * &	&#38;	&amp;	ampersand
         * <	&#60;	&lt;	less-than
         * >	&#62;	&gt;	greater-than
         */
        @JvmStatic
        fun String.blacklistHtml(): String {
            return replace("&".toRegex(), "&amp;")
                .replace("\"".toRegex(), "&quot;")
                .replace("'".toRegex(), "&apos;")
                .replace("<".toRegex(), "&lt;")
                .replace(">".toRegex(), "&gt;")
            //.replace("\\n".toRegex(), "<br/>")
        }

        @JvmStatic
        fun String.wordcount(): Int {
            return this.split("\\s+".toRegex()).size
        }

        @JvmStatic
        fun Throwable.stackTraceToString(): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            this.printStackTrace(pw)
            return sw.toString()
        }

        @JvmStatic
        fun sizeInBytes(bytes: Int): String {
            return sizeInBytes(bytes.toDouble())
        }

        @JvmStatic
        fun sizeInBytes(bytes: Double): String {
            return if (bytes > GIGABYTE) {
                String.format("%.02f Gb", bytes / GIGABYTE)
            } else if (bytes > MEGABYTE) {
                String.format("%.02f Mb", bytes / MEGABYTE)
            } else if (bytes > KILOBYTE) {
                String.format("%.02f Kb", bytes / KILOBYTE)
            } else {
                String.format("%d bytes", bytes.toInt())
            }
        }

        @JvmStatic
        fun substring(input: String?, size: Int): String {
            return if (input == null) "null" else if (input.length <= size) input else input.substring(0, size)
        }

        @JvmStatic
        fun formatDate(date: Long): String {
            synchronized(DATE_FORMAT) { return DATE_FORMAT.format(Date(date)) }
        }

        @JvmStatic
        fun formatTime(diff: Long): String {
            require(diff >= 0) { "Time difference cannot be negative!" }
            var remain = diff
            //val millis = remain % 1000
            remain /= 1000
            val seconds = remain % 60
            remain /= 60
            val minutes = remain % 60
            remain /= 60
            val hours = remain % 24
            remain /= 24
            val days = remain
            return if (days == 0L) String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            ) else String.format("%d days, %2d:%02d:%02d", days, hours, minutes, seconds)
        }
    }
}