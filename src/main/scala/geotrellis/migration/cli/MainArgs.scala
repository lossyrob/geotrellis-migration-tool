package geotrellis.migration.cli

import monocle.macros.Lenses

@Lenses
case class MainArgs(
  transformArgs: TransformArgs = TransformArgs(),
  accumuloArgs: AccumuloArgs   = AccumuloArgs(),
  s3Args: S3Args               = S3Args(),
  hadoopArgs: HadoopArgs       = HadoopArgs(),
  fileArgs: FileArgs           = FileArgs()
)

