package geotrellis.migration

import geotrellis.migration.core.TransformArgs
import geotrellis.migration.core.backend.AccumuloTools
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import org.apache.accumulo.core.client.security.tokens.PasswordToken

object Main extends App {
  val instance = AccumuloInstance("gis", "localhost", "root", new PasswordToken("secret"))
  val store = new AccumuloAttributeStore(connector = instance.connector, "landsat")
  val tools = new AccumuloTools(store)
  tools.genericMove("BLUE", TransformArgs(
    typeName = "zorder"
  ))

  println(store.readLayerAttributes[AccumuloLayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](LayerId("BLUE", 3)))
}
