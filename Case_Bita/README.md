# Tool Bita

## Table of contents

- [Quick start](#quick-start)
- [Testcase Bank](#testcase-bank)
- [Testcase Bank2](#testcase-bank2)
- [Testcase Bank3](#testcase-bank3)
- [Testcase Bank4](#testcase-bank4)
- [Testcase Voters](#voters)
- [Testcase Hot Swap](#hot-swap)
- [Testcase QuickSort](#quicksort)

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
To check if the race condition has occured, we check the balance of Stevie if this is greater then 0. If this is so, we know that the deposit arrived before the withdraw.
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
This testcase and the [voters testcase](#voters) brought a sporadic bug up, that caused the actor system to hang while shutting it down.

To solve this bug, we have adapted the TestHelper of Bita to add a timeout in the shutdown code. This will throw an exception, but will prevent the test to hang indefinitely.  
An other approache was suggested by the thesis promotor @PCordemans to use a supervising actor that will hold the actors under test as childeren.
This allowed us to rewrite the TestHelper of Bita to use a single actor system instance, which eliminated the need of shutting it and re-creating it every new test.
Although this approache isn't viable yet, due to an runtime error.


## Voters

The voters testcase is an alternative to [testcase bank](#testcase-bank). In this testcase, we have 2 voters and 1 ballot.
Upon testing, the ballot will receive a list of voters that he needs to traverse. Each element in that will list will be asked to cast a vote to determine the winner. Each votee will elect himself of course. The ballot will register the last vote as the winner.
  
This testcase and [testcase bank4](#testcase-bank4) was prone to bug that also present in previous testcases, for instance [testcase bank3](#testcase-bank3) and [testcase bank2](#testcase-bank2). But in those cases, the bug only occured sporadic and was hard to reproduce.
With the voter testcase, this always occured on the same shedule. 


## Hot Swap

The hot swap testcase is intended to see if Bita is able to detect race condition due to changing receive handling. 
Bita is a special criterion for this, called PMHR.

The testcase is very simple (only 2 messages), perhaps to simple since Bita is unable to generate any schedules for it.


## QuickSort

This testcase is borrowed from Bita and is an implementation of the Quick Sort algoritme with Akka actors.
The testcase was used to see what Bita would do with a fully deterministic example.