package geotrellis.migration.core.backend

import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.spark.io.s3.S3AttributeStore._

import com.amazonaws.services.s3.model.AmazonS3Exception
import geotrellis.migration.core.AttributeStoreTools
import spray.json._
import DefaultJsonProtocol._

import scala.io.Source
import java.nio.charset.Charset

class S3Tools(val attributeStore: S3AttributeStore) extends AttributeStoreTools {
  val format = "s3"
  lazy val layerIds = attributeStore.layerIds

  def layerPath(id: LayerId): String =
    attributeStore.path(attributeStore.prefix, "_attributes", s"${SEP}${id.name}${SEP}${id.zoom}.json")

  def readKey[T: JsonFormat](key: String): (LayerId, T) = {
    val is = attributeStore.s3Client.getObject(attributeStore.bucket, key).getObjectContent
    val json =
      try {
        Source.fromInputStream(is)(Charset.forName("UTF-8")).mkString
      } finally {
        is.close()
      }

    json.parseJson.convertTo[(LayerId, T)]
  }

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): Map[LayerId, T] = {
    val path: String= (layerId, attributeName) match {
      case (Some(id), None)       => layerPath(id)
      case (None, Some(attr))     => attributeStore.attributePrefix(attr)
      case (Some(id), Some(attr)) => attributeStore.attributePath(id, attr)
      case _                      => attributeStore.path(attributeStore.prefix, "_attributes", ".json")
    }


    attributeStore.s3Client
      .listObjectsIterator(attributeStore.bucket, path)
      .map{ os =>
        try {
          readKey[T](os.getKey)
        } catch {
          case e: AmazonS3Exception =>
            throw new LayerIOError(s"Unable to list $attributeName attributes from ${attributeStore.bucket}/${os.getKey}").initCause(e)
        }
      }
      .toMap
  }
}
