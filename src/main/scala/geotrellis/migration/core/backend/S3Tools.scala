package geotrellis.migration.core.backend

import geotrellis.migration.core.AttributeStoreTools
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.spark.io.s3.S3AttributeStore._
import com.amazonaws.services.s3.model.AmazonS3Exception
import spray.json._
import DefaultJsonProtocol._

import scala.io.Source
import java.nio.charset.Charset

import geotrellis.migration.cli.{S3Args, TransformArgs}

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

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): List[(Option[LayerId], T)] = {
    val path: String = (layerId, attributeName) match {
      case (Some(id), None)       => layerPath(id)
      case (None, Some(attr))     => attributeStore.attributePrefix(attr)
      case (Some(id), Some(attr)) => attributeStore.attributePath(id, attr)
      case _                      => attributeStore.path(attributeStore.prefix, "_attributes", ".json")
    }

    attributeStore.s3Client
      .listObjectsIterator(attributeStore.bucket, path)
      .map{ os =>
        try {
          val (id, v) = readKey[T](os.getKey)
          Some(id) -> v
        } catch {
          case e: AmazonS3Exception =>
            throw new LayerIOError(s"Unable to list $attributeName attributes from ${attributeStore.bucket}/${os.getKey}").initCause(e)
        }
      }
      .toList
  }

  def layerMove(layerName: String, args: TransformArgs): Unit = genericLayerMove[S3LayerHeader](layerName, args)
}

object S3Tools {
  def apply(args: S3Args) = new S3Tools(new S3AttributeStore(args.bucket, args.prefix))
}
