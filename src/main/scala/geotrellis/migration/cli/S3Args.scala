package geotrellis.migration.cli

import monocle.macros.Lenses
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken

@Lenses
case class S3Args(bucket: String = "", prefix: String = "")
