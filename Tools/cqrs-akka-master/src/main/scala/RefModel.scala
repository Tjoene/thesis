package net.debasishg.domain.trade
package model

trait RefModel extends Serializable {
  type Instrument = String
  type Account = String
  type NetAmount = BigDecimal
  type Customer = String
  type Broker = String

  sealed trait Market 
  case object HongKong extends Market
  case object Singapore extends Market
  case object NewYork extends Market
  case object Tokyo extends Market
  case object Other extends Market

  def makeMarket(m: String) = m match {
    case "HongKong" => HongKong
    case "Singapore" => Singapore
    case "NewYork" => NewYork
    case "Tokyo" => Tokyo
    case _ => Other
  }
}
