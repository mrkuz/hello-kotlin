package net.bnb1.gradle

import java.util.concurrent.TimeUnit

class Git {

    companion object {

        /**
         * Returns hash of the last commit.
         */
        fun commitHash(): String {
            return "git rev-parse --short HEAD".execute()
        }

        private fun String.execute(timeout: Int = 5): String {
            val process = ProcessBuilder()
                .command(*split(" ").toTypedArray())
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
            process.waitFor(timeout.toLong(), TimeUnit.SECONDS)
            return process.inputStream.bufferedReader().readText()
        }
    }
}