package xurl.resources

import xurl.config.AppConfig

import cats.effect._
import cats.effect.std.Console
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer
import skunk._

sealed abstract class AppResources[F[_]](
    val pg: Resource[F, Session[F]],
    val redis: RedisCommands[F, String, String]
)

object AppResources {
  def make[F[_]: Async: Console: MkRedis](conf: AppConfig): Resource[F, AppResources[F]] = {
    implicit val tracer: Tracer[F] = Tracer.noop
    implicit val meter: Meter[F]   = Meter.noop

    for {
      pgPool <- Session.pooled[F](
        host = conf.db.host,
        port = conf.db.port,
        user = conf.db.user,
        database = conf.db.database,
        password = conf.db.password,
        max = conf.db.connections
      )
      redis <- Redis[F].utf8(conf.cache.url)
    } yield new AppResources[F](pgPool, redis) {}
  }
}
