package geotrellis.migration.cli

import monocle.macros.Lenses
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

@Lenses
case class HadoopArgs(rootPath: Path = new Path("/"), uri: String = "") {
  def getConfiguration = {
    val conf = new Configuration
    conf.set("fs.defaultFS", uri)
    conf
  }
}
