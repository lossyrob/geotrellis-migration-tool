package geotrellis.migration.cli

import monocle.macros.Lenses
import org.apache.accumulo.core.client.security.tokens.{AuthenticationToken, PasswordToken}

@Lenses
case class AccumuloArgs(
  instanceName: String       = "",
  zookeeper: String          = "",
  user: String               = "",
  table: String              = "metadata",
  token: AuthenticationToken = new PasswordToken(""))
