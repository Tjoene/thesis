# Thesis: Testing Concurrent Actor Behavior

## Table of contents

- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Versions](#versions)

## Quick start

- Install [Simple Build Tool](http://www.scala-sbt.org/). At the time of writing, version 0.13.7 was used
- Clone the repo: `git clone https://github.com/Tjoene/thesis.git`.
- Copy the depencenies under `/Dependencies` to `C:\Users\<USER>\.ivy2\local\` for Windows, or `<HOME>/.ivy2/local/` for Linux
- Download the depencenies into the project: `sbt update`.
- Compile the source: `sbt compile`
- Run the included tests `sbt test`

## What's included

In this repo you'll see serveral folders:

```
thesis/
├── Case_Bita/
├── Case_Setak/
├── Case_TestKit/
│
├── Dependencies/
└── Tools/
```

In the `Case_*` folders you'll find (serveral) testcase(s) written to see the tested tool is able to find the race conditions we are looking for.
These testcase(s) can vary from very simple to more complex programs.

The `Dependencies` folder contains the pre-compiled jars of the tools that are needed to be included in the cases.
These jars should be placed in the local Ivy-repository on your computer.

The `Tools` folder contains a copy of the source code for the tools that were used during testing.  

## Versions

Altough the goal was to use the latest version (Scala 2.11.5 and Akka 2.3.9), but due to out-dated programs, this was adjusted to the following versions:

- [Scala](http://scala-lang.org/) version 2.9.2
- [Akka Actor](http://akka.io/) version 2.0.3

The program `Setak` is an exception, it uses Akka Actor 1.x
