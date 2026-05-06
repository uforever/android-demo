package com.example.demo;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentProviderBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ProviderFragment 用于演示 ContentProvider（内容提供者）组件
 * 
 * ContentProvider 是 Android 四大组件之一，用于实现跨应用数据共享：
 * 
 * 【核心特点】
 * - 提供统一的数据访问接口
 * - 支持跨进程通信（IPC）
 * - 通过 URI 标识数据
 * - 支持增删改查（CRUD）操作
 * 
 * 【URI 格式】
 * content://authority/path/id
 * - authority: 唯一标识 ContentProvider（在 AndroidManifest.xml 中注册）
 * - path: 数据路径（如 books）
 * - id: 可选，指定具体记录
 * 
 * 【使用方式】
 * 通过 ContentResolver 访问 ContentProvider：
 * - getContentResolver().query()
 * - getContentResolver().insert()
 * - getContentResolver().update()
 * - getContentResolver().delete()
 */
public class ProviderFragment extends Fragment {

    /** 日志标签 */
    private static final String TAG = "ProviderFragment";
    
    /** ContentProvider 的 authority */
    public static final String AUTHORITY = "com.example.demo.provider";
    
    /** ContentProvider 的内容 URI */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

    /** ViewBinding 对象 */
    private FragmentProviderBinding binding;
    
    /** 随机数生成器，用于随机选择书籍 */
    private final Random random = new Random();
    
    /** 书名数组 */
    private final String[] bookNames = {"《Android开发艺术》", "《第一行代码》", "《Effective Java》", "《算法导论》", "《深入理解JVM》"};
    
    /** 作者数组 */
    private final String[] authors = {"张三", "李四", "王五", "赵六", "钱七"};

    /**
     * 创建 Fragment 视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后设置监听器
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    /**
     * 设置按钮点击事件监听器
     */
    private void setupListeners() {
        // 添加随机书籍
        binding.btnAddBook.setOnClickListener(v -> addRandomBook());
        
        // 查询所有书籍
        binding.btnQueryBooks.setOnClickListener(v -> queryAllBooks());
    }

    /**
     * 添加随机书籍到数据库
     * 
     * 使用 ContentResolver.insert() 向 ContentProvider 插入数据
     */
    private void addRandomBook() {
        // 随机选择书名和作者
        String bookName = bookNames[random.nextInt(bookNames.length)];
        String author = authors[random.nextInt(authors.length)];

        // 创建 ContentValues，存储要插入的数据
        ContentValues values = new ContentValues();
        values.put("name", bookName);
        values.put("author", author);

        // 通过 ContentResolver 插入数据
        Uri uri = requireContext().getContentResolver().insert(CONTENT_URI, values);
        if (uri != null) {
            Log.d(TAG, "添加书籍成功: " + bookName + " - " + author);
            // 插入成功后刷新列表
            queryAllBooks();
        }
    }

    /**
     * 查询所有书籍
     * 
     * 使用 ContentResolver.query() 从 ContentProvider 查询数据
     */
    private void queryAllBooks() {
        List<String> bookList = new ArrayList<>();
        Cursor cursor = null;
        try {
            // 通过 ContentResolver 查询数据
            cursor = requireContext().getContentResolver().query(
                    CONTENT_URI,  // 查询的 URI
                    null,         // 要返回的列（null 表示返回所有列）
                    null,         // WHERE 条件
                    null,         // WHERE 参数
                    null          // ORDER BY 排序
            );
            
            // 遍历 Cursor 获取数据
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
                    bookList.add(name + " - " + author);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "查询书籍失败: " + e.getMessage());
        } finally {
            // 必须关闭 Cursor，避免资源泄漏
            if (cursor != null) {
                cursor.close();
            }
        }
        
        // 显示查询结果
        displayBooks(bookList);
    }

    /**
     * 在界面上显示书籍列表
     * 
     * @param books 书籍列表
     */
    private void displayBooks(List<String> books) {
        // 清空容器中的所有视图
        binding.llBooksContainer.removeAllViews();

        if (books.isEmpty()) {
            // 没有数据时显示提示
            binding.tvBooksEmpty.setVisibility(View.VISIBLE);
            binding.llBooksContainer.setVisibility(View.GONE);
        } else {
            // 有数据时显示列表
            binding.tvBooksEmpty.setVisibility(View.GONE);
            binding.llBooksContainer.setVisibility(View.VISIBLE);

            // 动态创建 TextView 显示每本书
            for (String book : books) {
                TextView tv = new TextView(requireContext());
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setPadding(0, 8, 0, 8);
                tv.setText(book);
                tv.setTextSize(14);
                binding.llBooksContainer.addView(tv);
            }
        }
    }

    /**
     * Fragment 视图销毁时释放资源
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
