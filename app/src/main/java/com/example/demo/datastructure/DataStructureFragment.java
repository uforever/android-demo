package com.example.demo.datastructure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.lsposed.lsparanoid.Obfuscate;

import com.example.demo.datastructure.graph.GraphFragment;
import com.example.demo.datastructure.hashmap.HashMapFragment;
import com.example.demo.datastructure.heap.HeapFragment;
import com.example.demo.datastructure.linkedlist.LinkedListFragment;
import com.example.demo.datastructure.stack.StackFragment;
import com.example.demo.datastructure.trie.TrieFragment;
import com.example.demo.databinding.FragmentDataStructureBinding;
import com.google.android.material.tabs.TabLayoutMediator;

@Obfuscate
public class DataStructureFragment extends Fragment {

    private FragmentDataStructureBinding binding;

    private static final String[] TITLES = {"链表", "栈", "堆", "哈希表", "字典树", "图"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDataStructureBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.viewPager.setAdapter(new DataStructurePagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TITLES[position])).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class DataStructurePagerAdapter extends FragmentStateAdapter {

        public DataStructurePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new StackFragment();
                case 2:
                    return new HeapFragment();
                case 3:
                    return new HashMapFragment();
                case 4:
                    return new TrieFragment();
                case 5:
                    return new GraphFragment();
                default:
                    return new LinkedListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TITLES.length;
        }
    }
}
