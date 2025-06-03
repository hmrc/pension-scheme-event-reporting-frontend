import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

import scala.sys.process.*

lazy val appName: String = "pension-scheme-event-reporting-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.6.4"

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
    CodeCoverageSettings(),
    scalacOptions ++= Seq(
      "-feature",
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent", // Suppress warnings from routes files
      "-Wconf:src=twirl/.*:silent",  // Suppress warnings from twirl files
      "-Wconf:src=target/.*:silent", // Suppress warnings from target files
      "-Wconf:msg=Flag.*repeatedly:silent", // Suppress repeated flag warnings
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/app.js"
        ))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat),
    // auto-run migrate script after g8Scaffold task
    g8Scaffold := {
      g8Scaffold.evaluated
      streams.value.log.info("Running migrate script")
      val scriptPath = baseDirectory.value.getCanonicalPath + "/migrate.sh"
      s"bash -c $scriptPath".!
    }
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
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