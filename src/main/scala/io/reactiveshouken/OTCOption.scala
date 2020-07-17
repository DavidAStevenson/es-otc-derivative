package io.reactiveshouken

object OTCOption {

  class Quantity(val value: Int)

  def apply(contractId: String, instrument: String, quantity: Quantity): Unit = {
    require(contractId.nonEmpty, "contractId is required.")
    require(quantity.value > 0, "quantity must be greater than 0")
  }

}
