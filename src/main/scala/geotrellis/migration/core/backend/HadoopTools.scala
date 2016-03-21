package geotrellis.migration.core.backend

import geotrellis.migration.core.AttributeStoreTools
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.hadoop._

import org.apache.hadoop.fs.Path
import spray.json.DefaultJsonProtocol._
import spray.json._

class HadoopTools(val attributeStore: HadoopAttributeStore) extends AttributeStoreTools {
  val format = "hadoop"
  lazy val layerIds = attributeStore.layerIds

  def readFile[T: JsonFormat](path: Path): Option[(LayerId, T)] = {
    HdfsUtils
      .getLineScanner(path, attributeStore.hadoopConfiguration)
      .map{ in =>
        val txt =
          try {
            in.mkString
          }
          finally {
            in.close()
          }
        txt.parseJson.convertTo[(LayerId, T)]
      }
  }

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): Map[LayerId, T] = {
    val path: Path = (layerId, attributeName) match {
      case (Some(id), None) => attributeStore.layerWildcard(id)
      case (None, Some(attr)) => attributeStore.attributeWildcard(attr)
      case (Some(id), Some(attr)) => attributeStore.attributePath(id, attr)
      case _                      => new Path("*.json")
    }

    HdfsUtils
      .listFiles(path, attributeStore.hadoopConfiguration)
      .map{ path: Path =>
        readFile[T](path) match {
          case Some(tup) => tup
          case None => throw new LayerIOError(s"Unable to list $attributeName attributes from $path")
        }
      }
      .toMap
  }
}
