package org.sandbox.example

import enumeratum._

object EnumMarshalingSpec {

  trait Flag extends EnumEntry {
    def Key: Class[_]
  }

  sealed abstract class FlagX(val code: Int, val Key: Class[_] = FlagX.Key) extends Flag //with EnumEntry
  object FlagX extends Enum[FlagX] {
    val values = findValues
    case object Off extends FlagX(0)
    case object On  extends FlagX(1)
    def Key = classOf[FlagX]
  }
  sealed abstract class FlagY(val code: Int, val Key: Class[_] = FlagY.Key) extends Flag //with EnumEntry
  object FlagY extends Enum[FlagY] {
    val values = findValues
    case object Disable extends FlagY(0)
    case object Enable  extends FlagY(1)
    def Key = classOf[FlagY]
  }

  case class Holder(flags: Map[String, Flag])
  object Holder {
    def apply[A <: Flag](entities : A*) = new Holder(Map(entities.map(e => (e.Key.getSimpleName, e)):_*))
  }
}

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.actor.ActorSystem

object EnumProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  import EnumMarshalingSpec._
  //implicit object
  class AccountParamsFormat(implicit system: ActorSystem) extends RootJsonFormat[Holder] {
    def write(h: Holder) = JsObject( h.flags.mapValues(v => JsString(v.entryName)) )
    def read(json: JsValue) = Holder( Map(json.asJsObject.fields.collect { case (k, JsString(v)) =>  toFlag(k, v) }.flatten.toSeq : _*) )
    private def toFlag(key: String, name: String): Option[(String, Flag)] = key match {
      case "FlagX" => log(key, FlagX.values); FlagX.withNameInsensitiveOption(name).map(key -> _)
      case "FlagY" => log(key, FlagY.values); FlagY.withNameInsensitiveOption(name).map(key -> _)
    }
    private def log(obj: Any*) = system.log.info("{}", obj)
  }
}

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class EnumMarshalingSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private implicit val system = ActorSystem(getClass.getSimpleName)
  private implicit val mat    = ActorMaterializer()

  import EnumMarshalingSpec._
  import EnumProtocol._
  implicit val accountParamsFormat = new AccountParamsFormat

  "A Holder" should {
    // uncomment these lines to pass the test successfully
    //log(FlagY.values)
    //log(FlagX.values)

    "be marshalled and unmarshalled with FlagY.Enable flag" in {
      val e = Holder(FlagY.Enable)
      Marshal(e).to[ResponseEntity].map(log).flatMap(Unmarshal(_).to[Holder]).map{_ should === (e)}
    }
    "be marshalled and unmarshalled with FlagY.Disable flag" in {
      val e = Holder(FlagY.Disable)
      Marshal(e).to[ResponseEntity].map(log).flatMap(Unmarshal(_).to[Holder]).map{_ should === (e)}
    }
    "be marshalled and unmarshalled with two flag" in {
      val e = Holder(FlagX.On, FlagY.Disable)
      Marshal(e).to[ResponseEntity].map(log).flatMap(Unmarshal(_).to[Holder]).map{_ should === (e)}
    }
  }
  private def log[T](obj: T): T = { system.log.info("{}", obj); obj }

  override protected def afterAll() = {
    import scala.concurrent.Await
    import scala.concurrent.duration.DurationInt
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}