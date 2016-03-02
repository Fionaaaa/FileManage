package com.newer.listview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_OTHER = "other";
    private static final String KEY_IMAGE ="image" ;
    private ListView listView;
    private ArrayList<HashMap<String, Object>> data;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 20; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put(KEY_NAME, "name_" + i);
            item.put(KEY_PHONE, "phone" );
            item.put(KEY_OTHER, "other_" + i);
            item.put(KEY_IMAGE,android.R.drawable.ic_dialog_email);
            data.add(item);
        }
        //适配器：数据和适配器视图间的桥梁
        String[] from = {KEY_NAME, KEY_PHONE,KEY_OTHER,KEY_IMAGE};                  //数据的key
        int[] to = {R.id.textView_name, R.id.textView_phone,R.id.textView_other,R.id.imageView};    //布局中要加载的控件
        simpleAdapter = new SimpleAdapter(this, data, R.layout.item_list, from, to);
        listView.setAdapter(simpleAdapter);
    }
}
