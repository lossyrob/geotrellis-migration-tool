package geotrellis.migration.json.from

import geotrellis.raster._
import geotrellis.spark.tiling._
import geotrellis.vector.Extent
import geotrellis.proj4.CRS

/**
  * @param cellType    value type of each cell
  * @param layout      definition of the tiled raster layout
  * @param extent      Extent covering the source data
  * @param crs         CRS of the raster projection
  */
case class RasterMetaData(cellType: CellType,
                          layout: LayoutDefinition,
                          extent: Extent,
                          crs: CRS)