package bank4

class DelayBankSpec extends BankSpec {

    // The name of this test battery
    override def name = "bank4_delay"

    // Are we expecting certain shedules to fail?
    override def expectFailures = true

    // delay between start and end message
    override def delay = 500
}
