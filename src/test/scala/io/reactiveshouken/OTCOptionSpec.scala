package io.reactiveshouken

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class OTCOptionSpec extends ScalaTestWithActorTestKit() with AnyWordSpecLike {

  import OTCOption._

  "The OTC Option contract" should {

    "throw an IllegalArgumentException when 'ContractId' is empty" in {
      intercept[IllegalArgumentException] { OTCOption("", "TOYOTA", new Quantity(10000), Put, Sell) }
    }

    "throw an IllegalArgumentException when 'Quantity' is zero" in {
      intercept[IllegalArgumentException] { OTCOption("123456789", "TOYOTA", new Quantity(0), Put, Sell) }
    }

  }

}
