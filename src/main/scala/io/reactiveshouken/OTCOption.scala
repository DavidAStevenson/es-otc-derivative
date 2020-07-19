package io.reactiveshouken

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior

object OTCOption {

  /* Protocol */
  sealed trait Command
  final case class EnterContract(
      inst: Instrument,
      qty: Quantity,
      putCall: PutCall,
      buySell: BuySell
  ) extends Command
  case class PartialExercise(q: Quantity) extends Command
  case class GetState(replyTo: ActorRef[StateMsg]) extends Command

  sealed trait Response
  case class StateMsg(effectiveQuantity: Quantity) extends Response

  /* Events */
  sealed trait Event
  final case class ContractEntered(
      contractId: ContractId,
      instrument: Instrument,
      quantity: Quantity,
      putCall: PutCall,
      buySell: BuySell
  ) extends Event
  final case class PartiallyExercised(contractId: ContractId, quantity: Quantity) extends Event

  /* State */

  class ContractId(val value: String)
  class Instrument(val value: String)
  class Quantity(val value: Int)

  sealed abstract class PutCall
  case object Put extends PutCall
  case object Call extends PutCall

  sealed abstract class BuySell
  case object Buy extends BuySell
  case object Sell extends BuySell

  case class OTCOptionState(inst: Instrument, qty: Quantity, putCall: PutCall, buySell: BuySell)
  final case class State(otcOption: Option[OTCOptionState]) {

    def enterContract(inst: Instrument, qty: Quantity, putCall: PutCall, buySell: BuySell): State =
      copy(otcOption = Some(OTCOptionState(inst, qty, putCall, buySell)))

    def partiallyExercise(exerciseQty: Quantity): State = {
      otcOption match {
        case None =>
          // no option state should validated for before even getting here...
          State(None)
        case Some(oldState) =>
          val newState: OTCOptionState =
            oldState.copy(qty = new Quantity(oldState.qty.value - exerciseQty.value))
          State(Some(newState))
      }
    }
  }
  object State {
    val empty = State(None)
  }

  def handleCommand(contractId: ContractId, state: State, command: Command): Effect[Event, State] =
    command match {
      case GetState(replyTo: ActorRef[StateMsg]) =>
        state.otcOption match {
          case Some(otcOption) => replyTo ! StateMsg(otcOption.qty)
          case None            => // TODO anything?
        }
        Effect.none
      case EnterContract(inst, qty, putCall, buySell) =>
        // TODO - don't require it, ignore invalid quantity commands
        require(qty.value > 0, "quantity must be greater than 0")
        Effect
          .persist(ContractEntered(contractId, inst, qty, putCall, buySell))
          .thenRun(_ => println("persisted a ContractEntered event"))
      case PartialExercise(exerciseQty) =>
        // TODO validate... qty should be neither 0 nor current qty, to be "partial"
        // TODO - can the option be exercised? Assuming yes, for now
        // TODO - don't exercise if the exerciseQty is zero
        Effect
          .persist(PartiallyExercised(contractId, exerciseQty))
          .thenRun(_ => println(s"persisted a PartiallyExercised($exerciseQty) event"))
    }

  def handleEvent(state: State, event: Event): State =
    event match {
      case ContractEntered(_, inst, qty, putCall, buySell) =>
        state.enterContract(inst, qty, putCall, buySell)
      case PartiallyExercised(_, exerciseQty) =>
        state.partiallyExercise(exerciseQty)
    }

  def apply(contractId: ContractId): Behavior[Command] = {
    require(contractId.value.nonEmpty, "contractId is required.")
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("OTCOption", contractId.value),
      State.empty,
      (state, command) => handleCommand(contractId, state, command),
      (state, event) => handleEvent(state, event)
    )

  }
}
