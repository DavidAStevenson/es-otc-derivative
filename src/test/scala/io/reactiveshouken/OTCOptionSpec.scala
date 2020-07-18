package io.reactiveshouken

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class OTCOptionSpec extends ScalaTestWithActorTestKit() with AnyWordSpecLike {

  import OTCOption._

  "The OTC Option contract" should {

    "throw an IllegalArgumentException when 'ContractId' is empty" in {
      intercept[IllegalArgumentException] {
        OTCOption(new ContractId(""), new Instrument("TOYOTA"), new Quantity(10000), Put, Sell)
      }
    }

    "throw an IllegalArgumentException when 'Quantity' is zero" in {
      intercept[IllegalArgumentException] {
        OTCOption(new ContractId("123456789"), new Instrument("TOYOTA"), new Quantity(0), Put, Sell)
      }
    }

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

  }
}
