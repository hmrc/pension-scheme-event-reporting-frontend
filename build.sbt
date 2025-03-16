import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import scala.sys.process._

lazy val appName: String = "pension-scheme-event-reporting-frontend"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(majorVersion := 0)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    scalaVersion := "2.13.12",
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
    ScoverageKeys.coverageMinimumStmtTotal := 75,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s"
    ),
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    update / evictionWarningOptions :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    resolvers ++= Seq(Resolver.jcenterRepo),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/autocomplete/location-autocomplete.min.js",
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

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-utils"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := true,
  fork := true
)

addCommandAlias("runCoverageCheck", "clean;coverage;test;coverageReport")

javaOptions in Universal ++= Seq(
  "-J-Xms256m",
  "-J-Xmx256m")