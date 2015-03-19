Programs for the Benchmark
======

## Table of contents

- [Quick start](#quick-start)
- [Programs](#programs)
  - [SignalCollect](#signalcollect)
  - [Fyrie](#fyrie)
  - [CQRS](#cqrs)
  - [Geotrellis](#geotrellis)
  - [Gatling](#gatling)
  - [Marshal](#marshal)

Quick start
------

In this folder, you will find the different testcases that were used with the tool Bita.
We also have tested serveral real life applications, but these are located under `thesis/Tools/`.

- Install [Simple Build Tool](http://www.scala-sbt.org/). At the time of writing, version 0.13.7 was used
- Clone the repo: `git clone https://github.com/Tjoene/thesis.git`.
- Copy the pre-compiled depencenies under `/Dependencies` to `C:\Users\<USER>\.ivy2\local\` for Windows, or `<HOME>/.ivy2/local/` for Linux
- Download the project depencenies: `sbt update`.
- Compile the source: `sbt compile`
- Compile the tests: `sbt test:compile`
- Move into the the desired folder of the program using `cd`
- Run the desired tests `sbt "testOnly"`, followed by the name of the test as followed: `bita.test_name` (e.g. `bita.DiamondSpec`).

**WARNING:** Avoid using `sbt test`. Running all the tests at once can result in false failures. 

Programs
------

## SignalCollect
**Source version:** clone from [commit 29fdc03](https://github.com/uzh/signal-collect/tree/29fdc039ebd84c509945efb6bc2342bbf8f9bc9d)

According to the paper ´Bita: Coverage-guidedn Automatic Testing of Actor Programs´, Bita is able to detect [a known bug in SignalCollect](https://github.com/uzh/signal-collect/issues/58), where the developers aren't able to reproduce it.
In the paper, Bita claims to detect the bug in every experiment, withing an average time of 176 seconds.

How ever, attempts to reproduce this result wasn't succesfull. The test in question always succeeds with the random sheduler, and using Bita's criterions yielded 0 generated shedules, meaning there weren't any actor messages detected in this particular test. 


## Fyrie
**Source version:** clone from [commit 070e5bd](https://github.com/derekjw/fyrie-redis/tree/070e5bd30f06f8da7a1e9c1e44c6b2b73537c03f)

Fyrie is a scala and akka actor based program that acts as a client for the Redis key-value cache and store server. 
In order to run these tests, you need to have a Redis server running on the localhost. You can download the redis source from [here](http://redis.io/).
There is also a port [available for windows](https://github.com/MSOpenTech/redis), this can be build using VisualStudio 2013.


## CQRS
**Source version:** clone from [commit e8f0d7b](https://github.com/debasishg/cqrs-akka/tree/e8f0d7b58e8bb7a7d000aefd3c1df2e8d3c49555)


## Geotrellis
**Source version:** clone from [tag 0.7.0](https://github.com/geotrellis/geotrellis/tree/v0.7.0)
**Status:** Uncompilable. A depencenies named ls-sbt was used in this version for adding other depencenies, but the repo behind it has been removed from the www.

A geographic data processing engine written in Scala.


## Gatling
**Source version:** clone from [tag 1.4.0](https://github.com/gatling/gatling/tree/1.4.0)
**Status:** Uncompilable. The project uses maven and not SBT. In it's current form we can not use it as we don't have the environment for it.

Gatling is a stress test tool for HTTP servers.


## Marshal
**Source version:** private repo, code not available for public


