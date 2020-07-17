package io.reactiveshouken

object OTCOption {

  class ContractId(val value: String)
  class Instrument(val value: String)
  class Quantity(val value: Int)

  sealed abstract class PutCall
  case object Put extends PutCall
  case object Call extends PutCall

  sealed abstract class BuySell
  case object Buy extends BuySell
  case object Sell extends BuySell

  def apply(
      contractId: ContractId,
      instrument: Instrument,
      quantity: Quantity,
      putCall: PutCall,
      buySell: BuySell
  ): Unit = {
    require(contractId.value.nonEmpty, "contractId is required.")
    require(quantity.value > 0, "quantity must be greater than 0")
  }

}
