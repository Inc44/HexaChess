package im.bpu.hexachess.dao;

import java.sql.Connection;

public abstract class DAO<T> {
	protected Connection connect;
	public DAO() {
		open();
	}
	public void open() {
		try {
			connect = SingleConnection.getInstance();
		} catch (Exception exception) {
			System.err.println("DAO Open Error");
			exception.printStackTrace();
		}
	}
	public abstract T create(T obj);
	public abstract T update(T obj);
	public abstract void delete(T obj);
}