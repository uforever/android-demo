package com.example.demo.crypto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.demo.crypto.encoding.EncodingFragment;
import com.example.demo.crypto.hash.HashFragment;
import com.example.demo.crypto.kdf.KdfFragment;
import com.example.demo.crypto.pqc.PqcFragment;
import com.example.demo.crypto.signature.SignatureFragment;
import com.example.demo.crypto.symmetric.SymmetricFragment;
import com.example.demo.crypto.asymmetric.AsymmetricFragment;
import com.example.demo.databinding.FragmentCryptoBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CryptoFragment extends Fragment {

    private FragmentCryptoBinding binding;

    private static final String[] TITLES = {"编码", "对称加密", "非对称加密", "哈希/HMAC", "签名", "密钥派生", "PQC"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CryptoProvider.init();
        binding = FragmentCryptoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.viewPager.setAdapter(new CryptoPagerAdapter(this));

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

    private static class CryptoPagerAdapter extends FragmentStateAdapter {

        public CryptoPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new SymmetricFragment();
                case 2:
                    return new AsymmetricFragment();
                case 3:
                    return new HashFragment();
                case 4:
                    return new SignatureFragment();
                case 5:
                    return new KdfFragment();
                case 6:
                    return new PqcFragment();
                default:
                    return new EncodingFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TITLES.length;
        }
    }
}
