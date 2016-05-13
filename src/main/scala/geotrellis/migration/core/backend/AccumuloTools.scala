package geotrellis.migration.core.backend

import geotrellis.migration.cli.{AccumuloArgs, TransformArgs}
import geotrellis.migration.core.AttributeStoreTools
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import org.apache.accumulo.core.client.BatchWriterConfig
import org.apache.accumulo.core.data.{Range, Value}
import org.apache.accumulo.core.security.Authorizations
import org.apache.hadoop.io.Text
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConversions._

class AccumuloTools(val attributeStore: AccumuloAttributeStore) extends AttributeStoreTools {
  val format = "accumulo"

  lazy val layerIds: Seq[LayerId] = {
    fetch(None, None)
      .map { _.toString.parseJson.convertTo[(LayerId, Unit)]._1 }
      .toSeq
      .distinct
  }

  private def fetch(layerId: Option[LayerId], attributeName: Option[String]): Iterator[Value] = {
    val scanner = attributeStore.connector.createScanner(attributeStore.attributeTable, new Authorizations())
    layerId.foreach { id =>
      scanner.setRange(new Range(new Text(id.toString)))
    }
    attributeName.foreach { an =>
      scanner.fetchColumnFamily(new Text(an))
    }
    scanner.iterator.map(_.getValue)
  }

  override def delete(layerId: LayerId, attributeName: Option[String]): Unit = {
    val numThreads = 1
    val config = new BatchWriterConfig()
    config.setMaxWriteThreads(numThreads)
    val deleter = attributeStore.connector.createBatchDeleter(attributeStore.attributeTable, new Authorizations(), numThreads, config)
    deleter.setRanges(List(new Range(new Text(layerId.toString))))
    attributeName.foreach { name =>
      deleter.fetchColumnFamily(new Text(name))
    }
    deleter.delete()
  }

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): List[(Option[LayerId], T)] = {
    fetch(layerId, attributeName)
      .map { s => layerId -> s.toString.parseJson.convertTo[(LayerId, T)]._2 }
      .toList
  }

  def layerMove(layerName: String, args: TransformArgs): Unit = genericLayerMove[AccumuloLayerHeader](layerName, args)
}

object AccumuloTools {
  def apply(args: AccumuloArgs) = {
    val instance = AccumuloInstance(args.instanceName, args.zookeeper, args.user, args.token)
    val store = new AccumuloAttributeStore(connector = instance.connector, args.table)
    new AccumuloTools(store)
  }
}
