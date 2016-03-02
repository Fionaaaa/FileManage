package com.newer.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.newer.listview.com.newer.file.FileOperator;

import java.io.File;

/**
 * 自定义一个文件ListView 适配器
 * Created by mine on 15-11-15.
 */
public class FileAdapter extends BaseAdapter {
    //数据
    private File[] files;
    //上下文
    Context context;
    //布局服务器
    LayoutInflater inflater;

    /**
     * 构造函数:初始化 数据+上下文+布局服务器
     *
     * @param context
     * @param files
     */
    protected FileAdapter(Context context, File[] files) {
        this.files = files;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return files[position];
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    /**
     * 手机屏幕每显示一个item，就调用一次
     *
     * @param position
     * @param convertView
     * @param parent
     * @return 列表项
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ImageButtonListener imageButtonListener;
        if (null == convertView) {
            //convertView 不可重用时，实例化xml文件创建视图
            convertView = inflater.inflate(R.layout.item_file, parent, false);
            holder = new ViewHolder();

            holder.imageButton = (ImageButton) convertView.findViewById(R.id.imageButton);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_file);
            holder.textViewName = (TextView) convertView.findViewById(R.id.textView_file);
            holder.textViewInfo = (TextView) convertView.findViewById(R.id.textView_info);

            imageButtonListener = new ImageButtonListener();
            holder.imageButton.setOnClickListener(imageButtonListener);
            holder.imageButton.setTag(imageButtonListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            imageButtonListener = (ImageButtonListener) holder.imageButton.getTag();
        }
        //给控件设置text值
        File file = files[position];
        holder.textViewName.setText(file.getName());
        holder.imageButton.setImageResource(R.drawable.ic_more_vert_grey600_16dp);
        if (file.isFile()) {
            holder.imageView.setImageResource(R.drawable.ic_file);
            holder.textViewInfo.setText(getFileSize(file.length()));
        } else {
            holder.imageView.setImageResource(R.drawable.ic_folder);
            holder.textViewInfo.setText(String.format("目录：%d个文件", file.exists() ? file.listFiles().length : -1));
//            holder.textViewInfo.setText(String.format("目录：%d个文件",file.listFiles().length));
        }
        imageButtonListener.file = file;
        return convertView;
    }

    /**
     * FileAdapter内部类  imageButton的监听器
     */
    private class ImageButtonListener implements View.OnClickListener {
        File file;

        @Override
        public void onClick(final View v) {
            //弹出选择菜单
            PopupMenu menu = new PopupMenu(context, v);
            //菜单布局
            menu.inflate(R.menu.popup);
            //显示菜单
            menu.show();
            /**
             * 匿名内部类  菜单监听器
             */
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_remove:
                            doRemove(v,file);
                            break;
                        case R.id.action_copy:
                            doCopy(file,file.getParentFile());
                            break;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 菜单选项：复制
     * @param file
     * @param parentFile
     */
    public  void doCopy(File file, File parentFile) {
        if (file.isFile()){
            try {
                FileOperator.copyFile(file,parentFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
//            Toast.makeText(context,"暂不支持复制文件夹",Toast.LENGTH_SHORT).show();
            try {
                FileOperator.copyFolder(file,parentFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        files = file.getParentFile().listFiles();
        notifyDataSetChanged();
    }

    /**
     * 菜单选项：删除
     * @param v
     * @param f
     */

    private void doRemove(View v,final File f) {
        @SuppressLint({"NewApi", "LocalSuppress"}) PopupMenu menu = new PopupMenu(context, v, Gravity.CENTER);
        menu.inflate(R.menu.remove);
        menu.show();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.yes:
                        FileOperator.remove(f);
                        break;
                    case R.id.no:
                        Toast.makeText(context, "取消删除", Toast.LENGTH_SHORT).show();
                        break;
                }
                files = f.getParentFile().listFiles();
                notifyDataSetChanged();
                return false;
            }
        });

    }

    /**
     * FileAdapter的内部类
     * 列表项的【结构持有者】
     * 存储item_file.xml文件中的控件
     * 直接通过字段访问，提高性能
     */
    private static class ViewHolder {
        ImageButton imageButton;
        TextView textViewName;
        TextView textViewInfo;
        ImageView imageView;
    }

    /**
     * 文件大小显示方式
     *
     * @param size
     * @return
     */
    public static String getFileSize(long size) {
        String msg = null;
        if (size > 1024 * 1024) {
            msg = String.format("文件大小：%.2fM", size / 1024f / 1024f);
        } else {
            msg = String.format("文件大小：%.2fKB", size / 1024f);
        }
        return msg;
    }

}
