import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with AccessibilityMatchers {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure()
      .build()
  }

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/foo")

  implicit lazy val messages: Messages = {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    messagesApi.preferred(fakeRequest)
  }

  "the check your answers page" must {
    val checkYourAnswersPage = app.injector.instanceOf[CheckYourAnswersView]
    val content = checkYourAnswersPage(SummaryList())

    "pass accessibility test" in {
      content.toString() must passAccessibilityChecks
    }
  }

}
