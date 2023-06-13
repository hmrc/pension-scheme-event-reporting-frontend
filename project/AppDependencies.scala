import sbt._

object AppDependencies {

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc"             % "6.4.0-play-28",
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping"  % "1.12.0-play-28",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-28"     % "7.13.0",
    "uk.gov.hmrc"                   %% "domain"                         % "8.1.0-play-28",
    "org.typelevel"                 %% "cats-core"                      % "2.9.0",
    "com.univocity"                 %  "univocity-parsers"               % "2.9.1",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"  % "7.13.0",
    "org.scalatest"           %% "scalatest"               % "3.2.15",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.15.0",
    "org.scalatestplus"       %% "mockito-4-6"             % "3.2.15.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"      % "5.1.0",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.64.0",
    "com.github.tomakehurst"  %  "wiremock-jre8"           % "2.35.0",
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"   % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
