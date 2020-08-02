package docker.communicator

import java.util.stream.Stream
import kotlin.streams.toList
import utils.runBackgroundProcess

private const val DOCKER_PROCESS_TIMEOUT: Long = 10

fun dockerProcess(vararg args: String): Result<Stream<String>> {
    val pb = ProcessBuilder()
            .command("docker", *args)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

    return when (val result = runBackgroundProcess(pb, DOCKER_PROCESS_TIMEOUT)) {
        is Success -> {
            val errorMessage = result.value.errorStream.bufferedReader().readText()

            return if (errorMessage.isEmpty() && result.value.exitValue() == 0) {
                Success(result.value.inputStream.bufferedReader().lines())
            } else Failure(errorMessage)
        }
        is Failure -> Failure(result.reason)
    }
}

fun <T> dockerWithFormatter(vararg args: String, format: String = "json .", parser: (String) -> T): Result<List<T>> {
    return when (val processResult = dockerProcess(*args, "--format={{$format}}")) {
        is Success -> Success(processResult.value.map { parser(it) }.toList())
        is Failure -> Failure(processResult.reason)
    }
}

inline fun <R> docker(vararg args: String, onSuccess: (Stream<String>) -> R): Result<R> {
    return when (val processResult = dockerProcess(*args)) {
        is Success -> Success(onSuccess(processResult.value))
        is Failure -> Failure(processResult.reason)
    }
}
