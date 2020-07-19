package io.reactiveshouken

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class OTCOptionSpec
    extends ScalaTestWithActorTestKit(s"""
  akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
  """)
    with AnyWordSpecLike {

  import OTCOption._

  "The OTC Option contract" should {

    "throw an IllegalArgumentException when 'ContractId' is empty" in {
      intercept[IllegalArgumentException] {
        OTCOption(new ContractId(""))
      }
    }

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

    "decrease outstanding quantity when partially exercised" in {
      val contract = testKit.spawn(OTCOption(new ContractId("123456789")))
      val probe = testKit.createTestProbe[OTCOption.StateMsg]

      val initialQuantity = 10000
      val exerciseQuantity = 2500
      contract ! OTCOption.EnterContract(
        new Instrument("TOYOTA"),
        new Quantity(initialQuantity),
        Put,
        Sell
      )

      contract ! OTCOption.PartialExercise(new Quantity(exerciseQuantity))

      contract ! OTCOption.GetState(probe.ref)
      val result = probe.expectMessageType[OTCOption.StateMsg]
      result.effectiveQuantity.value shouldBe (initialQuantity - exerciseQuantity)
    }

    "keep its state" in {
      val contractId = new ContractId("abcdef01")
      val contract = testKit.spawn(OTCOption(contractId))
      val probe = testKit.createTestProbe[OTCOption.StateMsg]

      val initialQuantity = 10000
      val exerciseQuantity = 4000

      contract ! OTCOption.EnterContract(
        new Instrument("TOYOTA"),
        new Quantity(initialQuantity),
        Put,
        Sell
      )

      contract ! OTCOption.PartialExercise(new Quantity(exerciseQuantity))

      contract ! OTCOption.GetState(probe.ref)
      val result = probe.expectMessageType[OTCOption.StateMsg]
      result.effectiveQuantity.value shouldBe (initialQuantity - exerciseQuantity)

      testKit.stop(contract)

      val restartedContract = testKit.spawn(OTCOption(contractId))
      restartedContract ! OTCOption.GetState(probe.ref)
      val result2 = probe.expectMessageType[OTCOption.StateMsg]
      result2.effectiveQuantity.value shouldBe (initialQuantity - exerciseQuantity)
    }

  }
}
