package models.nonresident

import play.api.libs.json.Json

/**
  * Created by emma on 16/11/16.
  */
case class MarketValueGaveAwayModel (marketValueGaveAwayAmt: BigDecimal)

object MarketValueGaveAwayModel {
  implicit val format = Json.format[MarketValueGaveAwayModel]
}