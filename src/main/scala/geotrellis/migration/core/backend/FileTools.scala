package geotrellis.migration.core.backend

import geotrellis.migration.core.AttributeStoreTools
import geotrellis.spark._
import geotrellis.spark.io.file.FileAttributeStore
import geotrellis.spark.io.file.FileAttributeStore._

import org.apache.commons.io.filefilter.WildcardFileFilter
import spray.json._

import java.io.FileFilter

class FileTools(val attributeStore: FileAttributeStore) extends AttributeStoreTools {
  val format = "file"
  lazy val layerIds = attributeStore.layerIds

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): Map[LayerId, T] = {
    val filter: FileFilter = (layerId, attributeName) match {
      case (Some(id), None)       => new WildcardFileFilter(s"${id.name}${SEP}${id.zoom}${SEP}*.json")
      case (None, Some(attr))     => new WildcardFileFilter(s"*${SEP}${attr}.json")
      case (Some(id), Some(attr)) => new WildcardFileFilter(s"${id.name}${SEP}${id.zoom}${SEP}${attr}.json")
      case _                      => new WildcardFileFilter("*.json")
    }

    attributeStore.attributeDirectory
      .listFiles(filter)
      .map(attributeStore.read[T])
      .toMap
  }
}
