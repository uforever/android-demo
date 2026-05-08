package com.example.demo.network;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.demo.databinding.FragmentNetworkBinding;
import com.example.demo.network.grpc.GrpcFragment;
import com.example.demo.network.http.HttpFragment;
import com.example.demo.network.websocket.WebSocketFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class NetworkFragment extends Fragment {

    private FragmentNetworkBinding binding;

    private static final String[] TITLES = {"HTTP(s)", "WebSocket", "gRPC"};

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

        binding.viewPager.setAdapter(new NetworkPagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(TITLES[position]);
                    }
                }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class NetworkPagerAdapter extends FragmentStateAdapter {

        public NetworkPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new WebSocketFragment();
                case 2:
                    return new GrpcFragment();
                default:
                    return new HttpFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TITLES.length;
        }
    }
}
