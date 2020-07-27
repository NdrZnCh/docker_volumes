package docker.communicator

sealed class Result<out Success>

data class Success<out Success>(val value: Success) : Result<Success>()
data class Failure(val reason: String) : Result<Nothing>()
