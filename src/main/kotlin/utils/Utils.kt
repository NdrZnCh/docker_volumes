package utils

import com.intellij.openapi.progress.runBackgroundableTask
import docker.volumes.DockerVolumesBundle.messagePointer
import java.io.IOException
import java.util.concurrent.TimeUnit

inline fun <K, V> Map<K, V>.filterIf(doFilter: Boolean, predicate: (Map.Entry<K, V>) -> Boolean): Map<K, V> {
    return if (doFilter) this.filter(predicate) else this
}

inline fun <V> Array<V>?.firstOr(or: () -> V) = this?.firstOrNull() ?: or()

inline fun <V> Collection<V>.firstOr(or: () -> V): V = if (this.isEmpty()) or() else this.first()

fun <V> MutableList<V>.addIf(add: Boolean, element: V) {
    if (add) this.add(element)
}

fun <V> Set<V>.minusIfNotNull(element: V?) = if (element != null) this.minus(element) else this

fun runBackgroundProcess(processBuilder: ProcessBuilder, timeout: Long): Result<Process> {
    val process = try {
        processBuilder.start()
    } catch (e: IOException) {
        return Failure(e.message ?: "Error while running ${processBuilder.command().joinToString()} command")
    }

    runBackgroundableTask(messagePointer("docker.background.task.title"), null, false) {
        process.waitFor(timeout, TimeUnit.SECONDS)
    }

    process.waitFor(timeout, TimeUnit.SECONDS)

    return Success(process)
}
