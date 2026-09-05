# Post-2.1.0 Java 21 Linux baseline

Recorded on 2026-09-05 with `package/run-benchmarks.sh full` after the first
post-release refactoring stage.

- JDK: OpenJDK 21.0.12
- OS: Linux 7.0.0-30-generic, x86_64
- CPU: Intel Core i7-6700K, 4 cores / 8 threads
- JMH: 1.37, 3 warmups, 5 measurements, 2 forks

| Benchmark | Mode | Score | 99.9% error |
| --- | --- | ---: | ---: |
| `readConvertedRow` | average | 117.202 ns/op | 4.414 ns/op |
| `rebuildStructuredQuery` | average | 866.453 ns/op | 31.344 ns/op |
| `renderLegacyUpdate` | average | 2090.456 ns/op | 136.637 ns/op |
| `renderPreparedUpdate` | average | 1496.415 ns/op | 50.305 ns/op |
| `borrowSelectAndReturn` | sample, 8 threads | 5.224 us/op | 0.106 us/op |
| `borrowUnderSaturation` | throughput, 8 threads | 37.268 ops/s | 1.113 ops/s |

Regular pool percentiles: p50 2.796 us/op, p90 3.192 us/op, p95 3.448
us/op, p99 5.440 us/op. Long-tail scheduling pauses are not portable across
hosts.

The saturation scenario reserves one of two connections and lets eight threads
contend for the other with a 250 ms acquisition timeout. Across both forks it
recorded 335 successful borrows and 871 timeouts, a 72.22% timeout share. This
is a repeatable overload diagnostic, not a recommended production pool size.

The previous `2.1.0` file remains the immutable release baseline. Compare these
results only on equivalent hardware, JDK, operating system, and power profile.
