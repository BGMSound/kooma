# 🐻 Kooma
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
![Latest Release](https://img.shields.io/github/v/release/BGMSound/kooma)
[![Apache 2.0 license](https://img.shields.io/badge/License-APACHE%202.0-green.svg?logo=APACHE&style=flat)](https://opensource.org/licenses/Apache-2.0)
<br>
Kooma is a Kotlin library for structured concurrency with virtual threads, inspired by coroutines.
It wraps Java's StructuredTaskScope with a coroutine-style API, making concurrent code simpler and more expressive.

## Installation and Getting Started
### Installation
Add the following dependency to your `build.gradle.kts` file:
<br><br>
```kotlin
dependencies {
    implementation("io.github.bgmsound:kooma:${version}")
}
```

### Getting Started

#### Quick Example
```kotlin
fun main() {
    taskScope {
        val a = async { sleep(300); "Hello" }
        val b = async { sleep(300); "World" }

        println("${a.await()} ${b.await()}") // "Hello World"
    }
}
```

All tasks inside a scope run on virtual threads concurrently.
The scope blocks until every subtask completes — or cancels all remaining tasks if any one of them throws.

---

#### Scopes

**`taskScope`**

Blocks until all subtasks complete. Any exception is propagated to the entire scope and cancels remaining tasks.
```kotlin
fun main() {
    val elapsed = measureTimeMillis {
        taskScope {
            val a = async { sleep(500); 1 }
            val b = async { sleep(500); 2 }
            println(a.await() + b.await()) // 3
        }
    }
    println("Elapsed: ${elapsed}ms") // ~500ms, not ~1000ms
}
```

**`supervisorTaskScope`**

Each subtask runs independently. Failures are isolated and surfaced only when calling `await()` on the failed task.
```kotlin
fun main() {
    supervisorTaskScope {
        val risky = async {
            sleep(100)
            throw RuntimeException("oops")
        }
        val safe = async {
            sleep(300)
            42
        }

        runCatching { risky.await() }.onFailure { println("caught: ${it.message}") }
        println(safe.await()) // 42
    }
}
```

**`asyncTaskScope` / `asyncSupervisorTaskScope`**

Runs the entire scope on a new virtual thread and returns a `Deferred<T>` immediately, without blocking the caller.
Useful when you want to isolate a scope from its parent or kick off background work.
```kotlin
fun main() {
    val deferred = asyncTaskScope {
        val a = async { sleep(500); "A" }
        val b = async { sleep(500); "B" }
        a.await() + b.await()
    }

    println("doing other work...")
    println(deferred.await()) // "AB"
}
```

---

#### Deferred and CompletableFuture

`async` returns a `Deferred<T>`, which can be awaited individually or converted to a `CompletableFuture<T>` via `asCompletableFuture()`.
This makes it easy to bridge into existing `CompletableFuture`-based code.
```kotlin
fun main() {
    taskScope {
        val a = async { sleep(300); "Hello" }
        val b = async { sleep(300); "World" }

        val future = b.asCompletableFuture()
            .thenApply { it.uppercase() }

        println(a.await())    // "Hello"
        println(future.get()) // "WORLD"
    }
}
```

If the `Deferred` is already complete when `asCompletableFuture()` is called, it returns an already-completed `CompletableFuture`.
Otherwise, the `CompletableFuture` completes at the same time the `Deferred` does.

---
[[click to see more sample code]](https://github.com/BGMSound/kooma/tree/main/kooma-test)

## License
kooma is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).