package geotrellis.migration

import geotrellis.migration.cli.MainOptions
import geotrellis.migration.core.backend._

object Main extends App {
  MainOptions.parse(args) match {
    case Some(config) => {
      config.transformArgs.format match {
        case "accumulo" => AccumuloTools(config.accumuloArgs)
        case "file"     => FileTools(config.fileArgs)
        case "hadoop"   => HadoopTools(config.hadoopArgs)
        case "s3"       => S3Tools(config.s3Args)
        case ""         => throw new Exception("No backend specified")
        case _          => throw new Exception("Wrong backend specified")
      }
    }
    case None => throw new Exception("No valid arguments passed")
  }
}
