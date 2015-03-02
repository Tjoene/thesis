# Tool Bita

## Table of contents

- [Quick start](#quick-start)
- [Testcase Bank](#testcase-bank)
- [Testcase Bank2](#testcase-bank2)
- [Testcase Bank3](#testcase-bank3)
- [Testcase Bank4](#testcase-bank4)

## Quick start

In this folder you will find the different testcases that were used to test the tool Bita.

- Install [Simple Build Tool](http://www.scala-sbt.org/). At the time of writing, version 0.13.7 was used
- Clone the repo: `git clone https://github.com/Tjoene/thesis.git`.
- Copy the depencenies under `/Dependencies` to `C:\Users\<USER>\.ivy2\local\` for Windows, or `<HOME>/.ivy2/local/` for Linux
- Download the depencenies into the project: `sbt update`.
- Compile the source: `sbt compile`
- Run the desired tests `sbt "testOnly ..."`

**WARNING:** sometimes running all the testcases at once can result in false failures. 
It is recommended to run them one by one using the `testOnly` command, followed by the desired test.

## Testcase Bank

This testcase is a simple 3-actor system that mimics a bank system.
You will have one actor for the that respresents the bank, and actors for the accounts.
The first account (Freddy) will be initialized with an amount of $500 as start balance, the other account (Johnny) will have 0 as start balance.

The bank will issue a transfer of 500 from Freddy to Johnny. This will trigger a deposit of $500 to Johnny from Freddy.
After the transfer, the bank will ask the balance of Johnny, which should be $500.

The race condition in this testcase should be on Johnny where the message of Deposit and Balance can be switched.
This will result in the returned balance of Johnny to be $0, instead of the desired $500.

The testcase should be a baseline to see if Bita is capable of detecting the different execution paths in this actor model.


## Testcase Bank2

This is an extention of [the previous testcase](#testcase-bank) with an addition that money will be moved from one account (Johnny) to an other (Stevie).
The movent is triggered by a deposit of $5 from Freddy to Johnny. Once Johnny receives the deposit, he will send a Continue message to himself. This will trigger 
the transfer of $1 at the time to the next account (Stevie). Johnny will keep sending the message Continue to himself as long as the balance is positive.

In order to introduce a race condition in the system, we add a withdrawal from Johnny to the bank for an ammount of $5. 
To check if the race condition has occured, we check the balance of Stevie if this is greater then 0. If this is so, we know that the deposit arrived before the withdraw 
and that Johnny had the time to send a Continue to himself. 

This testcase has the advantage what the behaviour is of Bita when the execution if dependable on the time between two messages.
We can clearly see that Bita is not able to detect this, so have to find a way to intoduce the delay between messages manually. 


## Testcase Bank3

This is an other extention of the previous testcase, where we add an other account after Stevie, named Charlie.
Once Johnny receives an deposit from Freddy, he starts to send an ammount of 1 to Stevie, and Stevie will send to Charlie.

With this testcase we aim to increase the number of execution paths and see howmany Bita is able to find.
We could clearly see that Bita isn't able to detect them all, and needs help of a manual delay to find more execution paths.
Also, the testcases are not the same every time we run, this is due to the fact that Bita needs a shedule to start from. This is always 
a random shedule that is generated before the real test. When this change, the other shedules differ too.  


## Testcase Bank4

This testcase is the same as [testcase bank3](#testcase-bank3), but using the CallingThreadDispatcher to see if we are able to fix the random outcome
of the previous testcase.
We also adapted the TestHelper of Bita in order to solve a bug that could case the test to hang when a certain shedules occured that caused a timeout in the 
test.