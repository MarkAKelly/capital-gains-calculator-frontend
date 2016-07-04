package views.resident

/**
  * Created by david on 04/07/16.
  */
class CurrentIncomeViewSpec {

  "Allowable Losses view" should {

    lazy val view = views.allowableLosses(allowableLossesForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }
  }
}
