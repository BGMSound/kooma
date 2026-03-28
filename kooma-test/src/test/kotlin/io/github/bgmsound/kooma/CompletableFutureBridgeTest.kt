package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.supervisorScope
import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CompletionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompletableFutureBridgeTest {

    @Test
    fun `the completion status of Deferred should be accurately reflected in the CompletableFuture`() {
        taskScope {
            val deferred = async {
                sleep(200)
                "Completable Future"
            }
            // Convert to CompletableFuture before the Deferred completes
            val future = deferred.asCompletableFuture()
            assertFalse(future.isDone, "The task should not be completed yet.")

            // Test the chaining feature of the Future
            var chainedResult = ""
            future.thenAccept {
                chainedResult = it.uppercase()
            }

            // Deferred.await() and Future.join() must guarantee the same result
            assertEquals("Completable Future", deferred.await())
            assertEquals("Completable Future", future.join())

            // Verify that the callback chaining executed normally
            assertEquals("COMPLETABLE FUTURE", chainedResult)
        }
    }

    @Test
    fun `converting an already completed Deferred should return an immediately completed CompletableFuture`() {
        taskScope {
            val deferred = async { "Instant Result" }

            // Explicitly wait for the task to finish
            deferred.await()

            // Call asCompletableFuture() on an already completed state
            val future = deferred.asCompletableFuture()

            assertTrue(future.isDone, "The returned Future should already be in a completed state.")
            assertEquals("Instant Result", future.join())
        }
    }

    @Test
    fun `if an exception occurs in Deferred, the CompletableFuture should complete exceptionally`() {
        supervisorScope {
            val deferred = async {
                sleep(100)
                throw IllegalStateException("Future Error")
            }

            val future = deferred.asCompletableFuture()

            // Java's CompletableFuture wraps the exception in a CompletionException.
            val exception = assertThrows<CompletionException> {
                future.join()
            }

            // Verify that the original exception is properly set as the cause
            assertNotNull(exception.cause)
            assertTrue(exception.cause is IllegalStateException)
            assertEquals("Future Error", exception.cause?.message)
        }
    }
}