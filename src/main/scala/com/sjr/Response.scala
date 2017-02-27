package com.sjr

import play.api.libs.json.{JsResult, JsValue, Json, Reads}

/**
  *
  * Note: example from Konrad Wudkowski
  */

case class TransferAccount(foo: String)

object TransferAccount {
  implicit val reads = Json.reads[TransferAccount]
}

sealed trait Response
case class WithoutTransferAccount(creationReason: String) extends Response
case class WithTransferAccount(transferAccount: TransferAccount) extends Response

object Response {

  val withoutTransferAccountReads = Json.reads[WithoutTransferAccount]//.map(_.asInstanceOf[Response])
  val withTransferAccountReads = Json.reads[WithTransferAccount]//.map(_.asInstanceOf[Response])

  implicit val reads = new Reads[Response] {
    def reads(json: JsValue): JsResult[Response] = {
      (json \ "creationReason").validate[String].flatMap {
        case "Transferred" => withTransferAccountReads.reads(json)
        case _ => withoutTransferAccountReads.reads(json)
      }
    }
  }
}

object Main extends App {

  val jsonWith =
    """
      | { "creationReason": "Transferred",
      |   "transferAccount" :
      |   {
      |      "foo": "Transferred"
      |   }
      | }
    """.stripMargin

  val jsonWithout =
    """
      | { "creationReason": "Not Transferred"
      | }
    """.stripMargin

  val jsValueWith = Json.parse(jsonWith)
  val jsValueWithout = Json.parse(jsonWithout)

  println(jsValueWith.validate[Response].get)
  println(jsValueWithout.validate[Response].get)

}
