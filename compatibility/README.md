# Compatibility fixtures

`2.0.4/Legacy2xClient.java` is intentionally compiled against the `2.0.4`
artifact and then executed, without recompilation, against the current `2.x`
artifact. It exercises legacy CRUD calls and deprecated aliases that remain
part of the `2.x` compatibility contract.

Run the complete API and binary upgrade gate with:

```bash
package/check-api-compatibility.sh
```

The fixture must not call APIs introduced after `2.0.4`; otherwise it no
longer represents an existing consumer upgrading only its runtime JAR.
