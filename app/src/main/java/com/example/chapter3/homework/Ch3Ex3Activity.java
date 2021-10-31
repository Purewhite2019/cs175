package com.example.chapter3.homework;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 ViewPager 和 Fragment 做一个简单版的好友列表界面
 * 1. 使用 ViewPager 和 Fragment 做个可滑动界面
 * 2. 使用 TabLayout 添加 Tab 支持
 * 3. 对于好友列表 Fragment，使用 Lottie 实现 Loading 效果，在 5s 后展示实际的列表，要求这里的动效是淡入淡出
 */
public class Ch3Ex3Activity extends AppCompatActivity {
    static List<Fragment> fragmentList;
    static List<String> tabs;
    static ArrayList<ArrayList<String>> friends;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: ex3-1. 添加 ViewPager 和 Fragment 做可滑动界面
        // TODO: ex3-2, 添加 TabLayout 支持 Tab
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ch3ex3);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) { }
            @Override
            public void onPageSelected(int i) { }
            @Override
            public void onPageScrollStateChanged(int i) { }
        });

        tabs = new ArrayList<>();
        fragmentList = new ArrayList<>();
        friends = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            tabs.add("Tab " + i);
            tabLayout.addTab(tabLayout.newTab().setText(tabs.get(i)));
            fragmentList.add(PlaceholderFragment.newInstance(i));
            friends.add(new ArrayList<String>());
            for (int j = 0; j < 10; ++j) {
                friends.get(i).add(Integer.toString(i * 10 + j));
            }
        }

        PagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragmentList.get(i);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabs.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager, false);
    }
}