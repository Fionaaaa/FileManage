package com.newer.listview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Book> data;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        initView();
    }

    private void initView() {
        listView= (ListView) findViewById(R.id.listView_book);
        data=new ArrayList<>();
        for(int i=0;i<30;i++){
            Book book=new Book();
            book.setAuthor("@SmallQiang");
            book.setTitle("<<理工大学三国传" + i + ">>");
            book.setRating((int) (Math.random() * 5 + 1));
            data.add(book);
        }
        adapter=new MyAdapter(data,this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book b=adapter.getItem(position);
                Toast.makeText(BookActivity.this,b.getTitle(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
