# org.agty.sql benchmarks

The benchmarks are kept in a separate Maven project so JMH annotation processing and runtime dependencies never enter the library artifact.

Run a short correctness smoke test:

```bash
package/run-benchmarks.sh smoke
```

Run the repeatable local baseline:

```bash
package/run-benchmarks.sh full
```

Both modes write machine-readable results under `benchmarks/target/`. Compare results only on equivalent hardware, JDK, operating system, power profile, and database storage. Shared CI runners are used as a smoke gate, not as an absolute latency SLA.

The versioned reference measurements are stored in `baselines/` together with their execution environment.

`CorePathBenchmark` measures legacy/prepared update rendering, structural query rebuilding, and common `RowData` conversions. `PooledDataSourceBenchmark` reports sampled latency for an eight-thread Hikari/H2 `borrow -> SELECT -> close` cycle.

`PooledDataSourceTimeoutBenchmark` intentionally reserves one of two pooled connections and lets eight threads contend for the other. Its `successfulBorrows` and `timeouts` secondary counters expose timeout rate separately from aggregate throughput. This is a saturation diagnostic, not a production capacity target.
