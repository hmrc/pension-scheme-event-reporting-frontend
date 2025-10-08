import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.2.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"             % "12.17.0",
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc"                   %% "domain-play-30"                         % "13.0.0",
    "uk.gov.hmrc"                   %% "play-partials-play-30"                  % "10.2.0",
    "org.typelevel"                 %% "cats-core"                              % "2.13.0",
    "com.univocity"                 %  "univocity-parsers"                      % "2.9.1",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                   % "2.20.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0"       % Test,
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"   % "1.1.0"          % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
