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
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		// Buy stock straight from the bank
		// Need not place the stock in the order list
		// Add it straight to the user's position
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// Adjust cash possessed since the trader spent money to purchase a
		// stock.
		Stock a = m.getStockForSymbol(symbol);
		if (volume*(a.getPrice()) > this.cashInHand){
			throw new StockMarketExpection("Trader doesn't have enough money.");
		}
		else {
			BuyOrder e = new BuyOrder(symbol, volume, a.getPrice(), this);
			this.position.add(e);
			cashInHand = cashInHand - volume*a.getPrice();
		}
		
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Place a new order and add to the orderlist
		// Also enter the order into the orderbook of the market.
		// Note that no trade has been made yet. The order is in suspension
		// until a trade is triggered.
		//
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// A trader cannot place two orders for the same stock, throw an
		// exception if there are multiple orders for the same stock.
		// Also a person cannot place a sell order for a stock that he does not
		// own. Or he cannot sell more stocks than he possesses. Throw an
		// exception in these cases.
		
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
			for(Order p:position){
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
			//if (this.position.contains() {
				//throw new StockMarketExpection("Trader doesn't have enough money.");
			//}
			//else {
				SellOrder e = new SellOrder(symbol, volume, true, this);
				this.ordersPlaced.add(e);
				m.addOrder(e);
				
				//this.position.remove(e);
				//cashInHand = cashInHand - volume*a.getPrice();
			//}
		}
	
	}

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
			for (Order p:position){
				if (p.getStockSymbol() == o.getStockSymbol()){
					p.setSize(p.getSize() - o.getSize());
					if (p.getSize() == 0){
						position.remove(p);
					}
					
				}
			}
		}
		else if (o.isOrderType() == 2){
			ordersPlaced.remove(o);
			cashInHand = cashInHand - o.getSize() * o.getPrice();
			for (Order p:position){
				if (p.getStockSymbol() == o.getStockSymbol()){
					p.setSize(p.getSize() + o.getSize());
				}
				else {
					position.add(o);
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
		for (Order o : position) {
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
