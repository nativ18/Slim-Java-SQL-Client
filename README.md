# GenericDao
GenericDao is a java library, writen in Java 8 and under APACHE licence.

# Introduction

The main goal is to provide automatic DAO abilities over POJO entities by simply extending 
the GenericDao superclass. 

# Features

*   Out of the box <b>SQL CRUD(create, read, update, delete)</b> by easily extending class GenericDao.

Example: 

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

*   <b>Pagination</b> - selecting pages of records using one line of code. 
    - by page index.
    - from record id.
    - by record id for which you would like to paginate only new records.

Example: 

    paginationParams.setPageIndex(int pageIndex); // quering by page index
    - or -
    paginationParams.setHeighestThen(int value);  // quering for heigher values then value
    - or -
    paginationParams.setIndexValue(int indexValue); // quering page for indexValue
    // "id" is the column on which this example paginates.
    OrderssSQLPager.getResults(paginationParams, OrdersDao, OrdersDao.getTableName(), "id")

*   Very easy for maintenance and extensions.

# Running it
Current MySql Connector is implemented using <b>JDBC</b>. You'll need to update the environment fields in class <b>EnvConstants with your sql server name, password and username</b>.

This library is a demo for Customers and Orders Model. 
In order to run it make sure that a corresponding Model exists(The fields' names can be taken from Order and Customer classes).   

Final note:

The library uses Annotations and Reflections and so do not recommend for Android applications usage since these patterns works 
slow on Android.
