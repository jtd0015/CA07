package pkg.order;

import java.util.*;

import pkg.stock.*;
import pkg.market.*;
import pkg.market.api.PriceSetter;
import pkg.exception.*;

public class OrderBook {
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		if (order.isOrderType() == 1){
			ArrayList<Order> tempList = sellOrders.get(order.getStockSymbol());
			tempList.add(order);
			sellOrders.put(order.getStockSymbol(), tempList);	
		}
		else if (order.isOrderType() == 2){
			ArrayList<Order> tempList = buyOrders.get(order.getStockSymbol());
			tempList.add(order);
			buyOrders.put(order.getStockSymbol(), tempList);
		}
	}

	public void trade() {
		HashMap<String, Stock> stockList = m.getStockList();
		Set<String> stk = stockList.keySet();
		String[] stocks = stk.toArray(new String[stk.size()]);
		for (String sym : stocks)
		{		
			// Separates orders for stock by order type
			ArrayList<Order> supply = sellOrders.get(sym);
			ArrayList<Order> demand = buyOrders.get(sym);

			// If no buy orders or sell orders for stock exist
			// then don't check for potential trades and move
			// to next stock
			if(supply != null && demand != null && supply.size() > 0 && demand.size() > 0)
			{
				sortByPrice(supply);
				sortByPrice(demand);
				Collections.reverse(supply);
				int fullSupplySize = 0;
				int fullDemandSize = 0;

				// 2. Find the market price which maximizes buy/sell orders

				// get min share amount at each market price
				HashMap<Double, Integer> minSizeSupply = new HashMap<Double, Integer>();
				HashMap<Double, Integer> minSizeDemand = new HashMap<Double, Integer>();
				ArrayList<Double> prices = new ArrayList<Double>(); // Represents all prices which have at least one order
				int s = 0;
				int d = 0;
				while (s < supply.size())
				{
					Order temp = supply.get(s);

					if (minSizeSupply.containsKey(temp.getPrice()))
						minSizeSupply.put(temp.getPrice(), minSizeSupply.get(temp.getPrice()) + temp.getSize());
					else
						minSizeSupply.put(temp.getPrice(), temp.getSize() + fullSupplySize);

					fullSupplySize += temp.getSize();

					if (!prices.contains(temp.getPrice()))
						prices.add(temp.getPrice());

					s++;
				}

				while (d < demand.size())
				{
					Order temp = demand.get(d);

					if (minSizeDemand.containsKey(temp.getPrice()))
						minSizeDemand.put(temp.getPrice(), minSizeDemand.get(temp.getPrice()) + temp.getSize());
					else
						minSizeDemand.put(temp.getPrice(), temp.getSize() + fullDemandSize);

					fullDemandSize += temp.getSize();

					if (!prices.contains(temp.getPrice()))
						prices.add(temp.getPrice());

					d++;
				}

				// find out market price with highest min share amount
				int i = 0;
				double marketPrice = 0.0;
				int curMinShare = -1;
				while (i < prices.size())
				{
					if (minSizeSupply.containsKey(prices.get(i)) && minSizeDemand.containsKey(prices.get(i)))
					{
						int tempSupply = minSizeSupply.get(prices.get(i));
						int tempDemand = minSizeDemand.get(prices.get(i));

						if (Math.min(tempSupply, tempDemand) > curMinShare)
						{
							curMinShare = Math.min(tempSupply, tempDemand);
							marketPrice = prices.get(i);
						}
					}
					i++;
				}

				// 3. Update the stocks price in the market using the PriceSetter.

				PriceSetter ps = new PriceSetter();
				ps.registerObserver(m.getMarketHistory());
				m.getMarketHistory().setSubject(ps);
				ps.setNewPrice(m, sym, marketPrice);

				// 4. Delegate to trader that the trade has been made, so that the
				// trader's orders can be placed to his possession (a trader's position
				// is the stocks he owns)
				if (marketPrice == 0.0){
					marketPrice = calculateAverageMarketPrice(prices);
				}	
				i = 0;
				while (i < supply.size())
				{
					Order potentialOrder = supply.get(i);
					if (potentialOrder.getPrice() <= marketPrice)
					{
						try
						{
							(potentialOrder.getTrader()).tradePerformed(potentialOrder, marketPrice);
						}
						catch (StockMarketExpection e) {
							e.printStackTrace();
						}
						// 5. Remove the traded orders from the order book
						supply.remove(i);
					}			
					i++;
				}

				sellOrders.put(sym, supply);

				i = 0;
				while (i < demand.size())
				{
					Order potentialOrder = demand.get(i);
					if (potentialOrder.getPrice() >= marketPrice)
					{
						try
						{
							(potentialOrder.getTrader()).tradePerformed(potentialOrder, marketPrice);
						}
						catch (StockMarketExpection e) {
							e.printStackTrace();
						}
						// 5. Remove the traded orders from the order book
						demand.remove(i);
					}			
					i++;
				}

				buyOrders.put(sym, demand);
			}
		}
	}
	
	public HashMap<String, ArrayList<Order>> getSellOrders()
	{
		return sellOrders;
	}

	public HashMap<String, ArrayList<Order>> getBuyOrders()
	{
		return buyOrders;
	}
	
	public int orderInBook(){
		return 0;
	}
	
	private void sortByPrice(ArrayList<Order> orders)
	{
		int i = 0;

		while (i < orders.size())
		{
			int j = 0;
			while (j < orders.size() - 1)
			{
				Order tempTarg = orders.get(j);
				Order tempNext = orders.get(j+1);
				if (tempTarg.getPrice() < tempNext.getPrice())
				{
					Collections.swap(orders, j, j+1);
				}
				j++;
			}
			i++;
		}
	}

	private double calculateAverageMarketPrice(ArrayList<Double> list)
	{
		double sum = 0;
		for (double d : list)
			sum += d;
		double avg = sum / list.size();	
		return avg;
	}
}
