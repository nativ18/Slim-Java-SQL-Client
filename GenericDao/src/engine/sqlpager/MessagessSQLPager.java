package engine.sqlpager;

import java.util.ArrayList;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;
import dao.MessagesDao;
import dao.UserDao;
import model.Message;
import utils.CheckedParam;

public class MessagessSQLPager extends SQLPager<Message> {

    private static final String JOIN_SELECT_FIRST_PAGE = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? order by n.creation_date desc limit %s;", MessagesDao.TABLE_NAME, UserDao.TABLE_NAME);
    private static final String JOIN_SELECT_NEXT_PAGE = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? and n.creation_date<=? order by n.creation_date desc limit %s;", MessagesDao.TABLE_NAME, UserDao.TABLE_NAME);
    private static final String SELECT_ONLY_NEW_FOR_ID = String.format("select * from %s n join %s u on n.actor_id = u.id where n.owner_id=? and n.creation_date>? order by id desc limit %s;", MessagesDao.TABLE_NAME, UserDao.TABLE_NAME);

    public MessagessSQLPager(Function<CheckedParam, ArrayList<Message>> function,
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
