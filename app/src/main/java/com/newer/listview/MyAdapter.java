package com.newer.listview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**  自定义适配器
 * Created by mine on 15-11-14.
 */
public class MyAdapter extends BaseAdapter{
    //数据
    private ArrayList<Book> data;
    private Context context;

    //布局服务   inflater(充气机)
    private LayoutInflater inflater;

    /**
     * 构造方法
     * @param data  数据
     * @param context  上下文
     */
    public MyAdapter(ArrayList<Book> data, Context context) {
        this.data = data;
        this.context = context;
        //获得服务
        inflater=LayoutInflater.from(context);
    }

    /**
     * 列表数量
     * @return int 视图中有多少行
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     *  获得指定位置的数据
     * @param position  位置
     * @return
     */
    @Override
    public Book getItem(int position) {
        return data.get(position);
    }

    /**
     * 获得指定位置的id
     * @param position  位置
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * 加载视图
     * @param position  位置
     * @param convertView  可重用的视图
     * @param parent  父元素
     * @return  View 视图项
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ButtonListener listener;

        ViewHolder holder = null;
        //加载布局，创建视图
        if(null==convertView){
            //没有可复用
            convertView = inflater.inflate(R.layout.item_book, parent, false);
            holder=new ViewHolder();

            //获得视图中的控件
            holder.imageView= (ImageView) convertView.findViewById(R.id.imageView);
            holder.title=(TextView) convertView.findViewById(R.id.textView_title);
            holder.author=(TextView) convertView.findViewById(R.id.textView_author);
            holder.ratingBar=(RatingBar) convertView.findViewById(R.id.ratingBar);
            holder.button= (Button) convertView.findViewById(R.id.button);

            listener =new ButtonListener();
            holder.button.setOnClickListener(listener);

            holder.button.setTag(listener);
            convertView.setTag(holder);
        }else{
            //可复用
            holder= (ViewHolder) convertView.getTag();
            listener= (ButtonListener) holder.button.getTag();
        }

        //绑定数据
        Book b=data.get(position);
        holder.author.setText("作者：" + b.getAuthor());
        holder.title.setText("标题：" + b.getTitle());
        holder.ratingBar.setRating(b.getRating());
//        holder.imageView.setImageResource(android.R.drawable.ic_btn_speak_now);

        listener.setData(b);

        Log.d("getView",String.format("视图项：%d,数据：%d",holder.id,position));
        //返回视图
        return convertView;
    }

    static int counter=1;

    private static class ViewHolder{
        int id=counter++;
        ImageView imageView;
        TextView title;
        TextView author;
        RatingBar ratingBar;
        Button button;
    }

    //按钮监听器
    class ButtonListener implements View.OnClickListener{

        private Book b;
        @Override
        public void onClick(View v) {
//            Toast.makeText(context,data.getTitle(),Toast.LENGTH_SHORT).show();
            PopupMenu menu=new PopupMenu(context,v);
            menu.inflate(R.menu.popup);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_copy:
                            doEdit();
                            break;
                        case R.id.action_remove:
                            doRemove();
                            break;
                    }
                    return true;
                }

                private void doEdit() {
                    showToast("编辑" + b.getTitle());
                }

                private void doRemove() {
                    showToast("删除" + b.getTitle());
                    data.remove(b);

                    //通知界面改变
                    notifyDataSetChanged();
                }
            });
            menu.show();
        }

        public void setData(Book b) {
            this.b=b;
        }
    }

    private void showToast(String s) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}
