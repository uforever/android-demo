package com.example.demo.network;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.demo.databinding.FragmentNetworkBinding;
import com.example.demo.network.grpc.GrpcFragment;
import com.example.demo.network.http.HttpFragment;
import com.example.demo.network.websocket.WebSocketFragment;
import com.google.android.material.tabs.TabLayout;

public class NetworkFragment extends Fragment {

    private FragmentNetworkBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNetworkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.viewPager.setAdapter(new NetworkPagerAdapter(getChildFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class NetworkPagerAdapter extends FragmentPagerAdapter {

        private static final String[] TITLES = {"HTTP(s)", "WebSocket", "gRPC"};

        public NetworkPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HttpFragment();
                case 1:
                    return new WebSocketFragment();
                case 2:
                    return new GrpcFragment();
                default:
                    return new HttpFragment();
            }
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
