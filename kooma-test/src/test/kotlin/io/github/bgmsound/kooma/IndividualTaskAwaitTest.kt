package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IndividualTaskAwaitTest {
    @Test
    fun `individual tasks should be awaitable independently before the entire scope joins`() {
        val startTime = System.currentTimeMillis()
        val taskBFinishTime = AtomicLong(0)

        taskScope {
            // Task A: A very slow task (1000ms)
            val taskA = async {
                sleep(1000)
                "Result A"
            }

            // Task B: A fast task (200ms)
            val taskB = async {
                sleep(200)
                "Result B"
            }

            // [Crucial Point] 
            // In standard Java StructuredTaskScope, we MUST call scope.join() 
            // before getting any results. But in Kooma, we can await Task B immediately.
            val resultB = taskB.await()
            taskBFinishTime.set(System.currentTimeMillis())

            // Verification 1: Task B's result is correct
            assertEquals("Result B", resultB)

            // Verification 2: Task B was awaited independently of Task A.
            // If we had to wait for the whole scope (Join), it would take at least 1000ms.
            val elapsedForB = taskBFinishTime.get() - startTime
            assertTrue(
                elapsedForB in 200..300,
                "Task B should be returned as soon as it's finished, regardless of Task A. Elapsed: ${elapsedForB}ms"
            )

            // Verification 3: After Task B is done, Task A should still be running or just finishing
            val resultA = taskA.await()
            assertEquals("Result A", resultA)

            val totalElapsed = System.currentTimeMillis() - startTime
            assertTrue(totalElapsed >= 1000, "Total scope time should be determined by the longest task. Total: ${totalElapsed}ms")
        }
    }

    @Test
    fun `awaiting an already completed task should return immediately`() {
        taskScope {
            val task = async {
                "Instant Result"
            }

            // Wait for a bit to ensure the task is definitely done
            sleep(100)

            val elapsed = measureTimeMillis {
                val result = task.await()
                assertEquals("Instant Result", result)
            }

            // Should be near 0ms because the result is already in the Deferred's outcome
            assertTrue(elapsed < 50, "Awaiting a completed task should not block. Elapsed: ${elapsed}ms")
        }
    }
}