package com.example.demo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * BookDatabaseHelper 是一个 SQLite 数据库帮助类
 * 
 * SQLiteOpenHelper 提供了数据库创建和版本管理的便捷方法：
 * - onCreate(): 数据库第一次创建时调用
 * - onUpgrade(): 数据库版本升级时调用
 * 
 * 本类管理图书信息数据库，包含一张 books 表
 */
@Obfuscate
public class BookDatabaseHelper extends SQLiteOpenHelper {

    /** 数据库名称 */
    private static final String DATABASE_NAME = "books.db";
    
    /** 数据库版本 */
    private static final int DATABASE_VERSION = 1;

    /** 表名 */
    public static final String TABLE_BOOKS = "books";
    
    /** 列名：ID（主键） */
    public static final String COLUMN_ID = "_id";
    
    /** 列名：书名 */
    public static final String COLUMN_NAME = "name";
    
    /** 列名：作者 */
    public static final String COLUMN_AUTHOR = "author";

    /** 创建 books 表的 SQL 语句 */
    private static final String SQL_CREATE_BOOKS_TABLE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_AUTHOR + " TEXT NOT NULL)";

    /**
     * 构造函数
     * 
     * @param context Context 对象
     */
    public BookDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 数据库第一次创建时调用
     * 
     * @param db SQLiteDatabase 对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行建表语句
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * 数据库版本升级时调用
     * 
     * @param db SQLiteDatabase 对象
     * @param oldVersion 旧版本号
     * @param newVersions 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersions) {
        // 删除旧表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        // 重新创建表
        onCreate(db);
    }
}
