package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.supervisorTaskScope
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class SupervisorTaskScopeTest {
    @Test
    fun `supervisorScope should allow sibling tasks to continue even if one fails`() {
        val siblingTaskFinished = AtomicInteger(0)

        supervisorTaskScope {
            // Task 1: Fails after 100ms
            val failTask = async {
                sleep(100)
                throw RuntimeException("Individual Error")
            }

            // Task 2: Should complete successfully despite Task 1's failure
            val successTask = async {
                sleep(300)
                siblingTaskFinished.incrementAndGet()
            }

            // Only the failed task throws an exception when awaited
            assertThrows<RuntimeException> { failTask.await() }

            // The sibling task completes its work normally
            successTask.await()
        }

        assertEquals(1, siblingTaskFinished.get())
    }
}