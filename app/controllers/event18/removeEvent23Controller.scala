package controllers.event18

import connectors.UserAnswersCacheConnector
import models.enumeration.EventType
import controllers.actions._
import forms.event18.removeEvent23FormProvider
import javax.inject.Inject
import pages.Waypoints
import pages.event18.removeEvent23Page
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event18.removeEvent23View

import scala.concurrent.{ExecutionContext, Future}

class RemoveEvent23Controller @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: removeEvent23FormProvider,
                                        view: removeEvent23View
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(removeEvent23Page).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException(removeEvent23Page, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(removeEvent23Page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}
