package docker.communicator

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import docker.data.DockerVolume
import kotlin.properties.Delegates

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
    return when (val result = docker("volume", "ls", format = "json .Name") { volumeInspect(it.removeQuotes()) }) {
        is Success -> result.value.filterNotNull()
        is Failure -> emptyList()
    }
}

fun volumeInspect(name: String): DockerVolume? {
    return when (val result = docker("volume", "inspect", name) { jsonToDockerVolume(it) }) {
        is Success -> result.value.firstOrNull()
        is Failure -> null
    }
}

fun volumePrune(): Result<Unit> = docker("volume", "prune", "--force")

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
        if (labels.isNotEmpty()) this.append(" " + labels.joinToString())
        if (options.isNotEmpty()) this.append(" " + options.joinToString())
    }

    return when (val result = docker(*command.split(" ").toTypedArray())) {
        is Success -> {
            val newVolume = volumeInspect(arg.name)

            if (newVolume != null) {
                Success(newVolume)
            } else Failure("Error while getting information about docker volume")
        }
        is Failure -> Failure(result.reason)
    }
}

fun removeVolume(element: String): Result<Unit> = docker("volume", "rm", element, "--force")
