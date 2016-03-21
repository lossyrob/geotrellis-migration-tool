package geotrellis.migration.core

import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.index.KeyIndex
import geotrellis.raster.{MultibandTile, Tile}
import geotrellis.spark.io.avro.AvroRecordCodec
import geotrellis.spark.io.avro.codecs.KeyValueRecordCodec

import org.apache.avro.Schema
import spray.json._
import DefaultJsonProtocol._

import scala.reflect.ClassTag

trait AttributeStoreTools {
  val attributeStore: AttributeStore
  val layerIds: Seq[LayerId]
  val format: String

  def readAll[T: JsonFormat](layerId: Option[LayerId], attributeName: Option[String]): List[(Option[LayerId], T)]

  def delete(layerId: LayerId, attributeName: Option[String]): Unit =
    attributeName.fold(attributeStore.delete(layerId))(attributeStore.delete(layerId, _))

  def loadUpdatedMetadata[H: JsonFormat, M: JsonFormat, K: JsonFormat](layerName: String, args: TransformArgs): List[(LayerId, (H, M, K, Schema))] = {
    layerIds
      .filter(_.name == layerName)
      .flatMap(id => readAll[JsValue](Some(id), Some("metadata")).map { case (k, v) => k.get -> metadataTransfrom[H, M, K](v, args) })
      .toList
  }

  def move[H: JsonFormat, K: SpatialComponent: JsonFormat: AvroRecordCodec: ClassTag](layerName: String, args: TransformArgs): Unit = {
    loadUpdatedMetadata[H, TileLayerMetadata[K], KeyIndex[K]](layerName, args).foreach {
      case (id, (header, metadata, keyIndex, schema)) =>
        delete(id, None)
        if(schema != null) attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, schema)
        else {
          if(args.tileType == "multiband")
            attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, KeyValueRecordCodec[K, MultibandTile].schema)
          else
            attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, KeyValueRecordCodec[K, Tile].schema)
        }
    }
  }

  def genericLayerMove[H: JsonFormat](layerName: String, args: TransformArgs): Unit =
    args
      .copy(format = this.format)
      .temporalResolution
      .fold(move[H, SpatialKey](layerName, args))(_ => move[H, SpaceTimeKey](layerName, args))

  def layerMove(layerName: String, args: TransformArgs): Unit
}
