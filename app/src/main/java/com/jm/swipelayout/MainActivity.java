package com.jm.swipelayout;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jm.swipelayout.adapter.CommonAdapter;
import com.jm.swipelayout.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private Adapter adapter;
    private ArrayList<String> mTestData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.lv);
        adapter = new Adapter(this, mTestData, R.layout.item_list);
        listview.setAdapter(adapter);
    }

    class Adapter extends CommonAdapter<String> {

        public Adapter(Context context, List<String> data, int layoutId) {
            super(context, data, layoutId);
        }

        @Override
        public void convert(ViewHolder holder, final String s) {
            holder.setText(R.id.tv_name, s);

            final SwipeLayout swipeLayout = holder.getView(R.id.swipelayout);

            swipeLayout.setOnItemClickListener(new SwipeLayout.OnItemClickListener() {
                @Override
                public void onItemClick() {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            });

            swipeLayout.getDeleteView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SwipeLayoutManager.getInstance().quickClose();
                    mTestData.remove(s);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext, "数据总数: " + mTestData.size(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void initData() {
        for (int i=0;i<20;i++) {
            mTestData.add("测试数据 "+i);
        }
    }
}
