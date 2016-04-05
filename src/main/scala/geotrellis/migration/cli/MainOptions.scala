package geotrellis.migration.cli

import TransformArgs._
import scopt.OptionParser
import monocle.syntax.all._
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.hadoop.fs.Path

object MainOptions {
  val parser = new OptionParser[MainArgs](Info.name) {
    head(Info.name, Info.version)

    // TransformArgs
    opt[String]("index-type") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.indexType) set x
    } validate { x =>
      if (indexTypes.contains(x)) success
      else failure(s"Option --index-type must have one of possible values: ${indexTypes.mkString(", ")}")
    } text s"index-type is a String property, available index types: ${indexTypes.mkString(", ")}"

    opt[String]("tile-type") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.tileType) set x } validate { x =>
      if (tileTypes.contains(x)) success
      else failure(s"Option --tile-type must have one of possible values: ${tileTypes.mkString(", ")}")
    } text s"tile-type is a String property, available index types: ${tileTypes.mkString(", ")}"

    opt[String]("backend") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.format) set x } validate { x =>
      if (backendTypes.contains(x)) success
      else failure(s"Option --backend must have one of possible values: ${backendTypes.mkString(", ")}")
    } text s"backend is a String property, available backend types: ${backendTypes.mkString(", ")}"

    opt[Int]("x-resolution") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.xResolution) set x } text s"x-resolution is an Int property"

    opt[Int]("y-resolution") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.yResolution) set x } text s"y-resolution is an Int property"

    opt[Long]("temporal-resolution") action { (x, c) =>
      c &|-> (MainArgs.transformArgs ^|-> TransformArgs.temporalResolution) set Some(x) } text s"temporal-resolution is a Long property"

    // AccumuloArgs
    opt[String]("istance-name") action { (x, c) =>
      c &|-> (MainArgs.accumuloArgs ^|-> AccumuloArgs.instanceName) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --instance-name must be non-empty")
    } text s"instance-name is a non-empty String property"

    opt[String]("zookeeper") action { (x, c) =>
      c &|-> (MainArgs.accumuloArgs ^|-> AccumuloArgs.zookeeper) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --zookeeper must be non-empty")
    } text s"zookeeper is a non-empty String property"

    opt[String]("user") action { (x, c) =>
      c &|-> (MainArgs.accumuloArgs ^|-> AccumuloArgs.user) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --user must be non-empty")
    } text s"user is a non-empty String property"

    opt[String]("pwd") action { (x, c) =>
      c &|-> (MainArgs.accumuloArgs ^|-> AccumuloArgs.token) set new PasswordToken(x)
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --pwd must be non-empty")
    } text s"pwd is a non-empty String property"

    opt[String]("table") action { (x, c) =>
      c &|-> (MainArgs.accumuloArgs ^|-> AccumuloArgs.table) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --table must be non-empty")
    } text s"table is a non-empty String property"

    // FileArgs
    opt[String]("file-path") action { (x, c) =>
      c &|-> (MainArgs.fileArgs ^|-> FileArgs.rootPath) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --file-path must be non-empty")
    } text s"file-path is a non-empty String property"

    // HadoopArgs
    opt[String]("hadoop-path") action { (x, c) =>
      c &|-> (MainArgs.hadoopArgs ^|-> HadoopArgs.rootPath) set new Path(x)
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --hadoop-path must be non-empty")
    } text s"hadoop-path is a non-empty String property"

    opt[String]("hadoop-defaultFS") action { (x, c) =>
      c &|-> (MainArgs.hadoopArgs ^|-> HadoopArgs.uri) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --hadoop-defaultFS must be non-empty")
    } text s"hadoop-defaultFS is a non-empty String property"

    // S3Args
    opt[String]("bucket") action { (x, c) =>
      c &|-> (MainArgs.s3Args ^|-> S3Args.bucket) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --bucket must be non-empty")
    } text s"bucket is a non-empty String property"

    opt[String]("prefix") action { (x, c) =>
      c &|-> (MainArgs.s3Args ^|-> S3Args.prefix) set x
    } validate { x =>
      if (x.nonEmpty) success else failure(s"Option --prefix must be non-empty")
    } text s"prefix is a non-empty String property"

    help("help") text "prints this usage text"
  }

  def parse(args: Array[String]) = parser.parse(args, MainArgs())
}
