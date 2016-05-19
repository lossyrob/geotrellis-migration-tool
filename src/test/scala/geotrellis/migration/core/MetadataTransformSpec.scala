package geotrellis.migration.core

import geotrellis.migration.cli.TransformArgs
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.file._
import geotrellis.spark.io.index.KeyIndex

import org.apache.avro.Schema
import org.scalatest._
import spray.json._
import DefaultJsonProtocol._

class MetadataTransformSpec extends FunSpec with Matchers {
  def getJson(resource: String): String = {
    val stream = getClass.getResourceAsStream(resource)
    val lines = scala.io.Source.fromInputStream(stream).getLines
    val json = lines.mkString(" ")
    stream.close()
    json
  }

  describe("MetadataTransforms") {
    it("should transform old temporal metadata up to new") {
      val (oldJson, newJson) = getJson("/test-temporal-old.json") -> getJson("/test-temporal-new.json")
      val (hn, mn, kn, sn) = metadataTransform[FileLayerHeader, TileLayerMetadata[SpaceTimeKey], KeyIndex[SpaceTimeKey]](oldJson.parseJson, TransformArgs(
        indexType = "zorder",
        format    = "file",
        temporalResolution = Some(1000L * 60 * 60 * 365 * 1)
      ))

      val (ho, mo, ko, so) = newJson.parseJson.convertTo[JsObject].convertTo[JsObject].getFields("header", "metadata", "keyIndex", "schema") match {
        case Seq(h, m, k, s) => {
          (h.convertTo[FileLayerHeader], m.convertTo[TileLayerMetadata[SpaceTimeKey]], k.convertTo[KeyIndex[SpaceTimeKey]], Some(s.convertTo[Schema]))
        }
      }

      hn should be(ho)
      mn should be(mo)
      kn.toJson should be(ko.toJson)
      sn should be(so)
    }

    it("should transform old spatial metadata up to new") {
      val (oldJson, newJson) = getJson("/test-spatial-old.json") -> getJson("/test-spatial-new.json")
      val (hn, mn, kn, sn) = metadataTransform[FileLayerHeader, TileLayerMetadata[SpatialKey], KeyIndex[SpatialKey]](oldJson.parseJson, TransformArgs(
        indexType = "zorder",
        format    = "file"
      ))

      val (ho, mo, ko, so) = newJson.parseJson.convertTo[JsObject].convertTo[JsObject].getFields("header", "metadata", "keyIndex", "schema") match {
        case Seq(h, m, k, s) => {
          (h.convertTo[FileLayerHeader], m.convertTo[TileLayerMetadata[SpatialKey]], k.convertTo[KeyIndex[SpatialKey]], Some(s.convertTo[Schema]))
        }
      }

      hn should be(ho)
      mn should be(mo)
      kn.toJson should be(ko.toJson)
      sn should be(so)
    }
  }
}
