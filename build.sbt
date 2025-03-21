import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

import scala.sys.process.*

lazy val appName: String = "pension-scheme-event-reporting-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings))
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "models.enumeration.EventType",
      "models.enumeration.EventType._",
      "models.enumeration.AddressJourneyType",
      "models.enumeration.AddressJourneyType._",
      "pages.Waypoints",
      "pages.EmptyWaypoints",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Index",
      "models.Mode",
      "models.requests.RequiredSchemeDataRequest",
      "controllers.routes._",
      "viewmodels.govuk.all._",
      "pages.Waypoints"
    ),
    PlayKeys.playDefaultPort := 8216,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*TestController;.*LanguageSwitchController;.*viewmodels.govuk.*;.*components.*;" +
      "models.Mode.*;models.Enumerable.*;pages.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:src=target/.*:s",
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=html/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    resolvers ++= Seq(Resolver.jcenterRepo),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/app.js"
        ))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest, uglify),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("application.js"),
    // auto-run migrate script after g8Scaffold task
    g8Scaffold := {
      g8Scaffold.evaluated
      streams.value.log.info("Running migrate script")
      val scriptPath = baseDirectory.value.getCanonicalPath + "/migrate.sh"
      s"bash -c $scriptPath".!
    }
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings() ++ itSettings)
  .settings(libraryDependencies ++= AppDependencies.test)


lazy val itSettings = Seq(
  fork := true
)

addCommandAlias("runCoverageCheck", "clean;coverage;test;coverageReport")

Universal / javaOptions ++= Seq(
  "-J-Xms256m",
  "-J-Xmx256m")