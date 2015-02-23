# Thesis: Testing Concurrent Actor Behavior

## Table of contents

- [Quick start](#quick-start)
- [Versions](#versions)

## Quick start

- Install [Simple Build Tool](http://www.scala-sbt.org/). At the time of writing, version 0.13.7 was used
- Clone the repo: `git clone https://github.com/Tjoene/thesis.git`.
- Copy the depencenies under `/DEPENDENCIES` to `C:\Users\<USER>\.ivy2\local\` for Windows, or `<HOME>/.ivy2/local/` for Linux
- Download the depencenies into the project: `sbt update`.
- Compile the source: `sbt compile`
- Run the included tests `sbt test`

## Versions

The sources are all tested for the following sources:

- [Scala](http://scala-lang.org/) version 2.9.2
- [Akka Actor](http://akka.io/) versiob 2.0.3

The program `Setak` has an exception, that uses Akka Actor 1.x