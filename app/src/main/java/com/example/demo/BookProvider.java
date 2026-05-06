package com.example.demo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * BookProvider 是一个 ContentProvider 实现类
 * 
 * ContentProvider 是 Android 四大组件之一，用于跨应用数据共享：
 * 
 * 【核心功能】
 * - 提供统一的数据访问接口
 * - 支持增删改查（CRUD）操作
 * - 通过 URI 标识数据
 * - 支持跨进程通信（IPC）
 * 
 * 【URI 匹配规则】
 * - content://com.example.demo.provider/books → BOOKS (查询所有书籍)
 * - content://com.example.demo.provider/books/1 → BOOK_ID (查询指定ID的书籍)
 * 
 * 【生命周期】
 * - onCreate(): Provider 创建时调用，初始化数据库
 */
public class BookProvider extends ContentProvider {

    /** ContentProvider 的 authority（唯一标识） */
    public static final String AUTHORITY = "com.example.demo.provider";
    
    /** 内容 URI */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

    /** URI 匹配码：查询所有书籍 */
    private static final int BOOKS = 1;
    
    /** URI 匹配码：查询指定 ID 的书籍 */
    private static final int BOOK_ID = 2;

    /** UriMatcher 用于匹配不同的 URI */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * 静态初始化：注册 URI 匹配规则
     */
    static {
        // 匹配 content://authority/books
        uriMatcher.addURI(AUTHORITY, "books", BOOKS);
        // 匹配 content://authority/books/id
        uriMatcher.addURI(AUTHORITY, "books/#", BOOK_ID);
    }

    /** 数据库帮助类实例 */
    private BookDatabaseHelper dbHelper;

    /**
     * ContentProvider 创建时调用
     * 
     * @return true 表示初始化成功
     */
    @Override
    public boolean onCreate() {
        // 初始化数据库帮助类
        dbHelper = new BookDatabaseHelper(getContext());
        return true;
    }

    /**
     * 查询数据
     * 
     * @param uri 查询的 URI
     * @param projection 要返回的列（null 表示返回所有列）
     * @param selection WHERE 条件
     * @param selectionArgs WHERE 参数
     * @param sortOrder ORDER BY 排序
     * @return Cursor 对象（包含查询结果）
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                // 查询所有书籍
                cursor = db.query(BookDatabaseHelper.TABLE_BOOKS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // 查询指定 ID 的书籍
                String id = uri.getLastPathSegment();
                cursor = db.query(BookDatabaseHelper.TABLE_BOOKS, projection,
                        BookDatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id}, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 设置通知 URI，当数据变化时通知 ContentResolver
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    /**
     * 获取数据类型
     * 
     * @param uri 查询的 URI
     * @return MIME 类型字符串
     */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                // 多条记录的 MIME 类型
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".books";
            case BOOK_ID:
                // 单条记录的 MIME 类型
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".books";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * 插入数据
     * 
     * @param uri 插入的 URI
     * @param values 要插入的数据
     * @return 插入后记录的 URI（包含新记录的 ID）
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != BOOKS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 执行插入操作，返回新记录的 ID
        long id = db.insert(BookDatabaseHelper.TABLE_BOOKS, null, values);

        if (id > 0) {
            // 构建包含 ID 的 URI
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, id);
            // 通知数据变化
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(resultUri, null);
            }
            return resultUri;
        }

        return null;
    }

    /**
     * 删除数据
     * 
     * @param uri 删除的 URI
     * @param selection WHERE 条件
     * @param selectionArgs WHERE 参数
     * @return 删除的记录数
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                // 删除所有匹配条件的记录
                rowsDeleted = db.delete(BookDatabaseHelper.TABLE_BOOKS, selection, selectionArgs);
                break;
            case BOOK_ID:
                // 删除指定 ID 的记录
                String id = uri.getLastPathSegment();
                rowsDeleted = db.delete(BookDatabaseHelper.TABLE_BOOKS,
                        BookDatabaseHelper.COLUMN_ID + "=?", new String[]{id});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 通知数据变化
        if (rowsDeleted > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    /**
     * 更新数据
     * 
     * @param uri 更新的 URI
     * @param values 要更新的数据
     * @param selection WHERE 条件
     * @param selectionArgs WHERE 参数
     * @return 更新的记录数
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                // 更新所有匹配条件的记录
                rowsUpdated = db.update(BookDatabaseHelper.TABLE_BOOKS, values,
                        selection, selectionArgs);
                break;
            case BOOK_ID:
                // 更新指定 ID 的记录
                String id = uri.getLastPathSegment();
                rowsUpdated = db.update(BookDatabaseHelper.TABLE_BOOKS, values,
                        BookDatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // 通知数据变化
        if (rowsUpdated > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
