package geotrellis.migration

import geotrellis.migration.core.TransformArgs
import geotrellis.migration.core.backend.{AccumuloTools, HadoopTools, S3Tools}
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.hadoop.{HadoopAttributeStore, HadoopLayerHeader, HdfsUtils}
import geotrellis.spark.io.s3.{S3AttributeStore, S3LayerHeader}

import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

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

  val conf = new Configuration
  conf.set("fs.defaultFS", "hdfs://localhost:9000")
  HdfsUtils.renamePath(new Path("/geotrellis-testzzz/attributes"), new Path("/geotrellis-testzzz/_attributes"), conf)
  val store2 = new HadoopAttributeStore(new Path("/geotrellis-testzzz"), conf)
  val tools2 = new HadoopTools(store2)
  tools2.layerMove("RED", TransformArgs(
    typeName = "zorder"
  ))

  println(store2.readLayerAttributes[HadoopLayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](LayerId("RED", 3)))
}
