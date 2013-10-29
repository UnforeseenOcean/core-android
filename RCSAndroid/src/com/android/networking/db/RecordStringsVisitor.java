package com.android.networking.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

/**
 * Visitor that gets a record call for each record in the table
 * 
 * @author zeno
 * 
 */
public class RecordStringsVisitor extends RecordVisitor {

	// public abstract void record(int rpos, String[] record);
	List<String[]> records = new ArrayList<String[]>();

	public List<String[]> getRecords() {
		return records;
	}

	@Override
	public final void init() {
		records = new ArrayList<String[]>();
	}

	@Override
	public final void close() {

	}

	@Override
	public final long cursor(Cursor cursor) {

		String[] record = new String[getProjection().length];
		int rpos = 0;
		for (String column : getProjection()) {

			record[rpos] = cursor.getString(cursor.getColumnIndex(column));
			rpos++;
		}

		records.add(record);

		return rpos;
	}

}
