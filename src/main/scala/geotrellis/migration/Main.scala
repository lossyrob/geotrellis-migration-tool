package geotrellis.migration

import geotrellis.migration.cli.MainOptions
import geotrellis.migration.core.backend._

object Main {
  def main(args: Array[String]): Unit = {
    MainOptions.parse(args) match {
      case Some(config) => {
        config.transformArgs.format match {
          case "accumulo" => AccumuloTools(config.accumuloArgs).run(config.transformArgs)
          case "file" => FileTools(config.fileArgs).run(config.transformArgs)
          case "hadoop" => HadoopTools(config.hadoopArgs).run(config.transformArgs)
          case "s3" => S3Tools(config.s3Args).run(config.transformArgs)
          case "" => throw new Exception("No backend specified")
          case _ => throw new Exception("Wrong backend specified")
        }
      }
      case None => throw new Exception("No valid arguments passed")
    }
  }
}
