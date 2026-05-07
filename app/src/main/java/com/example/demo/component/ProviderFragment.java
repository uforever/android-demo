package com.example.demo.component;

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

public class ProviderFragment extends Fragment {

    private static final String TAG = "ProviderFragment";
    public static final String AUTHORITY = "com.example.demo.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

    private FragmentProviderBinding binding;
    private final Random random = new Random();
    private final String[] bookNames = {"《Android开发艺术》", "《第一行代码》", "《Effective Java》", "《算法导论》", "《深入理解JVM》"};
    private final String[] authors = {"张三", "李四", "王五", "赵六", "钱七"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnAddBook.setOnClickListener(v -> addRandomBook());
        binding.btnQueryBooks.setOnClickListener(v -> queryAllBooks());
    }

    private void addRandomBook() {
        String bookName = bookNames[random.nextInt(bookNames.length)];
        String author = authors[random.nextInt(authors.length)];

        ContentValues values = new ContentValues();
        values.put("name", bookName);
        values.put("author", author);

        Uri uri = requireContext().getContentResolver().insert(CONTENT_URI, values);
        if (uri != null) {
            Log.d(TAG, "添加书籍成功: " + bookName + " - " + author);
            queryAllBooks();
        }
    }

    private void queryAllBooks() {
        List<String> bookList = new ArrayList<>();
        try (Cursor cursor = requireContext().getContentResolver().query(CONTENT_URI, null, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
                    bookList.add(name + " - " + author);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "查询书籍失败: " + e.getMessage());
        }
        
        displayBooks(bookList);
    }

    private void displayBooks(List<String> books) {
        binding.llBooksContainer.removeAllViews();

        if (books.isEmpty()) {
            binding.tvBooksEmpty.setVisibility(View.VISIBLE);
            binding.llBooksContainer.setVisibility(View.GONE);
        } else {
            binding.tvBooksEmpty.setVisibility(View.GONE);
            binding.llBooksContainer.setVisibility(View.VISIBLE);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
