package utils

sealed class Result<T>

data class Success<T>(val value: T) : Result<T>()
data class Failure<T>(val reason: String) : Result<T>()

infix fun <T, U> Result<T>.then(f: (T) -> Result<U>) = when (this) {
    is Success -> f(this.value)
    is Failure -> Failure(this.reason)
}

infix fun <T, U> Result<T>.otherwise(f: (String) -> U) = if (this is Failure) f(this.reason) else null
