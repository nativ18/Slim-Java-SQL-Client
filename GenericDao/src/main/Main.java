package main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import datamanager.dao.CustomerDao;
import datamanager.dao.OrdersDao;
import datamanager.model.Customer;
import datamanager.model.Order;
import engine.sqlpager.OrdersSQLPager;
import engine.sqlpager.PaginationParams;
import utils.TestContext;
import utils.Tuple;

/**
 * Created by nativ on 6/18/16.
 */
public class Main {

	private static final int PAGE_INDEX = 3;
	private static final int ORDERS_COUNT = 100;
	private static final int MIDDLE_ORDER_ID = 50;
	private static final int FROM_ID = 98;

	private static OrdersDao mOrdersDao;
	private static CustomerDao mCustomerDao;
	private static TestContext mContext;

	public static void main(String[] arg) throws Exception {
		mContext = new TestContext();
		mCustomerDao = new CustomerDao();
		mOrdersDao = new OrdersDao();

		long customerId = insertcustomer();

		/** inserts ORDERS_COUNT orders for customerId */
		insertOrders(customerId);

		/**
		 * returns first page of orders
		 */
		getFirstOrdersPage(customerId);

		/**
		 * returns next page of orders as if the lowest id is MIDDLE_ORDER_ID
		 */
		getNextOrdersPageByLowestId(customerId);

		/**
		 * returns the PAGE_INDEX page from orders table
		 */
		getNextOrdersPageByPageIndex(customerId, PAGE_INDEX);

		/**
		 * returns only new records. If the database was empty at the beginning
		 * of the test the ids should be in the range of 0-100. So for
		 * FROM_ID=98 this method returns json with orders 99 and 100.
		 */
		getOnlyNewOrdersPage(customerId, FROM_ID);
	}

	/**
	 * @param customerId
	 * @return - json of the next page of results, paginating by entity's id.
	 *         e.g from 50 to 70 id.
	 * @throws Exception
	 */
	public static String getNextOrdersPageByLowestId(long customerId) throws Exception {
		OrdersDao OrdersDao = new OrdersDao();
		PaginationParams paginationParams = new PaginationParams();
		paginationParams.setLoadMoreId(MIDDLE_ORDER_ID);
		paginationParams.setIndexValue(customerId);
		OrdersSQLPager OrderssSQLPager = new OrdersSQLPager(OrdersDao::buildEntities, mContext);
		return OrderssSQLPager.getResults(paginationParams, OrdersDao, OrdersDao.getTableName(), "id");
	}

	/**
	 * @param customerId
	 * @return - json of the next page of results, paginating by page index.
	 * @throws Exception
	 */
	public static String getNextOrdersPageByPageIndex(long customerId, int pageIndex) throws Exception {
		OrdersDao OrdersDao = new OrdersDao();
		PaginationParams paginationParams = new PaginationParams();
		paginationParams.setPageIndex(pageIndex);
		paginationParams.setIndexValue(customerId);
		OrdersSQLPager OrderssSQLPager = new OrdersSQLPager(OrdersDao::buildEntities, mContext);
		return OrderssSQLPager.getResults(paginationParams, OrdersDao, OrdersDao.getTableName(), "id");
	}

	/**
	 * @return - json of the new items, starting from MIDDLE_MESSAGE_ID.
	 * @throws Exception
	 */
	public static String getOnlyNewOrdersPage(long customerId, long fromId) throws Exception {
		OrdersDao OrdersDao = new OrdersDao();
		PaginationParams paginationParams = new PaginationParams();
		paginationParams.setHeighestThen(fromId);
		paginationParams.setIndexValue(customerId);
		OrdersSQLPager OrderssSQLPager = new OrdersSQLPager(OrdersDao::buildEntities, mContext);
		return OrderssSQLPager.getResults(paginationParams, OrdersDao, OrdersDao.getTableName(), "id");
	}

	/**
	 * @return - json of the first page from Orders table
	 * @throws Exception
	 */
	private static String getFirstOrdersPage(long customerId) throws Exception {
		OrdersDao OrdersDao = new OrdersDao();
		PaginationParams paginationParams = new PaginationParams();
		paginationParams.setIndexValue(customerId);
		OrdersSQLPager OrdersSQLPager = new OrdersSQLPager(OrdersDao::buildEntities, mContext);
		return OrdersSQLPager.getResults(paginationParams, OrdersDao, OrdersDao.getTableName(), "id");
	}

	private static void insertOrders(long customerId) throws Exception {
		for (int i = 0; i < ORDERS_COUNT; i++) {
			Order message = new Order(-1, customerId, "content", 0, "imageUrl", 1800, System.currentTimeMillis());
			mOrdersDao.insert(mContext, message);
		}
	}

	private static long insertcustomer() throws Exception {
		Customer customer = new Customer(0, "first name", "last name", "location", "password",
				System.currentTimeMillis(), 1, "thumb");
		Tuple<Integer, PreparedStatement> t = mCustomerDao.insert(mContext, customer);
		ResultSet rs = t._2.getGeneratedKeys();
		if (rs.next()) {
			long customerId = rs.getLong(1);
			return customerId;
		}

		return -1;
	}

}
