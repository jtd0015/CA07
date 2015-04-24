package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.stock.Stock;
import pkg.order.BuyOrder;
import pkg.order.OrderBook;
import pkg.order.SellOrder;

public class Trader {
	// Name of the trader
	String name;
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> stocksOwned;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.stocksOwned = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}
	/**
          * Allows trader to buy straight from bank.
          *
          * @param Market, String, int
          */
	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		Stock a = m.getStockForSymbol(symbol);
		if (volume*(a.getPrice()) > this.cashInHand){
			throw new StockMarketExpection("Trader doesn't have enough money.");
		}
		else {
			BuyOrder e = new BuyOrder(symbol, volume, a.getPrice(), this);
			this.stocksOwned.add(e);
			cashInHand = cashInHand - volume*a.getPrice();
		}
		
	}
	
  	/**
          * Adds a new order to orderList
          *
          * @param Market, String, int, double, OrderType
          */
	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		
		if (orderType == OrderType.BUY){
			Stock a = m.getStockForSymbol(symbol);
			if (volume*(a.getPrice()) > this.cashInHand){
				throw new StockMarketExpection("Trader doesn't have enough money.");
			}
			else {
				for (Order p:ordersPlaced){
					if (p.getStockSymbol() == symbol) { 
						throw new StockMarketExpection("Trader already has order for stock " + symbol);
					}
					else{
						BuyOrder e = new BuyOrder(symbol, volume, a.getPrice(), this);
						this.ordersPlaced.add(e);
						m.addOrder(e);
					}
				}
			}	
		}
		else if (orderType == OrderType.SELL){
			int i = 0;
			Stock a = m.getStockForSymbol(symbol);
			for(Order p:stocksOwned){
				if (p.getStockSymbol() == symbol && p.getSize() > 0){
					i = 1;
					if (volume < p.getSize()){
						throw new StockMarketExpection("Trader doesn't have enough stocks.");
					}
					else {
						SellOrder e = new SellOrder(symbol, volume, a.getPrice(), this);
						this.ordersPlaced.add(e);
						m.addOrder(e);
					}
				}
			}
			if (i == 0){
				throw new StockMarketExpection("This stock is not owned by trader");
			}
		}
		else {
			
		}
	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Similar to the other method, except the order is a market order
		if (orderType == OrderType.BUY){
			Stock a = m.getStockForSymbol(symbol);
			if (volume*(a.getPrice()) > this.cashInHand){
				throw new StockMarketExpection("Trader doesn't have enough money.");
			}
			else {
				BuyOrder e = new BuyOrder(symbol, volume, true, this);
				if (m.orderBook.orderInBook() == 0) {
					throw new StockMarketExpection("Trader has already placed order for this.");
				}
				else {
					this.ordersPlaced.add(e);
					m.addOrder(e);
				}
			}	
		}
		else if (orderType == OrderType.SELL){
			Stock a = m.getStockForSymbol(symbol);
			SellOrder e = new SellOrder(symbol, volume, true, this);
			this.ordersPlaced.add(e);
			m.addOrder(e);
		}
	
	}

  	/**
          * Allows the system to update when trade is performed.
          *
          * @param Order, double
          */
	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		if (o.isOrderType() == 1){
			ordersPlaced.remove(o);
			cashInHand = cashInHand + o.getSize() * o.getPrice();
			for (Order p:stocksOwned){
				if (p.getStockSymbol() == o.getStockSymbol()){
					p.setSize(p.getSize() - o.getSize());
					if (p.getSize() == 0){
						stocksOwned.remove(p);
					}
					
				}
			}
		}
		else if (o.isOrderType() == 2){
			ordersPlaced.remove(o);
			cashInHand = cashInHand - o.getSize() * o.getPrice();
			for (Order p:stocksOwned){
				if (p.getStockSymbol() == o.getStockSymbol()){
					p.setSize(p.getSize() + o.getSize());
				}
				else {
					stocksOwned.add(o);
				}
			}
		}
		else {
			throw new StockMarketExpection("No order type.");
			
		}
			
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : stocksOwned) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
