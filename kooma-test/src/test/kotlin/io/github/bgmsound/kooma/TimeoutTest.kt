package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.asyncTaskScope
import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.concurrent.StructuredTaskScope
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeoutTest {

    @Test
    fun `taskScope should throw when timeout is exceeded`() {
        assertThrows<StructuredTaskScope.TimeoutException> {
            taskScope(timeout = Duration.ofMillis(200)) {
                async { sleep(1000); "too late" }.await()
            }
        }
    }

    @Test
    fun `taskScope should complete normally when finished before timeout`() {
        val result = taskScope(timeout = Duration.ofMillis(1000)) {
            async { sleep(100); "in time" }.await()
        }
        assertEquals("in time", result)
    }

    @Test
    fun `asyncTaskScope should throw when timeout is exceeded`() {
        val deferred = asyncTaskScope(timeout = Duration.ofMillis(200)) {
            sleep(1000)
            "too late"
        }
        assertThrows<StructuredTaskScope.TimeoutException> { deferred.await() }
    }
}