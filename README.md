# es-otc-derivatve

This is a sample of using event sourcing to model the individual transactions of an otc-derivative contract.
The purpose here is not to model an otc-derivative contract accurately, but to get a feel of doing it using event sourcing.
OTC Options are modelled.

The [Akka (Typed) Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html) API is used.

The "inmem" in-memory plugin is used for testing purposes.

## running

1) Use sbt to start-up

```
sbt
```

2) Run the tests

```
test
```
