package geotrellis.migration

import geotrellis.migration.core.TransformArgs
import geotrellis.migration.core.backend.AccumuloTools
import geotrellis.spark.io.accumulo.{AccumuloAttributeStore, AccumuloInstance}
import org.apache.accumulo.core.client.security.tokens.PasswordToken

object Main extends App {
  val instance = AccumuloInstance("gis", "zookeeper", "root", new PasswordToken("secret"))
  val store = new AccumuloAttributeStore(connector = instance.connector, "table")
  val tools = new AccumuloTools(store)
  tools.genericMove("BLUE", TransformArgs(
    typeName = "zorder"
  ))
}
