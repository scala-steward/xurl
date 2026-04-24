package xurl.health

import cats.effect._
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer
import skunk._
import weaver.IOSuite

object SkunkHealthSuite extends IOSuite {

  implicit val tracer: Tracer[IO] = Tracer.noop
  implicit val meter: Meter[IO]   = Meter.noop

  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "xurl",
      password = Some("postgres"),
      max = 10
    )

  test("ok returns true") { pg =>
    val health = new SkunkHealth[IO](pg)
    health.ok.map(expect(_))
  }
}
