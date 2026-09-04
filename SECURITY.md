# Security Policy

## Supported Versions

Security fixes are provided for the latest released `2.x` version. Older
versions should be upgraded before a report is evaluated.

## Reporting A Vulnerability

Do not open a public issue for a suspected vulnerability. Use GitHub's private
security advisory form for this repository:

`https://github.com/agtymc/org.agty.sql/security/advisories/new`

Include the affected version, driver, minimal reproduction, impact, and any
known workaround. Do not include production credentials or personal data.

## Operational Guidance

- Use `Arguments.useStatementPrepare(true)` for every value originating outside
  trusted application code.
- Keep `throwException=true`, TLS certificate validation enabled, and
  `logQueryValues=false` in production.
- Supply credentials through a secret manager or environment variables. Local
  ini files containing secrets must be readable only by their owner.
- Treat `SqlExpression.trusted(...)`, raw JDBC methods, and legacy rendering as
  explicit trust boundaries, not sanitizers.
