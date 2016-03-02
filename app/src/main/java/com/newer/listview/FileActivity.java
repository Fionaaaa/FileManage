package com.newer.listview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.newer.listview.com.newer.file.FileOperator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * test_file 文件夹的管理
 */
public class FileActivity extends AppCompatActivity {
    private File[] files;       //文件列表
    private FileAdapter adapter;   //适配器
    private ListView listView;

    private File parentFile;  //父目录
    private ArrayList<TextView> arrayList = new ArrayList<>();
    private LinearLayout linearLayout;        //文件导航

    private RelativeLayout relativeLayout = null;  //查看文件属性的布局

    private ArrayList<File> copyFiles;   //选择需要复制的文件
    private ArrayList<File> filesRemove;    //需要删除的文件

    int sortId;  //文件排序方式的id

    ProgressDialog pdCopy;  //复制文件进度条
    ProgressDialog pdDelete;    //删除文件进度条
    private int progressStatus; //进度百分数
    private static final int MAX_PROGREASS=100;  //进度最大值
    private String copuFileName;    //正在复制的文件的名字

    /**
     *  Handler 处理器
     */
    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            //表明该消息是由程序发送的
            switch (msg.what){
                case 0x123:
                    pdCopy.setProgress(progressStatus);
                    pdCopy.setMessage("正在复制：\n"+copuFileName);
                    break;
                case 0x321:
                    pdDelete.setProgress(progressStatus);
                    pdDelete.setMessage("正在删除：\n"+copuFileName);
                    break;
                default:initView(files);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        //获取SD卡内容
        parentFile = new File(Environment.getExternalStorageDirectory().getPath());
        files = parentFile.listFiles();

        sortId = R.id.file_as_char;   //文件排序默认为字符优先

        initView(files);
    }

    /**
     * 将SD卡的内容显示到ListView中
     *
     * @param files
     */
    private void initView(File[] files) {

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_file_info);   //查看xml、txt文件 属性 布局

        //自定义适配器
        adapter = new FileAdapter(this, files);
        listView = (ListView) findViewById(R.id.listView_file);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterViewListener());

        //进入CAB 模式
        listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new CbaListener());
    }

    /**
     * 适配器视图监听器
     */
    private class AdapterViewListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String fileName =files[position].getName();
            if (!fileName.contains(".")) {
                for (File f : files) {
                    if (f.getName().equals(fileName)) {
                        files = f.listFiles();
                        parentFile = f;
                    }
                }
                initTextView();
                initView(getFiles(files, sortId));
            } else {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri=Uri.fromFile(files[position]);

                String ext=fileName.substring(fileName.lastIndexOf(".") + 1);
                MimeTypeMap mime=MimeTypeMap.getSingleton();
                String type=mime.getMimeTypeFromExtension(ext);

                intent.setDataAndType(uri,type);
                if(ext!=null){
                    startActivity(intent);
                }else{
                    Toast.makeText(FileActivity.this,"暂不支持打开此类文件",Toast.LENGTH_SHORT).show();
                }

                /*if (!(fileName.contains(".txt") || fileName.contains(".xml"))) {
                    Toast.makeText(getApplicationContext(), "暂不支持打开此类文件", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        FileReader reader = new FileReader(files[position]);
                        BufferedReader buff = new BufferedReader(reader);
                        String st = null;
                        StringBuffer strBuff = new StringBuffer();
                        while ((st = buff.readLine()) != null) {
                            strBuff.append(st + "\n");
                        }
                        ((TextView) findViewById(R.id.textView_file_detail)).setText(strBuff);
                        relativeLayout.setVisibility(View.VISIBLE);
                        buff.close();
                        reader.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //imageButton 监听
                    findViewById(R.id.imageButton_info).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (relativeLayout != null && relativeLayout.getVisibility() == View.VISIBLE) {
                                relativeLayout.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }*/
            }
        }
    }

    /**
     * 文件导航栏的处理
     */
    private void initTextView() {
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml("/<u>" + parentFile.getName() + "</u>"));
//        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        textView.setTextSize(16);
        textView.setTextColor(Color.rgb(0x1E, 0x90, 0xFF));
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        linearLayout.addView(textView);
        arrayList.add(textView);
        textView.setTag(parentFile);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFile = (File) v.getTag();
                files = parentFile.listFiles();
                initView(getFiles(files, sortId));
                for (int i = arrayList.indexOf(v) + 1; i < arrayList.size(); i++) {
                    linearLayout.removeView(arrayList.get(i));
                    arrayList.remove(i);
                }
            }
        });
    }

    /**
     * 返回键的监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果处于查看文件属性
            if (relativeLayout != null && relativeLayout.getVisibility() == View.VISIBLE) {
                relativeLayout.setVisibility(View.INVISIBLE);
                return true;
            }
            View view = arrayList.get(arrayList.size() - 1);
            arrayList.remove(view);
            linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
            linearLayout.removeView(view);
            if (parentFile.getName().equals("test_file")) {
                return super.onKeyDown(keyCode, event);
            }
            parentFile = parentFile.getParentFile();
            files = parentFile.listFiles();
            initView(getFiles(files, sortId));   //返回上一个目录

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * 菜单监听
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id != R.id.action_sort) {
            sortId = id;
            item.setChecked(true);
            files = getFiles(files, id);
            initView(files);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 文件排序方式
     *
     * @param files
     * @param id
     * @return
     */
    private File[] getFiles(File[] files, int id) {
        ArrayList<File> list = new ArrayList();
        for (File f : files) {
            list.add(f);
        }


        switch (id) {
            case R.id.file_as_char:
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        return a.getName().compareTo(b.getName());
                    }
                });
                break;
            case R.id.file_as_folder:
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        if (a.isDirectory()) {
                            return -1;
                        } else if (b.isDirectory()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
                break;
            case R.id.file_as_max:
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        if (a.length() >= b.length()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                break;
            case R.id.file_as_min:
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        if (a.length() >= b.length()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case R.id.file_as_change:
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        if (b.lastModified() >= a.lastModified()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
        }
        for (int i = 0; i < list.size(); i++) {
            files[i] = list.get(i);
        }
        return files;
    }

    /**
     * 多选模式监听
     */
    private class CbaListener implements AbsListView.MultiChoiceModeListener {
        private Menu menu_main; //CAB 菜单
        private SparseBooleanArray sparseBooleanArray;

        /**
         * 选择状态改变
         *
         * @param mode
         * @param position
         * @param id
         * @param checked
         */
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            sparseBooleanArray = listView.getCheckedItemPositions();
            int count = listView.getCheckedItemCount();
            mode.setTitle("选中：" + String.valueOf(count));

            //选择一项时可以查看属性
            if (count == 1) {
                menu_main.findItem(R.id.action_cba_info).setEnabled(true);
            } else {
                menu_main.findItem(R.id.action_cba_info).setEnabled(false);
            }
        }

        /**
         * 加载多选模式
         *
         * @param mode
         * @param menu
         * @return
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.cab_menu, menu);
            menu_main = menu;

            //如果有复制内容，paste 可按
            if (copyFiles != null && copyFiles.size() != 0) {
                menu_main.findItem(R.id.action_cba_paste).setEnabled(true);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * 点击菜单
         *
         * @param mode
         * @param item
         * @return
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_cba_cached:
                    files = parentFile.listFiles();
                    initView(files);
                    Toast.makeText(getApplicationContext(), "已刷新", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_cba_remove:
                    showProgressRemove();
                    new Thread(){
                        @Override
                        public void run() {
                            doRemove();
                        }
                    }.start();
                    Toast.makeText(getApplicationContext(), "已删除", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_cba_copy:
                    doCbaCopy();
                    Toast.makeText(getApplicationContext(), "已复制", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_cba_paste:
                    showProgress();
                    new Thread(){
                        @Override
                        public void run() {
                            doPaste();
                        }
                    }.start();
                    Toast.makeText(getApplicationContext(), "已粘贴", Toast.LENGTH_SHORT).show();
                    menu_main.findItem(R.id.action_cba_paste).setEnabled(false);
                    break;
                case R.id.action_cba_info:
                    doCbaInfo();
                    break;
                case R.id.action_cba_creatFolder:
                    doCreate();
                    break;
            }

            mode.finish();  //退出多选模式
            return true;
        }

        /**
         *  创建新文件
         */
        private void doCreate() {
            View view=getLayoutInflater().inflate(R.layout.item_create_file,null);
            final EditText editText= (EditText) view.findViewById(R.id.editText_create_file);
            new AlertDialog.Builder(FileActivity.this)
                    .setTitle("新建文件")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String inName= String.valueOf(editText.getText());
                            if (which==AlertDialog.BUTTON_POSITIVE){
                                if(inName!=null){
                                    if(inName.contains(".")){
                                        try {
                                            new File(parentFile,inName).createNewFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }else{
                                        new File(parentFile,inName).mkdir();
                                    }
                                }
                            }
                            dialog.dismiss();   //关闭对话框
                            //重新加载文件列表
                            files=parentFile.listFiles();
                            initView(files);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }

        /**
         * 文件删除
         */
        private void doRemove() {

            for(int i=0;i<filesRemove.size();i++){
                FileOperator.remove(filesRemove.get(i));

                copuFileName=filesRemove.get(i).getName();
                progressStatus=MAX_PROGREASS*(i+1)/filesRemove.size();
                handler.sendEmptyMessage(0x321);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            pdDelete.dismiss();

            //通知界面改变
            files = parentFile.listFiles();
            handler.sendEmptyMessage(0x412);
        }

        /**
         * 文件粘贴
         */
        private void doPaste() {
            if (copyFiles.size() != 0) {
                for (int i=0;i<copyFiles.size();i++) {

                    copuFileName=copyFiles.get(i).getName();
                    progressStatus=MAX_PROGREASS*(i+1)/copyFiles.size();
                    handler.sendEmptyMessage(0x123);

                    if (files[i].isFile()) {
                        try {
                            FileOperator.copyFile(copyFiles.get(i), parentFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            FileOperator.copyFolder(copyFiles.get(i), parentFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                pdCopy.dismiss();   //关闭进度条对话框
            }
            copyFiles.removeAll(copyFiles);  //清空copyFile

            //通知界面改变
            files = parentFile.listFiles();
            handler.sendEmptyMessage(0x124);
        }


        /**
         * 文件复制
         */
        private void doCbaCopy() {
            //点击了复制，粘贴菜单才可按
            menu_main.findItem(R.id.action_cba_paste).setEnabled(true);

            copyFiles = new ArrayList<File>();
            for (int i = sparseBooleanArray.size() - 1; i >= 0; i--) {
                if (sparseBooleanArray.get(sparseBooleanArray.keyAt(i))) {
                    copyFiles.add(files[sparseBooleanArray.keyAt(i)]);   //获得选择的文件对象
                }
            }
        }

        /**
         * 查看文件属性
         */
        private void doCbaInfo() {
            TextView textView = (TextView) findViewById(R.id.file_info);
            ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_info);
            File file = null;
            for (int i = sparseBooleanArray.size() - 1; i >= 0; i--) {
                if (sparseBooleanArray.get(sparseBooleanArray.keyAt(i))) {
                    file = files[sparseBooleanArray.keyAt(i)];   //获得选择的文件对象
                }
            }
            if (null != relativeLayout) {
                relativeLayout.setVisibility(View.VISIBLE);
            }
            String fileName = file.getName();
            String fileType;
            if (fileName.contains(".")) {
                fileType = fileName.substring(fileName.lastIndexOf(".") + 1) + "文件";
            } else {
                fileType = "文件夹";
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd hh:mm:ss");
            Date date = new Date(file.lastModified());
            String lastChange = dateFormat.format(date);
            String info = "文件名：" + file.getName() + "\n文件类型：" + fileType + "\n" + FileAdapter.getFileSize(file.length())
                    + "\n最后修改时间：" + lastChange + "\n文件位置：" + file.getAbsolutePath();
            textView.setText(info);

            //imageButton 监听
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (relativeLayout != null && relativeLayout.getVisibility() == View.VISIBLE) {
                        relativeLayout.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    /**
     *  复制进度条
     */
    private void showProgress() {
        progressStatus=0;
        pdCopy=new ProgressDialog(FileActivity.this);
        pdCopy.setMax(MAX_PROGREASS);
        pdCopy.setTitle("复制进度");
        pdCopy.setMessage("正在复制：");
        pdCopy.setCancelable(false);
        pdCopy.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdCopy.setIndeterminate(false);
        pdCopy.show();
    }

    /**
     *  删除进度条
     */
    private void showProgressRemove() {
        progressStatus=0;
        pdDelete=new ProgressDialog(FileActivity.this);
        pdDelete.setMax(MAX_PROGREASS);
        pdDelete.setTitle("删除进度");
        pdDelete.setMessage("正在删除：");
        pdDelete.setCancelable(false);
        pdDelete.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdDelete.setIndeterminate(false);
        pdDelete.show();

        //获得需要删除的文件
        filesRemove=new ArrayList<>();
        //获得稀疏数组
        SparseBooleanArray array = listView.getCheckedItemPositions();

        for (int i = array.size() - 1; i >= 0; i--) {
            if (array.get(array.keyAt(i))) {
                File file = files[array.keyAt(i)];
                filesRemove.add(file);
            }
        }
    }
}

