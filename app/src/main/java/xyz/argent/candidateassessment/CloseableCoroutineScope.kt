package xyz.argent.candidateassessment

import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

class CloseableCoroutineScope(
    override val coroutineContext: CoroutineContext,
) : Closeable, CoroutineScope {
    override fun close() = coroutineContext.cancel()
}
