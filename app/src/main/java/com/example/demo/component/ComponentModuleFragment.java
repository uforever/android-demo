package com.example.demo.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.demo.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * ComponentModuleFragment - 组件模块主Fragment
 * 
 * 作为四大组件的容器，使用ViewPager2 + TabLayout实现组件间切换
 * 包含：Activity、Service、Broadcast、Provider四个子模块
 */
public class ComponentModuleFragment extends Fragment {

    /** ViewPager2用于页面切换 */
    private ViewPager2 viewPager;
    
    /** TabLayout用于标签切换 */
    private TabLayout tabLayout;

    /**
     * 创建Fragment视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_component_module, container, false);
        
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        
        // 设置ViewPager2适配器
        setupViewPager();
        
        return view;
    }

    /**
     * 设置ViewPager2适配器
     * 使用FragmentStateAdapter替代已弃用的FragmentPagerAdapter
     */
    private void setupViewPager() {
        // 创建并使用ComponentPagerAdapter
        ComponentPagerAdapter adapter = new ComponentPagerAdapter(requireActivity());
        
        // 设置适配器
        viewPager.setAdapter(adapter);
        
        // 将TabLayout与ViewPager2关联
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(adapter.getTitle(position))).attach();
    }

    /**
     * FragmentStateAdapter子类，用于管理ViewPager2的Fragment
     * 替代已弃用的FragmentPagerAdapter
     */
    private static class ComponentPagerAdapter extends FragmentStateAdapter {
        
        private final String[] titles = {"Activity", "Service", "Broadcast", "Provider"};
        private final Fragment[] fragments = {
                new ActivityFragment(),
                new ServiceFragment(),
                new BroadcastFragment(),
                new ProviderFragment()
        };
        
        public ComponentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }
        
        @Override
        public int getItemCount() {
            return fragments.length;
        }
        
        /**
         * 获取指定位置的标签标题
         */
        public String getTitle(int position) {
            return titles[position];
        }
    }
}
