import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.11.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"             % "11.12.0",
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"  % "3.2.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc"                   %% "domain-play-30"                         % "10.0.0",
    "uk.gov.hmrc"                   %% "play-partials-play-30"                  % "10.0.0",
    "org.typelevel"                 %% "cats-core"                              % "2.12.0",
    "com.univocity"                 %  "univocity-parsers"                      % "2.9.1",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.18.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "org.scalatestplus"       %% "mockito-4-6"             % "3.2.15.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"      % "7.0.1",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.64.8",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"   % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
