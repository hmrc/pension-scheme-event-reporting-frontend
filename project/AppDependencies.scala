import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.4.0"
  private val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% s"play-frontend-hmrc-$playVersion"             % "12.23.0",
    "uk.gov.hmrc"                   %% s"play-conditional-form-mapping-$playVersion"  % "3.4.0",
    "uk.gov.hmrc"                   %% s"bootstrap-frontend-$playVersion"             % bootstrapVersion,
    "uk.gov.hmrc"                   %% s"domain-$playVersion"                         % "13.0.0",
    "uk.gov.hmrc"                   %% s"play-partials-$playVersion"                  % "10.2.0",
    "org.typelevel"                 %% "cats-core"                                    % "2.13.0",
    "com.univocity"                 %  "univocity-parsers"                            % "2.9.1",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"                         % "2.20.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"   % bootstrapVersion  % Test,
    "org.scalatestplus"       %% "scalacheck-1-17"                % "3.2.18.0"        % Test,
    "io.github.wolfendale"    %% "scalacheck-gen-regexp"          % "1.1.0"           % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
