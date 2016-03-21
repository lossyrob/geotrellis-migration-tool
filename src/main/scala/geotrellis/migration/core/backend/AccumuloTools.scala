package geotrellis.migration.core.backend

import geotrellis.migration.core.AttributeStoreTools
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._

import org.apache.accumulo.core.data.{Range, Value}
import org.apache.accumulo.core.security.Authorizations
import org.apache.hadoop.io.Text
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConversions._

class AccumuloTools(val attributeStore: AccumuloAttributeStore) extends AttributeStoreTools {
  val format = "accumulo"
  lazy val layerIds = attributeStore.layerIds

  private def fetch(layerId: Option[LayerId], attributeName: Option[String]): Iterator[Value] = {
    val scanner = attributeStore.connector.createScanner(attributeStore.attributeTable, new Authorizations())
    layerId.foreach { id =>
      scanner.setRange(new Range(attributeStore.layerIdText(id)))
    }
    attributeName.foreach { an =>
      scanner.fetchColumnFamily(new Text(an))
    }
    scanner.iterator.map(_.getValue)
  }

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): Map[LayerId, T] = {
    fetch(layerId, attributeName)
      .map { _.toString.parseJson.convertTo[(LayerId, T)] }
      .toMap
  }
}
