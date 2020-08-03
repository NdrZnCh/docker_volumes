package docker.communicator

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import docker.data.DockerVolume
import java.util.stream.Collectors
import kotlin.properties.Delegates
import utils.Failure
import utils.Result
import utils.Success

private fun jsonToDockerVolume(json: String): DockerVolume? {
    return try {
        Gson().fromJson(json, DockerVolume::class.java)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null
    }
}

private fun String.removeQuotes() = this.removeSurrounding("\"")

fun volumesList(): List<DockerVolume> {
    return when (val result = dockerWithFormatter("volume", "ls", format = "json .Name") {
        volumeInspect(it.removeQuotes())
    }) {
        is Success -> result.value.filterNotNull()
        is Failure -> emptyList()
    }
}

fun volumeInspect(name: String): DockerVolume? {
    return when (val result = dockerWithFormatter("volume", "inspect", name) { jsonToDockerVolume(it) }) {
        is Success -> result.value.firstOrNull()
        is Failure -> null
    }
}

fun volumePrune(): Result<Unit> = docker("volume", "prune", "--force") { Unit }

class VolumeCreateArguments() {
    constructor(init: VolumeCreateArguments.() -> Unit) : this() {
        init()
    }

    var name: String by Delegates.notNull()
    var driver: String by Delegates.notNull()
    var options: Map<String, String> by Delegates.notNull()
    var labels: Map<String, String> by Delegates.notNull()
}

fun createVolume(arg: VolumeCreateArguments): Result<DockerVolume> {
    val labels = arg.labels.map { "--label ${it.key}=${it.value}" }
    val options = arg.options.map { "--opt ${it.key}=${it.value}" }

    val command = StringBuilder("volume create --name ${arg.name} --driver ${arg.driver}").apply {
        if (labels.isNotEmpty()) this.append(" " + labels.joinToString(separator = " "))
        if (options.isNotEmpty()) this.append(" " + options.joinToString(separator = " "))
    }.split(" ").toTypedArray()

    return when (val result = docker(*command) { it.collect(Collectors.joining()) }) {
        is Success -> {
            val newVolume = volumeInspect(result.value)

            if (newVolume != null) {
                Success(newVolume)
            } else Failure("Error while getting information about docker volume")
        }
        is Failure -> Failure(result.reason)
    }
}

fun removeVolume(element: String): Result<Unit> = docker("volume", "rm", element, "--force") { Unit }
