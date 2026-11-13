package com.example.pickmeup.data

import kotlinx.coroutines.CancellationException

/** runCatching 과 같지만 코루틴 취소(CancellationException)는 삼키지 않습니다. */
internal inline fun <T> runSuspendCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
