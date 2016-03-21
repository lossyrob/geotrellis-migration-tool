package geotrellis.migration

import geotrellis.migration.core.TransformArgs
import geotrellis.migration.core.backend.{AccumuloTools, S3Tools}

import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.s3.{S3AttributeStore, S3LayerHeader}

import org.apache.accumulo.core.client.security.tokens.PasswordToken

object Main extends App {
  val instance = AccumuloInstance("gis", "localhost", "root", new PasswordToken("secret"))
  val store0 = new AccumuloAttributeStore(connector = instance.connector, "landsat")
  val tools0 = new AccumuloTools(store0)
  tools0.layerMove("BLUE", TransformArgs(
    typeName = "zorder"
  ))

  println(store0.readLayerAttributes[AccumuloLayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](LayerId("BLUE", 3)))

  val store1 = new S3AttributeStore("geotrellis-test", "migration-test")
  val tools1 = new S3Tools(store1)
  tools1.layerMove("RED1", TransformArgs(
    typeName = "zorder"
  ))

  println(store1.readLayerAttributes[S3LayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](LayerId("RED1", 3)))

}
