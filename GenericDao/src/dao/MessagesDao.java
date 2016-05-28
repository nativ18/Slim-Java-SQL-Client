package dao;

import engine.GenericDao;
import model.Message;

public class MessagesDao extends GenericDao<Message> {

	public static final String TABLE_NAME = "messages";

	public MessagesDao() throws Exception {
		super(Message.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}
}
