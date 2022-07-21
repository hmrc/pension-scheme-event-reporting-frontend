#!/bin/bash

echo ""
echo "Applying migration TestCheckBox"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /testCheckBox                        controllers.TestCheckBoxController.onPageLoad(waypoints: Waypoints ?= EmptyWaypoints)" >> ../conf/app.routes
echo "POST       /testCheckBox                        controllers.TestCheckBoxController.onSubmit(waypoints: Waypoints ?= EmptyWaypoints)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "testCheckBox.title = testCheckBox" >> ../conf/messages.en
echo "testCheckBox.heading = testCheckBox" >> ../conf/messages.en
echo "testCheckBox.option1 = Option 1" >> ../conf/messages.en
echo "testCheckBox.option2 = Option 2" >> ../conf/messages.en
echo "testCheckBox.checkYourAnswersLabel = testCheckBox" >> ../conf/messages.en
echo "testCheckBox.error.required = Select testCheckBox" >> ../conf/messages.en
echo "testCheckBox.change.hidden = TestCheckBox" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTestCheckBoxUserAnswersEntry: Arbitrary[(TestCheckBoxPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TestCheckBoxPage.type]";\
    print "        value <- arbitrary[TestCheckBox].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTestCheckBoxPage: Arbitrary[TestCheckBoxPage.type] =";\
    print "    Arbitrary(TestCheckBoxPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTestCheckBox: Arbitrary[TestCheckBox] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(TestCheckBox.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TestCheckBoxPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration TestCheckBox completed"
