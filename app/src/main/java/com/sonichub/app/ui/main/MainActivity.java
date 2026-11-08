package com.sonichub.app.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.sonichub.app.R;
import com.sonichub.app.databinding.ActivityMainBinding;
import com.sonichub.app.ui.create.CreateActivity;
import com.sonichub.app.ui.discover.DiscoverFragment;
import com.sonichub.app.ui.messages.MessagesFragment;
import com.sonichub.app.ui.plaza.PlazaFragment;
import com.sonichub.app.ui.profile.ProfileFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private int currentId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateActivity.class));
                return false;
            }
            showFragment(id);
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_discover);
        } else {
            currentId = 0;
            showFragment(R.id.nav_discover);
        }
    }

    private void showFragment(int id) {
        if (id == currentId && fragments.containsKey(id)) return;
        Fragment target = fragments.get(id);
        if (target == null) {
            if (id == R.id.nav_plaza) {
                target = new PlazaFragment();
            } else if (id == R.id.nav_messages) {
                target = new MessagesFragment();
            } else if (id == R.id.nav_profile) {
                target = new ProfileFragment();
            } else {
                target = new DiscoverFragment();
            }
            fragments.put(id, target);
        }
        androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : fragments.values()) {
            if (fragment != target && fragment.isAdded()) tx.hide(fragment);
        }
        if (!target.isAdded()) {
            tx.add(R.id.fragmentContainer, target, String.valueOf(id));
        } else {
            tx.show(target);
        }
        tx.commit();
        currentId = id;
    }
}
