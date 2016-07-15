package datamanager.dao;

import datamanager.model.Order;
import engine.GenericDao;

public class OrdersDao extends GenericDao<Order> {

	public static final String TABLE_NAME = "orders";

	public OrdersDao() throws Exception {
		super(Order.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}
}
