package io.reactiveshouken

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object OTCOption {

  /* Protocol */
  sealed trait Command
  case class EnterContract(inst: Instrument, qty: Quantity, putCall: PutCall, buySell: BuySell)
      extends Command
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

  case class OptionState(inst: Instrument, qty: Quantity, putCall: PutCall, buySell: BuySell)

  def apply(contractId: ContractId): Behavior[Command] = {
    require(contractId.value.nonEmpty, "contractId is required.")
    new OTCOption(contractId).inactive()
  }

}

import OTCOption._

class OTCOption(aContractId: ContractId) {

  private def inactive(): Behavior[Command] =
    Behaviors.receiveMessage {
      case terms: EnterContract =>
        // TODO - don't require it, ignore invalid quantity commands
        require(terms.qty.value > 0, "quantity must be greater than 0")
        effective(OptionState(terms.inst, terms.qty, terms.putCall, terms.buySell))
      case _ =>
        Behaviors.same
    }

  private def effective(terms: OptionState): Behavior[Command] =
    Behaviors.receiveMessage {
      case PartialExercise(exerciseQty) =>
        // TODO - can the option be exercised? Assuming yes, for now
        // TODO - don't exercise if the exerciseQty is zero
        effective(terms.copy(qty = new Quantity(terms.qty.value - exerciseQty.value)))
      case GetState(replyTo: ActorRef[StateMsg]) =>
        replyTo ! StateMsg(terms.qty)
        Behaviors.same
      case terms: EnterContract =>
        // this is not handled, contract already entered
        Behaviors.same
    }

}
