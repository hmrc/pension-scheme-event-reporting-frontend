#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                        controllers$if(!package.empty)$.$package$$endif$.$className$Controller.onPageLoad(waypoints: Waypoints ?= EmptyWaypoints)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$                        controllers$if(!package.empty)$.$package$$endif$.$className$Controller.onSubmit(waypoints: Waypoints ?= EmptyWaypoints)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$UserAnswersEntry: Arbitrary[(pages$if(!package.empty)$.$package$$endif$.$className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[pages$if(!package.empty)$.$package$$endif$.$className$Page.type]";\
    print "        value <- arbitrary[models$if(!package.empty)$.$package$$endif$.$className$].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$Page: Arbitrary[$if(!package.empty)$$package$.$endif$$className$Page.type] =";\
    print "    Arbitrary($if(!package.empty)$$package$.$endif$$className$Page)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$: Arbitrary[$if(!package.empty)$$package$.$endif$$className$] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf($if(!package.empty)$$package$.$endif$$className$.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[($if(!package.empty)$$package$.$endif$$className$Page.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration $className;format="snake"$ completed"
