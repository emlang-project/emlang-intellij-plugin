package com.emlang.intellij.runner

import com.emlang.intellij.settings.EmlangSettings
import java.io.File
import java.util.concurrent.TimeUnit

class EmlangRunner {

    data class RunResult(
        val success: Boolean,
        val output: String,
        val error: String
    )

    companion object {
        private const val TIMEOUT_SECONDS = 30L
    }

    fun runEmlang(filePath: String): RunResult {
        val settings = EmlangSettings.getInstance().state

        if (settings.binaryPath.isBlank()) {
            return RunResult(
                success = false,
                output = "",
                error = "Emlang binary path is not configured. Please configure it in Settings > Tools > Emlang."
            )
        }

        val binaryFile = File(settings.binaryPath)
        if (!binaryFile.exists()) {
            return RunResult(
                success = false,
                output = "",
                error = "Emlang binary not found at: ${settings.binaryPath}"
            )
        }

        if (!binaryFile.canExecute()) {
            return RunResult(
                success = false,
                output = "",
                error = "Emlang binary is not executable: ${settings.binaryPath}"
            )
        }

        return try {
            val command = buildCommand(settings.binaryPath, filePath)
            executeCommand(command, File(filePath).parentFile)
        } catch (e: Exception) {
            RunResult(
                success = false,
                output = "",
                error = "Failed to execute emlang: ${e.message}"
            )
        }
    }

    private fun buildCommand(binaryPath: String, filePath: String): List<String> {
        return listOf(binaryPath, "diagram", filePath)
    }

    private fun executeCommand(command: List<String>, workingDir: File?): RunResult {
        val processBuilder = ProcessBuilder(command).apply {
            if (workingDir != null && workingDir.isDirectory) {
                directory(workingDir)
            }
            redirectErrorStream(false)
        }

        val process = processBuilder.start()

        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }

        val completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (!completed) {
            process.destroyForcibly()
            return RunResult(
                success = false,
                output = "",
                error = "Emlang process timed out after $TIMEOUT_SECONDS seconds"
            )
        }

        val exitCode = process.exitValue()

        return if (exitCode == 0) {
            RunResult(
                success = true,
                output = stdout,
                error = ""
            )
        } else {
            RunResult(
                success = false,
                output = stdout,
                error = stderr.ifBlank { "Emlang exited with code $exitCode" }
            )
        }
    }
}
