# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [1.1.5](https://github.com/headout/ergo-kotlin/compare/v1.1.4...v1.1.5) (2020-09-24)


### Bug Fixes

* use fixed threadpool and runJob without context switch ([4d6f42e](https://github.com/headout/ergo-kotlin/commit/4d6f42e9f7e9e38e16358ccfa651bf83c334235d))
* use IO dispatcher for ping message ([48ae379](https://github.com/headout/ergo-kotlin/commit/48ae37981dc1e54221cb76113b76c9cd748373d8))

### [1.1.4](https://github.com/headout/ergo-kotlin/compare/v1.1.3...v1.1.4) (2020-09-24)


### Bug Fixes

* add to pending job only if processing ([59c8d5b](https://github.com/headout/ergo-kotlin/commit/59c8d5bc92f73e599b32970bfc37777caf98c60f))
* parse 404 results too ([32af477](https://github.com/headout/ergo-kotlin/commit/32af477b75cacb98400c8a10639e1f7c9d848da9))

### [1.1.2](https://github.com/headout/ergo-kotlin/compare/v1.1.1...v1.1.2) (2020-09-24)


### Bug Fixes

* complete stacktrace (with cause) in error metadata ([7b4c7d4](https://github.com/headout/ergo-kotlin/commit/7b4c7d48d15d7dea1ddef9b10cf90aa1e886b484))

### [1.1.1](https://github.com/headout/ergo-kotlin/compare/v1.1.0...v1.1.1) (2020-09-24)


### Bug Fixes

* add optional param for nullable types ([a2bf2be](https://github.com/headout/ergo-kotlin/commit/a2bf2be083cafaab24e8b0e30e5cce3f3ead691e))
* choose log level for received messages ([b22a44f](https://github.com/headout/ergo-kotlin/commit/b22a44f60e30e6e9fbc1bcecd5891357ea0fdd00))

## [1.1.0](https://github.com/headout/ergo-kotlin/compare/v1.0.2...v1.1.0) (2020-09-24)


### Features

* fetch visibility timeout if not provided ([d94545c](https://github.com/headout/ergo-kotlin/commit/d94545c6047a5dbffe6ea06864911e03e094c475))


### Bug Fixes

* check and throw error when creating spring bean ([a765c6a](https://github.com/headout/ergo-kotlin/commit/a765c6a557f0a95a0e437c2ee776d580c17537a2))

### [1.0.2](https://github.com/headout/ergo-kotlin/compare/v1.0.1...v1.0.2) (2020-09-24)


### Bug Fixes

* All SQS unit tests pass now ([dde44b0](https://github.com/headout/ergo-kotlin/commit/dde44b0aab747f8cb925516b4cfceb4c3c1ddd03))
* visibility timeout ping delay is wrongly in seconds ([910c7a9](https://github.com/headout/ergo-kotlin/commit/910c7a9eca68350b89590a44e9e9a043db51d3c3))

### [1.0.1](https://github.com/headout/ergo-kotlin/compare/v1.0.0...v1.0.1) (2020-09-24)


### Bug Fixes

* java tests failing due to static task functions ([f2f37dc](https://github.com/headout/ergo-kotlin/commit/f2f37dc6588abe9ac1faa6f1a54de963ee19918a))
* support Unit return type on Task function ([c15567e](https://github.com/headout/ergo-kotlin/commit/c15567ea58540ce545a5c60002f35917b75fb2e5))
