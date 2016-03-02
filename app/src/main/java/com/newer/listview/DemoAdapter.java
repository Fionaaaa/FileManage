package com.newer.listview;

import android.content.Context;
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

import java.io.File;

/**
 * 自定义适配器
 */
public class DemoAdapter extends BaseAdapter {

    // 数据
    private File[] files;

    // 上下文
    private Context context;

    // 布局服务: 实例化 xml 布局
    private LayoutInflater inflater;

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param files   文件列表
     */
    public DemoAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
        inflater = LayoutInflater.from(context);
    }

    /**
     * 获得数据的总数
     *
     * @return 数据的总数
     */
    @Override
    public int getCount() {
        return files.length;
    }

    /**
     * 获得特定位置的数据
     *
     * @param position 位置
     * @return 特定位置的数据
     */
    @Override
    public File getItem(int position) {
        return files[position];
    }

    /**
     * 获得特定位置数据的 id（使用 SQLite 的场景）
     *
     * @param position 位置
     * @return 特定位置数据的 id
     */
    @Override
    public long getItemId(int position) {
        // 位置是列表项在 ListView 中的索引
        // 在排序规则改变时，位置的数值会改变
        // id 是数据的唯一标识，不会改变

        return 0;
    }

    /**
     * 获得特定位置加载了数据的视图（列表项）
     *
     * @param position    位置
     * @param convertView 可重用的视图
     * @param parent      父元素
     * @return 列表项
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        ImageButtonListener listener;
        if (null == convertView) {
            // 若无【可重用的视图】，才实例化 xml 文件创建视图
            // inflate 方法执行的次数为屏幕上能显示的列表项的最大值
            convertView = inflater.inflate(R.layout.item_file, parent, false);

            // 创建【结构持有者】，获得视图中的各个控件的引用
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.imageView_file);
            holder.filename = (TextView) convertView.findViewById(R.id.textView_file);
            holder.summary = (TextView) convertView.findViewById(R.id.textView_info);
            holder.button = (ImageButton) convertView.findViewById(R.id.imageButton);

            // 创建按钮的监听器
            listener = new ImageButtonListener();
            // 注册监听器
            holder.button.setOnClickListener(listener);
            // 将监听器存储【绑定】到按钮中
            holder.button.setTag(listener);

            // 将【结构持有者】存储到视图中
            convertView.setTag(holder);
        } else {
            // 有【可重用的视图】，则从中取出它的【结构持有者】
            holder = (ViewHolder) convertView.getTag();

            // 获得按钮的监听器
            listener = (ImageButtonListener) holder.button.getTag();
        }

        // 在【结构持有者】中加载 position 位置的数据
        File file = files[position];

        holder.filename.setText(file.getName());
        holder.summary.setText(file.isFile()
                ? String.format("文件大小：%,d 字节", file.length())
                : String.format("目录：%d 个文件", file.listFiles().length));
        holder.icon.setImageResource(file.isFile()
                ? R.drawable.ic_file
                : R.drawable.ic_folder);

        // 修改监听器的监听的数据
        listener.setData(file);

        // 返回视图
        return convertView;
    }

    /**
     * 性能较差
     *
     * @param position
     * @param parent
     * @return
     */
    private View planA(int position, ViewGroup parent) {
        View convertView;// 使用 LayoutInflater 实例化 xml 布局文件【创建视图】
        convertView = inflater.inflate(R.layout.item_file, parent, false);

        // 使用 findViewById 获得视图中的各个控件的引用
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        TextView filename = (TextView) convertView.findViewById(R.id.textView_file);
        TextView summary = (TextView) convertView.findViewById(R.id.textView_info);
        ImageButton button = (ImageButton) convertView.findViewById(R.id.button);

        // 在控件中加载 position 位置的数据
        File file = files[position];
        filename.setText(file.getName());
        summary.setText(file.isFile()
                ? "文件大小：" + file.length()
                : "目录：" + file.listFiles().length + " 个文件");
        icon.setImageResource(file.isFile()
                ? R.drawable.ic_file
                : R.drawable.ic_folder);
        button.setOnClickListener(null);
        return convertView;
    }


    /**
     * 列表项的【结构持有者】
     * 存储 file_item.xml 文件中的控件结构
     * <p>
     * 直接通过字段访问，为提高性能
     */
    private static class ViewHolder {
        ImageView icon;
        TextView filename;
        TextView summary;
        ImageButton button;
    }

    /**
     * FileAdapter 的内部类
     * 自定义的 ImageButton 点击监听器
     */
    private class ImageButtonListener implements View.OnClickListener {

        // 监听器要操作的数据
        private File data;

        // 修改数据
        public void setData(File file) {
            data = file;
        }

        @Override
        public void onClick(View v) {

            // 创建弹出菜单
            PopupMenu menu = new PopupMenu(context, v);

            // 设置菜单的布局文件
            menu.inflate(R.menu.popup);

            // 注册菜单项监听器
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_copy:
                            doCopy(data);
                            break;
                        case R.id.action_remove:
                            doDelete(data);
                            break;
                    }
                    return true;
                }
            });

            // 显示弹出菜单
            menu.show();
        }

    }

    private void doDelete(File data) {
        Toast.makeText(context, "删除：" + data.getName(), Toast.LENGTH_SHORT).show();
    }

    private void doCopy(File data) {
        Toast.makeText(context, "复制：" + data.getName(), Toast.LENGTH_SHORT).show();
    }
}
