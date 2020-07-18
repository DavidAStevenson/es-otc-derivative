package io.reactiveshouken

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object OTCOption {

  /* Protocol */
  sealed trait Command
  case class PartialExercise(q: Quantity) extends Command
  case class GetState(replyTo: ActorRef[StateMsg]) extends Command

  sealed trait Response
  case class StateMsg(effectiveQuantity: Quantity) extends Response

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
  ): Behavior[Command] = {
    require(contractId.value.nonEmpty, "contractId is required.")
    require(quantity.value > 0, "quantity must be greater than 0")
    new OTCOption(contractId, instrument, quantity, putCall, buySell).effective()
  }

}

import OTCOption._

class OTCOption(
    aContractId: ContractId,
    anInstrument: Instrument,
    aQuantity: Quantity,
    aPutCall: PutCall,
    aBuySell: BuySell
) {

  private var quantity: Quantity = aQuantity

  private def effective(): Behavior[Command] =
    Behaviors.receiveMessage {
      case PartialExercise(exerciseQty) =>
        // TODO - can the option be exercised? Assuming yes, for now
        quantity = new Quantity(quantity.value - exerciseQty.value)
        Behaviors.same
      case GetState(replyTo: ActorRef[StateMsg]) =>
        replyTo ! StateMsg(quantity)
        Behaviors.same
    }

}
