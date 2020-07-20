package docker.communicator

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.streams.toList

private const val DOCKER_PROCESS_TIMEOUT: Long = 10

fun dockerProcess(vararg args: String): Result<Stream<String>> {
    val pb = ProcessBuilder()
            .command("docker", *args)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

    val process = try {
        pb.start()
    } catch (e: IOException) {
        return Failure(e)
    }

    process.waitFor(DOCKER_PROCESS_TIMEOUT, TimeUnit.SECONDS)

    val errorMessage = process.errorStream.bufferedReader().readText()

    return if (errorMessage.isEmpty() && process.exitValue() == 0) {
        Success(process.inputStream.bufferedReader().lines())
    } else Failure(RuntimeException(errorMessage))
}

fun <T> docker(vararg args: String, format: String = "json .", parser: (String) -> T): Result<List<T>> {
    return when (val processResult = dockerProcess(*args, "--format={{$format}}")) {
        is Success -> Success(processResult.value.map { parser.invoke(it) }.toList())
        is Failure -> Failure(processResult.reason)
    }
}

fun docker(vararg args: String): Result<Unit> {
    return when (val processResult = dockerProcess(*args)) {
        is Success -> Success(Unit)
        is Failure -> Failure(processResult.reason)
    }
}
