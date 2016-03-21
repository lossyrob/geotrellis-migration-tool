package geotrellis.migration.core

import geotrellis.spark._
import geotrellis.spark.io._
import org.apache.avro.Schema
import spray.json._
import DefaultJsonProtocol._
import geotrellis.spark.io.accumulo.AccumuloLayerHeader
import geotrellis.spark.io.index.KeyIndex

import scala.reflect.ClassTag

trait AttributeStoreTools {
  val attributeStore: AttributeStore
  val layerIds: Seq[LayerId]

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): Map[LayerId, T]

  def loadUpdatedMetadata[H: JsonFormat, M: JsonFormat, K: JsonFormat](layerName: String, args: TransformArgs): List[(LayerId, (H, M, K, Schema))] = {
    layerIds
      .filter(_.name == layerName)
      .flatMap(id => readAll[JsObject](Some(id), None).toList.map { case (k, v) => k -> metadataTransfrom[H, M, K](v, args) })
      .toList
  }

  def move[K: SpatialComponent: JsonFormat: ClassTag](layerName: String, args: TransformArgs): Unit = {
    loadUpdatedMetadata[AccumuloLayerHeader, TileLayerMetadata[K], KeyIndex[K]](layerName, args).foreach {
      case (id, (header, metadata, keyIndex, schema)) =>
        attributeStore.delete(id)
        attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, schema)
    }
  }

  def genericMove(layerName: String, args: TransformArgs): Unit =
    args.temporalResolution.fold(move[SpatialKey](layerName, args))(_ => move[SpaceTimeKey](layerName, args))
}
