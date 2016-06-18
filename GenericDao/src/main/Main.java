package main;

import com.amazonaws.services.lambda.runtime.Context;
import dao.MessagesDao;
import dao.UserDao;
import engine.sqlpager.MessagessSQLPager;
import engine.sqlpager.PaginationParams;
import model.Message;
import model.User;
import utils.TestContext;
import utils.Tuple;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by nativ on 6/18/16.
 */
public class Main {

    private static final int MESSAGES_COUNT = 100;
    private static final int MIDDLE_MESSAGE_ID = 50;

    private static MessagesDao mMessageDao;
    private static UserDao mUserDao;
    private static TestContext mContext;

    public static void main(String[] arg) throws Exception {
        mContext = new TestContext();
        mUserDao = new UserDao();
        mMessageDao = new MessagesDao();

        long userId = insertUser();
        insertMessages(userId);


        getFirstMessagesPage();
        getNextMessagesPageByLowestId();
        getNextMessagesPageByPageIndex();
        getOnlyNewMessagesPage();
    }


    /**
     * @return - json of the next page of results, paginating by entity's id. e.g from 50 to 70 id.
     * @throws Exception
     */
    public static String getNextMessagesPageByLowestId() throws Exception {
        MessagesDao messagesDao = new MessagesDao();
        PaginationParams paginationParams = new PaginationParams();
        paginationParams.setLoadMoreId(MIDDLE_MESSAGE_ID);
        MessagessSQLPager messagessSQLPager = new MessagessSQLPager(messagesDao::buildEntities, mContext);
        return messagessSQLPager.getResults(paginationParams, messagesDao, messagesDao.getTableName(), "id");
    }

    /**
     * @return - json of the next page of results, paginating by page index.
     * @throws Exception
     */
    public static String getNextMessagesPageByPageIndex() throws Exception {
        MessagesDao messagesDao = new MessagesDao();
        PaginationParams paginationParams = new PaginationParams();
        paginationParams.setPageIndex(1);
        MessagessSQLPager messagessSQLPager = new MessagessSQLPager(messagesDao::buildEntities, mContext);
        return messagessSQLPager.getResults(paginationParams, messagesDao, messagesDao.getTableName(), "id");
    }

    /**
     * @return -  json of the new items, starting from MIDDLE_MESSAGE_ID.
     * @throws Exception
     */
    public static String getOnlyNewMessagesPage() throws Exception {
        MessagesDao messagesDao = new MessagesDao();
        PaginationParams paginationParams = new PaginationParams();
        paginationParams.setUserHeighestVisibleId(MIDDLE_MESSAGE_ID);
        MessagessSQLPager messagessSQLPager = new MessagessSQLPager(messagesDao::buildEntities, mContext);
        return messagessSQLPager.getResults(paginationParams, messagesDao, messagesDao.getTableName(), "id");
    }

    /**
     * @return - json of the first page from messages table
     * @throws Exception
     */
    private static String getFirstMessagesPage() throws Exception {
        MessagesDao messagesDao = new MessagesDao();
        PaginationParams paginationParams = new PaginationParams();
        MessagessSQLPager messagessSQLPager = new MessagessSQLPager(messagesDao::buildEntities, mContext);
        return messagessSQLPager.getResults(paginationParams, messagesDao, messagesDao.getTableName(), "id");
    }

    private static void insertMessages(long userId) throws Exception {
        for (int i = 0; i < MESSAGES_COUNT; i++) {
            Message message = new Message(-1, userId, "content", 0, "imageUrl", 1800, System.currentTimeMillis());
            mMessageDao.insert(mContext, message);
        }
    }

    private static long insertUser() throws Exception {
        User user = new User(0, "first name", "last name", "location", "password", System.currentTimeMillis(), 1, "thumb");
        Tuple<Integer, PreparedStatement> t = mUserDao.insert(mContext, user);
        ResultSet rs = t._2.getGeneratedKeys();
        if (rs.next()) {
            long userId = rs.getLong(1);
            return userId;
        }

        return -1;
    }

}
