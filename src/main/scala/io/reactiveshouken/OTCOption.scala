package io.reactiveshouken


object OTCOption {

  def apply(contractId: String): Unit = {
    require(contractId.nonEmpty, "contractId is required.")
  }

}
