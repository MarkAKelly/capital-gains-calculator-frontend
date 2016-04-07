/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CapitalGainsCalculatorControllerSpec extends UnitSpec with WithFakeApplication {

  val s = "Action(parser=BodyParser(anyContent))"

  class fakeRequestTo(url : String) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
  }

  "CapitalGainsCalculatorController methods " should {

    //################### Customer Type tests #######################
    "return 200 from customer-type" in new fakeRequestTo("customer-type") {
      val result = CalculatorController.customerType(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML from customer-type" in new fakeRequestTo("customer-type"){
      val result = CalculatorController.customerType(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Disabled Trustee tests #######################
    "return 200 from disabled-trustee" in new fakeRequestTo("disabled-trustee") {
      val result = CalculatorController.disabledTrustee(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML from disabled-trustee" in new fakeRequestTo("disabled-trustee"){
      val result = CalculatorController.disabledTrustee(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Current Income tests #######################
    "be Action(parser=BodyParser(anyContent)) for currentIncome" in {
      val result = CalculatorController.currentIncome.toString()
      result shouldBe s
    }

    //################### Personal Allowance tests #######################
    "be Action(parser=BodyParser(anyContent)) for personalAllowance" in {
      val result = CalculatorController.personalAllowance.toString()
      result shouldBe s
    }

    //############## Other Properties tests ######################
    "return 200 from other-properties" in new fakeRequestTo("other-properties") {
      val result = CalculatorController.otherProperties(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML from other-properties" in new fakeRequestTo("other-properties"){
      val result = CalculatorController.otherProperties(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //############## Annual Exempt Amount tests ######################
    "return 200 for annual-exempt-amount" in new fakeRequestTo("annual-exempt-amount") {
      val result = CalculatorController.annualExemptAmount(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML for annual-exempt-amount" in new fakeRequestTo("annual-exempt-amount"){
      val result = CalculatorController.annualExemptAmount(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //############## Acquisition Value tests ######################
    "return 200 when sending a GET request `/calculate-your-capital-gains/acquisition-value`" in new fakeRequestTo("acquisition-value") {
      val result = CalculatorController.acquisitionValue(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML when sending a GET request `/calculate-your-capital-gains/acquisition-value`" in new fakeRequestTo("acquisition-value") {
      val result = CalculatorController.acquisitionValue(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Improvements tests #######################
    "return 200 when sending a GET to `/calculate-your-capital-gains/improvements`" in new fakeRequestTo("improvements") {
      val result = CalculatorController.improvements(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML when sending a GET to `/calculate-your-capital-gains/improvements`" in new fakeRequestTo("improvements"){
      val result = CalculatorController.improvements(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Disposal Date tests #######################
    "be Action(parser=BodyParser(anyContent)) for disposalDate" in {
      val result = CalculatorController.disposalDate.toString()
      result shouldBe s
    }

    //################### Disposal Value tests #######################
    "return 200 from disposal-value" in new fakeRequestTo("disposal-value") {
      val result = CalculatorController.disposalValue(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML from disposal-value" in new fakeRequestTo("disposal-value") {
      val result = CalculatorController.disposalValue(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Disposal Date tests #######################
    "return 200 from disposal-date" in new fakeRequestTo("disposal-date") {
      val result = CalculatorController.disposalDate(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML from disposal-date" in new fakeRequestTo("disposal-date"){
      val result = CalculatorController.disposalDate(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    //################### Acquisition Costs tests #######################
    "be Action(parser=BodyParser(anyContent)) for acquisitionCosts" in {
      val result = CalculatorController.acquisitionCosts.toString()
      result shouldBe s
    }

    //################### Disposal Costs tests #######################
    "be Action(parser=BodyParser(anyContent)) for disposalCosts" in {
      val result = CalculatorController.disposalCosts.toString()
      result shouldBe s
    }

    //################### Entrepreneurs Relief tests #######################
    "be Action(parser=BodyParser(anyContent)) for entrepreneursRelief" in {
      val result = CalculatorController.entrepreneursRelief.toString()
      result shouldBe s
    }

    //################### Allowable Losses tests #######################
    "be Action(parser=BodyParser(anyContent)) for allowableLosses" in {
      val result = CalculatorController.allowableLosses.toString()
      result shouldBe s
    }

    //################### Other Reliefs tests #######################
    "be Action(parser=BodyParser(anyContent)) for otherReliefs" in {
      val result = CalculatorController.otherReliefs.toString()
      result shouldBe s
    }
  }
}