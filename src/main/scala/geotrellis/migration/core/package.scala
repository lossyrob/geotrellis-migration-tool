package geotrellis.migration

import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.index.hilbert.{HilbertSpaceTimeKeyIndex, HilbertSpatialKeyIndex}
import geotrellis.spark.io.index.rowmajor.RowMajorSpatialKeyIndex
import geotrellis.spark.io.index.zcurve.{ZSpaceTimeKeyIndex, ZSpatialKeyIndex}
import org.apache.avro.Schema
import spray.json._
import DefaultJsonProtocol._
import geotrellis.migration.cli.TransformArgs

import scala.util.{Failure, Success, Try}

package object core {
  val indexTypes   = List("zorder", "hilbert", "rowmajor")
  val tileTypes    = List("singleband", "multiband")
  val backendTypes = List("hadoop", "file", "s3", "accumulo")

  // ugly keyIndex build function for in-built types
  def keyIndexBuild(keyBounds: JsObject, args: TransformArgs): JsValue = {
    def kbs = keyBounds.convertTo[KeyBounds[SpatialKey]]
    def kbt = keyBounds.convertTo[KeyBounds[SpaceTimeKey]]

    (args.indexType, args.temporalResolution) match {
      case ("rowmajor", None)    => new RowMajorSpatialKeyIndex(kbs).toJson
      case ("hilbert", None)     => HilbertSpatialKeyIndex(kbs, args.xResolution, args.yResolution).toJson
      case ("hilbert", Some(tr)) => HilbertSpaceTimeKeyIndex(kbt, args.xResolution, args.yResolution, tr.toInt).toJson
      case ("zorder", None)      => new ZSpatialKeyIndex(kbs).toJson
      case ("zorder", Some(tr))  => ZSpaceTimeKeyIndex.byMilliseconds(kbt, tr).toJson
      case _                     => throw new Exception("Wrong KeyIndex params")
    }
  }

  def metadataTransfrom[H: JsonFormat, M: JsonFormat, K: JsonFormat](old: JsValue, args: TransformArgs): (H, M, K, Option[Schema]) = {
    // layer metadata
    val (keyBounds, schema, metadata, header, keyIndex) =
      old
        .convertTo[JsObject]
        .getFields("keyBounds", "schema", "metadata", "header", "keyIndex") match {
           case Seq(kb, sc, md, he, ki) => {
             (kb.convertTo[JsObject],
             Try { sc.convertTo[JsObject] },
             md.convertTo[JsObject],
             he.convertTo[JsObject],
             ki.convertTo[JsObject])
           }
        }

    val newHeader =
      header
        .copy(fields = header.fields + ("format" -> args.format.toJson))
        .convertTo[H]

    val newMetadata =
      metadata
        .copy(fields = metadata.fields + ("bounds" -> keyBounds))
        .convertTo[M]

    val newKeyIndex = keyIndexBuild(keyBounds, args).convertTo[K]

    (newHeader, newMetadata, newKeyIndex, schema match {
      case Success(s) => Some(s.convertTo[Schema])
      case Failure(e) => Option.empty[Schema]
    })
  }
}
