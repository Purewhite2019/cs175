package com.example.chapter3.homework;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class PlaceholderFragment extends Fragment {

    private int idx = -1;
    private final int switchDuration = 500;
    FrameLayout view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO ex3-3: 修改 fragment_placeholder，添加 loading 控件和列表视图控件
        super.onCreateView(inflater, container, savedInstanceState);
        view = (FrameLayout) inflater.inflate(R.layout.fragment_placeholder, container, false);
        idx = getArguments().getInt("index");
//        TextView tx = view.findViewById(R.id.frag_id);
//        tx.setText("Hello, fragment " + idx);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final com.airbnb.lottie.LottieAnimationView lottieAnimationView = new com.airbnb.lottie.LottieAnimationView(view.getContext());
        lottieAnimationView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lottieAnimationView.setAnimation(R.raw.material_wave_loading);
        lottieAnimationView.loop(true);
        lottieAnimationView.setAlpha(1f);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_expandable_list_item_1, Ch3Ex3Activity.friends.get(idx));
        final ListView listView = new ListView(view.getContext());
        listView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        listView.setAlpha(0f);
        listView.setAdapter(adapter);


        view.addView(listView);
        view.addView(lottieAnimationView);
        lottieAnimationView.playAnimation();

        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里会在 5s 后执行
                // TODO ex3-4：实现动画，将 lottie 控件淡出，列表数据淡入
                lottieAnimationView.animate()
                        .alpha(0f)
                        .setDuration(switchDuration);

                listView.animate()
                        .alpha(1f)
                        .setDuration(switchDuration);


            }
        }, 5000);
    }

    public static PlaceholderFragment newInstance(int _idx) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt("index", _idx);
        fragment.setArguments(args);
        return fragment;
    }
}
