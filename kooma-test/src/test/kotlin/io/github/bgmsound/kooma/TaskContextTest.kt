package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.TaskContextBuilder.Companion.taskContext
import io.github.bgmsound.kooma.asyncTaskScope
import io.github.bgmsound.kooma.supervisorScope
import kotlin.test.Test
import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.assertEquals

class TaskContextTest {

    companion object {
        val USER_ID: ScopedValue<String> = ScopedValue.newInstance()
        val REQUEST_ID: ScopedValue<String> = ScopedValue.newInstance()
        val TENANT_ID: ScopedValue<String> = ScopedValue.newInstance()
    }

    @Test
    fun `taskScope should propagate context to child tasks`() {
        taskScope(context = taskContext { bind(USER_ID, "user-123") }) {
            val result = async { USER_ID.get() }
            assertEquals("user-123", result.await())
        }
    }

    @Test
    fun `taskScope should propagate multiple context values to child tasks`() {
        val ctx = taskContext {
            bind(USER_ID, "user-123")
            bind(REQUEST_ID, "req-456")
        }

        taskScope(context = ctx) {
            val userId = async { USER_ID.get() }
            val requestId = async { REQUEST_ID.get() }

            assertEquals("user-123", userId.await())
            assertEquals("req-456", requestId.await())
        }
    }

    @Test
    fun `taskScope should propagate combined contexts using plus operator`() {
        val ctx = taskContext { bind(USER_ID, "user-123") } +
                taskContext { bind(REQUEST_ID, "req-456") }

        taskScope(context = ctx) {
            val userId = async { USER_ID.get() }
            val requestId = async { REQUEST_ID.get() }

            assertEquals("user-123", userId.await())
            assertEquals("req-456", requestId.await())
        }
    }

    @Test
    fun `taskScope should propagate context to nested child scopes`() {
        taskScope(context = taskContext { bind(USER_ID, "user-123") }) {
            val result = async {
                taskScope {
                    async { USER_ID.get() }.await()
                }
            }
            assertEquals("user-123", result.await())
        }
    }

    @Test
    fun `taskScope without context should not have ScopedValue bound`() {
        taskScope {
            val result = async { USER_ID.isBound }
            assertFalse(result.await())
        }
    }

    @Test
    fun `supervisorScope should propagate context even when some tasks fail`() {
        val ctx = taskContext {
            bind(USER_ID, "user-123")
        }

        supervisorScope(context = ctx) {
            val failing = async {
                sleep(100)
                throw RuntimeException("oops")
            }
            val succeeding = async { USER_ID.get() }

            runCatching { failing.await() }
            assertEquals("user-123", succeeding.await())
        }
    }

    @Test
    fun `asyncTaskScope should propagate context to child tasks`() {
        val ctx = taskContext {
            bind(USER_ID, "user-123")
        }

        val deferred = asyncTaskScope(context = ctx) {
            async { USER_ID.get() }.await()
        }

        assertEquals("user-123", deferred.await())
    }

    @Test
    fun `asyncTaskScope should propagate multiple context values`() {
        val ctx = taskContext {
            bind(USER_ID, "user-123")
            bind(REQUEST_ID, "req-456")
            bind(TENANT_ID, "tenant-789")
        }

        val deferred = asyncTaskScope(context = ctx) {
            val userId = async { USER_ID.get() }
            val requestId = async { REQUEST_ID.get() }
            val tenantId = async { TENANT_ID.get() }
            "${userId.await()}-${requestId.await()}-${tenantId.await()}"
        }

        assertEquals("user-123-req-456-tenant-789", deferred.await())
    }

    @Test
    fun `context should not leak outside of taskScope`() {
        taskScope(context = taskContext {
            bind(USER_ID, "user-123")
        }) {
            async { USER_ID.get() }.await()
        }

        assertFalse(USER_ID.isBound)
    }

    @Test
    fun `context should not leak outside of asyncTaskScope`() {
        val deferred = asyncTaskScope(context = taskContext { bind(USER_ID, "user-123") }) {
            sleep(100)
            USER_ID.get()
        }

        deferred.await()
        assertFalse(USER_ID.isBound)
    }
}