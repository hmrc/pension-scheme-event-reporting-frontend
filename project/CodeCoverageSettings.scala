import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {
  val excludedPackages: Seq[String] = Seq(
    ".*Reverse.*",
    ".*Routes.*",
    "view.*",
    ".*ErrorHandler.*",
    ".*\\$anon.*",
    ".*utils.*",
    ".*testOnly.*",
    ".*LanguageSwitchController.*",
    ".*pages.fileUpload.*",
    ".*helpers.fileUpload.*",
    ".*controllers.fileUpload.*",
    ".*pages.amend.*",
    ".*enumeration.*",
    ".*pages.event4.*",
    ".*pages.event5.*",
    ".*pages.event8.*",
    ".*pages.event8a.*",
    ".*WhatYouWillNeedPage.*",
    ".*UnauthorisedPaymentRecipientNamePage.*",
    ".*controllers.partials.*",
    ".*models.enumeration.binders.*",
    ".*Event7MembersSummary.*",
    ".*AFTQuarter.*",
    ".*AuthorisingIndividual.*",
    ".*AuthorisingPSA.*",
    ".*CommonQuarters.*",
    ".*FileUploadOutcomeResponse.*",
    ".*FileUploadOutcomeStatus.*",
    ".*Mode.*",
    ".*PsaDetails.*",
    ".*PsaName.*",
    ".*PsaSchemeDetails.*",
    ".*PspDetails.*",
    ".*PspSchemeDetails.*",
    ".*Q1.*",
    ".*Q3.*",
    ".*Q4.*",
    ".*SendEmailRequest.*",
    ".*UploadId.*",
  )

  def apply(): Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages:= excludedPackages.mkString(",")
  )
}
