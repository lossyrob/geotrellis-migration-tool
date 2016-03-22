package geotrellis.migration.cli

import TransformArgs._
import monocle.macros.GenLens

case class TransformArgs(
  indexType: String = indexTypes.head, // zorder by default
  tileType: String  = tileTypes.head, // only for hadoop && file
  xResolution: Int  = 0, // for hilbert
  yResolution: Int  = 0, // for hilbert
  format: String    = "", // always empty, set by backend tools
  temporalResolution: Option[Long] = None // spatial index by default, used for all space indexes
) {
  if (!indexTypes.contains(indexType)) throw new Exception(s"Unsupported index type. Available index types: ${indexTypes.mkString(", ")}")
  if (!tileTypes.contains(tileType)) throw new Exception(s"Unsupported tile type. Available tile types: ${tileTypes.mkString(", ")}")
}

object TransformArgs {
  val indexTypes   = List("zorder", "hilbert", "rowmajor")
  val tileTypes    = List("singleband", "multiband")
  val backendTypes = List("hadoop", "file", "s3", "accumulo")

  val indexType          = GenLens[TransformArgs](_.indexType)
  val tileType           = GenLens[TransformArgs](_.tileType)
  val xResolution        = GenLens[TransformArgs](_.xResolution)
  val yResolution        = GenLens[TransformArgs](_.yResolution)
  val format             = GenLens[TransformArgs](_.format)
  val temporalResolution = GenLens[TransformArgs](_.temporalResolution)
}
