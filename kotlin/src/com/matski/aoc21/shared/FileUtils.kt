package com.matski.aoc21.shared

import mu.KLogger
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*


private class FileUtils {

    private val log = KotlinLogging.logger {}
    private val startDir = System.getProperty("user.dir")
    private fun dirOf(dir: String): String = dir.split("/").joinToString(separator = File.separator)
    val ignorableDirs = arrayOf(
        "$startDir/.idea",
        "$startDir/out",
        "$startDir/kotlin/gradle",
        "$startDir/kotlin/.gradle"
    ).map(this::dirOf)

    fun breadthFirstSearchForFile(fileName: String): File {
        val fileParts = fileName.split("/").iterator()
        var searchHint = fileParts.next()
        val queue = ArrayDeque<File>()
        queue.add(File(startDir))
        while (!queue.isEmpty()) {
            //get current directory from queue:
            val dirOrFile: File = queue.poll()
            if (processEntry(
                    dirOrFile,
                    searchHint,
                    fileName,
                    queue,
                    { if (fileParts.hasNext()) searchHint = fileParts.next() })
            ) return dirOrFile
        }
        throw IOException("File: '${fileName}' could not be found")
    }

    private fun processEntry(
        dirOrFile: File,
        searchHint: String,
        fileName: String,
        queue: ArrayDeque<File>,
        startUsingNextHint: () -> Unit
    ): Boolean {
        log.debug { "Processing file/directory: $dirOrFile" }
        if (dirOrFile.isDirectory && !ignorableDirs.contains(dirOrFile.absolutePath)) {
            val dirOrFileList = dirOrFile.listFiles()
            if (dirOrFileList != null) {
                if (dirOrFileList.any { file ->
                        file.name.equals(searchHint)
                    }) {
                    addToQueue(dirOrFile.listFiles(), queue)
                    startUsingNextHint()
                } else {
                    addToQueue(dirOrFileList, queue)
                }
            }
        } else {
            if (dirOrFile.name.equals(searchHint) && dirOrFile.absolutePath.contains(fileName)) return true
        }
        return false
    }

    private fun addToQueue(entry: Array<File>?, queue: ArrayDeque<File>?): Unit {
        entry?.forEach { e ->
            log.debug { "Adding entries of: $e" }
            queue?.add(e)
        }
    }
}

private val fu = FileUtils()

/**
 * Open and read all lines in an input file for advent of code, applying a mapper function to convert the input strings
 * into a list of objects
 */
fun <K> readFile(fileName: String, modifier: (String) -> (K)): List<K> {
    return fu.breadthFirstSearchForFile(fileName).readLines().mapNotNull(modifier)
}

fun readFile(fileName: String): List<String> {
    return readFile(fileName) { s -> s }
}

fun withInput(fileName: String, action: (String) -> Unit): Unit {
    readFile(fileName) { action }
}

val metricsLogger = KotlinLogging.logger("Metrics")

fun <T> withMetrics(logger: KLogger = metricsLogger, identifier: String = "", invokable: () -> T): T {
    val startTime = System.currentTimeMillis()
    val identifiable = if (identifier.length > 1) "[$identifier]" else ""
    logger.info { "$identifiable Start time: ${LocalDateTime.now()}"}
    val returnValue = invokable()
    logger.info { "$identifiable End time: ${LocalDateTime.now()}, total time:${(System.currentTimeMillis() - startTime)/1000}s"}
    return returnValue
}