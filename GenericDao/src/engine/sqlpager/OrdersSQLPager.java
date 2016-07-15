package engine.sqlpager;

import java.util.ArrayList;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;

import datamanager.dao.CustomerDao;
import datamanager.dao.OrdersDao;
import datamanager.model.Order;
import utils.CheckedParam;

public class OrdersSQLPager extends SQLPager<Order> {

    private static final String JOIN_SELECT_FIRST_PAGE = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? order by n.creation_date desc limit %s;", OrdersDao.TABLE_NAME, CustomerDao.TABLE_NAME);
    private static final String JOIN_SELECT_NEXT_PAGE = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? and n.creation_date<=? order by n.creation_date desc limit %s;", OrdersDao.TABLE_NAME, CustomerDao.TABLE_NAME);
    private static final String SELECT_ONLY_NEW_FOR_ID = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? and n.creation_date>? order by id desc limit %s;", OrdersDao.TABLE_NAME, CustomerDao.TABLE_NAME);

    public OrdersSQLPager(Function<CheckedParam, ArrayList<Order>> function,
                             Context context) {
        super(function, context);
    }

    public String getFirstPageQuery(String tableName, String fkName, long entityId) {
        return String.format(JOIN_SELECT_FIRST_PAGE, mPageSize);
    }

    public String getNextPageQuery(String tableName, String... fkName) {
        return String.format(JOIN_SELECT_NEXT_PAGE, mPageSize);
    }

    public String getNewResultsForIdQuery(String tableName, String... fkName) {
        return String.format(SELECT_ONLY_NEW_FOR_ID, mPageSize);
    }

}
