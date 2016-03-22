package geotrellis.migration.cli

import monocle.macros.Lenses

@Lenses
case class FileArgs(rootPath: String = "")
