package controllers$if(!package.empty)$.$package$$endif$

import connectors.UserAnswersCacheConnector
import models.enumeration.EventType
import controllers.actions._
import forms$if(!package.empty)$.$package$$endif$.$className$FormProvider
import javax.inject.Inject
$if(package.empty)$
import pages.{$className$Page, Waypoints}
$else$
import pages.Waypoints
import pages.$package$.$className$Page
$endif$
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html$if(!package.empty)$.$package$$endif$.$className$View

import scala.concurrent.{ExecutionContext, Future}

class $className;format="cap"$Controller @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: $className$FormProvider,
                                        view: $className$View
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get($className$Page).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException($className$Page, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect($className$Page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}
