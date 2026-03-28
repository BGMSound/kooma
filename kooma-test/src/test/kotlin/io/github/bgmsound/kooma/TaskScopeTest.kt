package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskScopeTest {
    @Test
    fun `async tasks should execute concurrently`() {
        // Measure execution time to verify non-blocking behavior
        val elapsed = measureTimeMillis {
            taskScope {
                val d1 = async { sleep(500); 1 }
                val d2 = async { sleep(500); 2 }
                val d3 = async { sleep(500); 3 }

                val sum = d1.await() + d2.await() + d3.await()
                assertEquals(6, sum)
            }
        }

        // Total time should be around 500ms, not 1500ms (500ms * 3)
        assertTrue(elapsed < 600, "Tasks are not executing concurrently. Elapsed: ${elapsed}ms")
    }

    @Test
    fun `taskScope should execute all nested tasks concurrently`() {
        val elapsed = measureTimeMillis {
            taskScope {
                val d1 = async {
                    launch {
                        sleep(200)
                    }
                    sleep(500)
                    1
                }
                val d2 = async {
                    launch {
                        sleep(300)
                    }
                    launch {
                        sleep(400)
                    }
                    sleep(500)
                    2
                }
                val d3 = async {
                    launch {
                        sleep(500)
                    }
                    sleep(500)
                    3
                }
                val sum = d1.await() + d2.await() + d3.await()
                assertEquals(6, sum)
            }
        }
        assertTrue(elapsed < 600, "Tasks are not executing concurrently. Elapsed: ${elapsed}ms")
    }

    @Test
    fun `a failure in taskScope should cancel all sibling tasks immediately`() {
        val siblingTaskFinished = AtomicInteger(0)

        assertThrows<RuntimeException> {
            taskScope {
                // Task 1: Fails after 100ms
                launch {
                    sleep(100)
                    throw RuntimeException("Intentional Failure")
                }

                // Task 2: Sibling task that should be cancelled by Task 1's failure
                launch {
                    try {
                        sleep(2000) // Long delay
                        siblingTaskFinished.incrementAndGet()
                    } catch (_: Exception) {
                        // Sibling task successfully caught the cancellation signal (InterruptedException)
                        println("Sibling task cancelled as expected.")
                    }
                }
            }
        }

        // Sibling task should never reach the increment code due to cancellation
        assertEquals(0, siblingTaskFinished.get())
    }
}