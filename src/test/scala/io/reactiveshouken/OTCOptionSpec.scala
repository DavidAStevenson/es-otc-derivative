package io.reactiveshouken

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class OTCOptionSpec extends ScalaTestWithActorTestKit() with AnyWordSpecLike {

  import OTCOption._

  "The OTC Option contract" should {

    "throw an IllegalArgumentException when 'ContractId' is empty" in {
      intercept[IllegalArgumentException] {
        OTCOption(new ContractId(""))
      }
    }

    /*
    "throw an IllegalArgumentException when 'Quantity' is zero" in {
      intercept[IllegalArgumentException] {
        val contract = testKit.spawn(OTCOption(new ContractId("abcdefgh")))
        val initialQuantity = 0
        contract ! OTCOption.EnterContract(new Instrument("TOYOTA"), new Quantity(initialQuantity), Put, Sell)
      }
    }
     */

    "come into effect when supplied contract particulars" in {
      val contract = testKit.spawn(OTCOption(new ContractId("abcdfe12")))
      val initialQuantity = 10000
      contract ! OTCOption.EnterContract(
        new Instrument("TOYOTA"),
        new Quantity(initialQuantity),
        Put,
        Sell
      )

      val probe = testKit.createTestProbe[OTCOption.StateMsg]
      contract ! OTCOption.GetState(probe.ref)
      val result = probe.expectMessageType[OTCOption.StateMsg]
      result.effectiveQuantity.value shouldBe initialQuantity
    }

    /*
    "decrease outstanding quantity when partially exercised" in {
      val initialQuantity = 10000
      val exerciseQuantity = 2500

      val contract = testKit.spawn(
        OTCOption(
          new ContractId("123456789"),
          new Instrument("TOYOTA"),
          new Quantity(initialQuantity),
          Put,
          Sell
        )
      )
      contract ! OTCOption.PartialExercise(new Quantity(exerciseQuantity))

      val probe = testKit.createTestProbe[OTCOption.StateMsg]
      contract ! OTCOption.GetState(probe.ref)
      val result = probe.expectMessageType[OTCOption.StateMsg]
      result.effectiveQuantity.value shouldBe (initialQuantity - exerciseQuantity)
    }
     */

    /*
    "keep its state" in {
      val contractId = new ContractId("abcdef01")

      val initialQuantity = 10000
      val exerciseQuantity = 2500

      val contract = testKit.spawn(
        OTCOption(
          contractId,
          new Instrument("TOYOTA"),
          new Quantity(initialQuantity),
          Put,
          Sell
        )
      )
      contract ! OTCOption.PartialExercise(new Quantity(exerciseQuantity))

      val probe = testKit.createTestProbe[OTCOption.StateMsg]
      contract ! OTCOption.GetState(probe.ref)
      val result = probe.expectMessageType[OTCOption.StateMsg]
      result.effectiveQuantity.value shouldBe (initialQuantity - exerciseQuantity)

      testKit.stop(contract)

      // no... I don't like modelling this without an explicit "Contract Entered" event
      //val restartedContract = testKit
    }
     */

  }
}
