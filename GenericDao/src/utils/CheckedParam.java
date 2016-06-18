package utils;

import java.sql.ResultSet;

public class CheckedParam {
	ResultSet resultSet;
	boolean mFetchAnswers;
	int mPageSize;

	public CheckedParam(ResultSet resultSet, boolean mFetchAnswers, int mPageSize) {
		super();
		this.resultSet = resultSet;
		this.mFetchAnswers = mFetchAnswers;
		this.mPageSize = mPageSize;
	}

	public CheckedParam(ResultSet resultSet, int mPageSize) {
		super();
		this.resultSet = resultSet;
		this.mPageSize = mPageSize;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public boolean isFetchAnswers() {
		return mFetchAnswers;
	}

	public void setmFetchAnswers(boolean mFetchAnswers) {
		this.mFetchAnswers = mFetchAnswers;
	}

	public int getmPageSize() {
		return mPageSize;
	}

	public void setmPageSize(int mPageSize) {
		this.mPageSize = mPageSize;
	}
	
	

}