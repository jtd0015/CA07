package pkg.order;

import pkg.exception.StockMarketExpection;
import pkg.trader.Trader;

public class BuyOrder extends Order {

	public BuyOrder(String stockSymbol, int size, double price, Trader trader) {
		// TODO:
		// Create a new buy order
		super();
		this.stockSymbol = stockSymbol;
		this.size = size;
		this.price = price;
		this.trader = trader;
	}

	public BuyOrder(String stockSymbol, int size, boolean isMarketOrder,
			Trader trader) throws StockMarketExpection {
		// TODO:
		// Create a new buy order which is a market order
		// Set the price to 0.0, Set isMarketOrder attribute to true
		//
		// If this is called with isMarketOrder == false, throw an exception
		// that an order has been placed without a valid price.
		if (isMarketOrder != true){
			throw new StockMarketExpection("Not a market order");
		}
		else {
			this.stockSymbol = stockSymbol;
			this.size = size;
			this.price = 0.0;
			this.trader = trader;
		}
		
		
		/*
		BuyOrder buyOrder = new addStock();
		if (order.isMarketOrder() == false) {
			throw new StockMarketExpection("Order has been placed without a valid price");
			
			set.isMarketOrder = true;
			double price = 0.0;
			market.addStock();
		}
		*/	
	}

	public void printOrder() {
		System.out.println("Stock: " + stockSymbol + " $" + price + " x "
				+ size + " (Buy)");
	}
	
	public int isOrderType(){
		return 2;
	}
}
