package com.example.demo.component;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentActivityBinding;

import java.util.List;

public class ActivityFragment extends Fragment {

    private FragmentActivityBinding binding;

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                         @Nullable android.view.ViewGroup container,
                                         @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnStartSecond.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.demo.SecondActivity.class);
            startActivity(intent);
        });

        binding.btnDial.setOnClickListener(v -> {
            String phoneNumber = binding.etPhone.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(getContext(), "请输入电话号码", Toast.LENGTH_SHORT).show();
                return;
            }
            dialPhone(phoneNumber);
        });
    }

    private void dialPhone(String phoneNumber) {
        Uri uri = Uri.parse("tel:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        
        PackageManager packageManager = requireActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        
        if (!activities.isEmpty()) {
            startActivity(intent);
        } else {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
            List<ResolveInfo> viewActivities = packageManager.queryIntentActivities(viewIntent, 0);
            
            if (!viewActivities.isEmpty()) {
                startActivity(viewIntent);
            } else {
                Toast.makeText(getContext(), "没有找到拨号应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
