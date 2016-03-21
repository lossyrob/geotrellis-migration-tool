package geotrellis.migration.json.from

import geotrellis.proj4.CRS
import geotrellis.spark._
import geotrellis.spark.tiling.LayoutDefinition
import geotrellis.raster._
import geotrellis.spark.io.index
import geotrellis.raster.io._
import geotrellis.vector._
import geotrellis.vector.io._

import com.github.nscala_time.time.Imports._
import org.apache.avro.Schema
import spray.json._

import scala.reflect.ClassTag

object Implicits {
  implicit def keyIndexFormat[K: ClassTag]: RootJsonFormat[index.KeyIndex[K]] =
    new JavaSerializationJsonFormat[index.KeyIndex[K]]

  implicit object CRSFormat extends RootJsonFormat[CRS] {
    def write(crs: CRS) =
      JsString(crs.toProj4String)

    def read(value: JsValue): CRS =
      value match {
        case JsString(proj4String) => CRS.fromString(proj4String)
        case _ =>
          throw new DeserializationException("CRS must be a proj4 string.")
      }
  }

  implicit object LayerIdFormat extends RootJsonFormat[LayerId] {
    def write(id: LayerId) =
      JsObject(
        "name" -> JsString(id.name),
        "zoom" -> JsNumber(id.zoom)
      )

    def read(value: JsValue): LayerId =
      value.asJsObject.getFields("name", "zoom") match {
        case Seq(JsString(name), JsNumber(zoom)) =>
          LayerId(name, zoom.toInt)
        case _ =>
          throw new DeserializationException("LayerId expected")
      }
  }

  implicit object LayoutDefinitionFormat extends RootJsonFormat[LayoutDefinition] {
    def write(obj: LayoutDefinition) =
      JsObject(
        "extent" -> obj.extent.toJson,
        "tileLayout" -> obj.tileLayout.toJson
      )

    def read(json: JsValue) =
      json.asJsObject.getFields("extent", "tileLayout") match {
        case Seq(extent, tileLayout) =>
          LayoutDefinition(extent.convertTo[Extent], tileLayout.convertTo[TileLayout])
        case _ =>
          throw new DeserializationException("LayoutDefinition expected")
      }
  }

  implicit object RasterMetaDataFormat extends RootJsonFormat[RasterMetaData] {
    def write(metaData: RasterMetaData) =
      JsObject(
        "cellType" -> metaData.cellType.toJson,
        "extent" -> metaData.extent.toJson,
        "layoutDefinition" -> metaData.layout.toJson,
        "crs" -> metaData.crs.toJson
      )

    def read(value: JsValue): RasterMetaData =
      value.asJsObject.getFields("cellType", "extent", "layoutDefinition", "crs") match {
        case Seq(cellType, extent, layoutDefinition, crs) =>
          RasterMetaData(
            cellType.convertTo[CellType],
            layoutDefinition.convertTo[LayoutDefinition],
            extent.convertTo[Extent],
            crs.convertTo[CRS]
          )
        case _ =>
          throw new DeserializationException("RasterMetaData expected")
      }
  }

  implicit object RootDateTimeFormat extends RootJsonFormat[DateTime] {
    def write(dt: DateTime) = JsString(dt.withZone(DateTimeZone.UTC).toString)

    def read(value: JsValue) =
      value match {
        case JsString(dateStr) =>
          DateTime.parse(dateStr)
        case _ =>
          throw new DeserializationException("DateTime expected")
      }
  }

  implicit object SchemaFormat extends RootJsonFormat[Schema] {
    def read(json: JsValue) = (new Schema.Parser).parse(json.toString())
    def write(obj: Schema) = obj.toString.parseJson
  }
}