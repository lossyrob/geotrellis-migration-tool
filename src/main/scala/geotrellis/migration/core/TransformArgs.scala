package geotrellis.migration.core

case class TransformArgs(format: String, typeName: String, xResolution: Int, yResolution: Int, temporalResolution: Option[Long] = None)
