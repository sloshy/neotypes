package neotypes.zio.stream

import zio.Task
import zio.DefaultRuntime
import neotypes.BaseIntegrationSpec
import neotypes.implicits.mappers.results._
import neotypes.implicits.syntax.session._
import neotypes.implicits.syntax.string._
import neotypes.zio.implicits._
import neotypes.zio.stream.implicits._
import org.scalatest.AsyncFlatSpec

class ZioStreamSpec extends AsyncFlatSpec with BaseIntegrationSpec {
  it should "work with zio.ZStream" in {
    val runtime = new DefaultRuntime {}

    val s = driver.session().asScala[Task]

    val program =
      "match (p:Person) return p.name"
        .query[Int]
        .stream[ZioStream](s)
        .runCollect

    runtime
      .unsafeRunToFuture(program)
      .map { names =>
        assert(names == (0 to 10).toList)
      }
  }

  override val initQuery: String =
    (0 to 10).map(n => s"CREATE (:Person {name: $n})").mkString("\n")
}
