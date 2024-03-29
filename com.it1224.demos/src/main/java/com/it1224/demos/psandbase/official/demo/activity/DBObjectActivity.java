package com.it1224.demos.psandbase.official.demo.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ab.activity.AbActivity;
import com.ab.db.storage.AbSqliteStorage;
import com.ab.db.storage.AbSqliteStorageListener.AbDataDeleteListener;
import com.ab.db.storage.AbSqliteStorageListener.AbDataInsertListener;
import com.ab.db.storage.AbSqliteStorageListener.AbDataSelectListener;
import com.ab.db.storage.AbSqliteStorageListener.AbDataUpdateListener;
import com.ab.db.storage.AbStorageQuery;
import com.ab.util.AbToastUtil;
import com.ab.view.titlebar.AbTitleBar;
import com.it1224.demos.R;
import com.it1224.demos.global.BaseApplication;
import com.it1224.demos.psandbase.official.demo.adapter.UserDBListAdapter;
import com.it1224.demos.psandbase.official.demo.dao.UserInsideDao;
import com.it1224.demos.psandbase.official.demo.model.LocalUser;

import java.util.ArrayList;
import java.util.List;

/**
 * 名称：DBObjectActivity
 * 描述：数据库演示andbase的对象化存储
 *
 * @author 还如一梦中
 * @date 2011-12-13
 */
public class DBObjectActivity extends AbActivity {

    private BaseApplication application;
    //列表适配器
    private UserDBListAdapter myListViewAdapter = null;
    //列表数据
    private List<LocalUser> userList = null;
    private ListView mListView = null;
    //定义数据库操作实现类
    private UserInsideDao userDao = null;

    //每一页显示的行数
    public int pageSize = 10;
    //当前页数
    public int pageNum = 1;
    //总条数
    public int totalCount = 0;
    //分页栏
    private LinearLayout mListViewForPage;
    //总条数和当前显示的几页
    public TextView total, current;
    //上一页和下一页的按钮
    private Button preView, nextView;

    //数据库操作类
    private AbSqliteStorage mAbSqliteStorage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAbContentView(R.layout.db_sample);

        AbTitleBar mAbTitleBar = this.getTitleBar();
        mAbTitleBar.setTitleText(R.string.db_object_name);
        mAbTitleBar.setLogo(R.drawable.button_selector_back);
        mAbTitleBar.setTitleBarBackground(R.mipmap.top_bg);
        mAbTitleBar.setTitleTextMargin(10, 0, 0, 0);
        mAbTitleBar.setLogoLine(R.mipmap.line);

        application = (BaseApplication) abApplication;

        //初始化AbSqliteStorage
        mAbSqliteStorage = AbSqliteStorage.getInstance(this);

        //初始化数据库操作实现类
        userDao = new UserInsideDao(DBObjectActivity.this);

        userList = new ArrayList<LocalUser>();

        //获取ListView对象
        mListView = (ListView) this.findViewById(R.id.mListView);
        //分页栏
        mListViewForPage = (LinearLayout) this.findViewById(R.id.mListViewForPage);
        //上一页和下一页的按钮
        preView = (Button) this.findViewById(R.id.preView);
        nextView = (Button) this.findViewById(R.id.nextView);
        //分页栏显示的文本
        total = (TextView) findViewById(R.id.total);
        current = (TextView) findViewById(R.id.current);

        //创建一个HeaderView用于向数据库中增加一条数据
        View headerView = mInflater.inflate(R.layout.db_list_header, null);
        //加到ListView的顶部
        mListView.addHeaderView(headerView);
        //使用自定义的Adapter
        myListViewAdapter = new UserDBListAdapter(this, userList);
        mListView.setAdapter(myListViewAdapter);

        //增加记录的按钮
        final Button addBtn = (Button) headerView.findViewById(R.id.addBtn);
        //增加的字段数据
        final EditText mEditText = (EditText) headerView.findViewById(R.id.add_name);
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户输入的数据
                String name = mEditText.getText().toString();
                if (name != null && !"".equals(name.trim())) {
                    //增加一条数据到数据库
                    LocalUser u = new LocalUser();
                    u.setName(name);
                    saveData(u);
                } else {
                    AbToastUtil.showToast(DBObjectActivity.this, "请输入名称!");
                }
            }
        });

        //上一页事件
        preView.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preView();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //下一页事件
        nextView.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        nextView();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        queryData();

    }


    private void checkPageBar() {
        if (userList == null || userList.size() == 0) {
            //无数据隐藏分页栏
            mListViewForPage.setVisibility(View.GONE);
        } else {
            queryDataCount();
        }
    }

    /*
     * 上一页
     */
    private void preView() {
        preView.setEnabled(false);
        preView.setBackgroundResource(R.mipmap.left_press);
        pageNum--;
        current.setText("当前页:" + String.valueOf(pageNum));
        userList.clear();
        queryData();
    }

    /*
     * 下一页
     */
    private void nextView() {
        nextView.setEnabled(false);
        nextView.setBackgroundResource(R.mipmap.right_press);
        pageNum++;
        current.setText("当前页:" + String.valueOf(pageNum));
        userList.clear();

        queryData();
    }

    /*
     * 文本是否可点击
     */
    public void checkView() {
        if (pageNum <= 1) {
            //上一页文本为不可点击状态
            preView.setEnabled(false);
            preView.setBackgroundResource(R.mipmap.left_press);
            //总条数小于每页显示的条数
            if (totalCount <= pageSize) {
                //下一页文本为不可点击状态
                nextView.setEnabled(false);
                nextView.setBackgroundResource(R.mipmap.right_press);
            } else {
                nextView.setEnabled(true);
                nextView.setBackgroundResource(R.mipmap.right_normal);
            }
        }//总条数-当前页*每页显示的条数 <=每页显示的条数
        else if (totalCount - (pageNum - 1) * pageSize <= pageSize) {
            //下一页文本为不可点击状态,上一页变为可点击
            nextView.setEnabled(false);
            nextView.setBackgroundResource(R.mipmap.right_press);
            preView.setEnabled(true);
            preView.setBackgroundResource(R.mipmap.left_normal);
        } else {
            //上一页下一页文本设置为可点击状态
            preView.setEnabled(true);
            preView.setBackgroundResource(R.mipmap.left_normal);
            nextView.setEnabled(true);
            nextView.setBackgroundResource(R.mipmap.right_normal);
        }
    }


    public void saveData(LocalUser u) {

        //无sql存储的插入
        mAbSqliteStorage.insertData(u, userDao, new AbDataInsertListener() {

            @Override
            public void onSuccess(long id) {
                //showToast("插入数据成功id="+id);
                queryData();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

        });
    }

    public void queryData() {
        //查询数据
        AbStorageQuery mAbStorageQuery = new AbStorageQuery();
        mAbStorageQuery.setLimit(pageSize);
        mAbStorageQuery.setOffset((pageNum - 1) * pageSize);

        //无sql存储的查询
        mAbSqliteStorage.findData(mAbStorageQuery, userDao, new AbDataSelectListener() {

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

            @Override
            public void onSuccess(List<?> paramList) {
                userList.clear();
                if (paramList != null) {
                    userList.addAll((List<LocalUser>) paramList);
                }
                myListViewAdapter.notifyDataSetChanged();
                checkPageBar();
            }

        });

    }

    public void queryDataCount() {
        //查询数据
        AbStorageQuery mAbStorageQuery = new AbStorageQuery();

        //无sql存储的查询
        mAbSqliteStorage.findData(mAbStorageQuery, userDao, new AbDataSelectListener() {

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

            @Override
            public void onSuccess(List<?> paramList) {
                if (paramList != null) {
                    totalCount = paramList.size();
                }

                total.setText("总条数:" + String.valueOf(totalCount));
                current.setText("当前页:" + String.valueOf(pageNum));
                checkView();
                mListViewForPage.setVisibility(View.VISIBLE);
            }

        });

    }


    /**
     * 更新数据
     * 描述：TODO
     *
     * @param u
     */
    public void updateData(LocalUser u) {

        //无sql存储的更新
        mAbSqliteStorage.updateData(u, userDao, new AbDataUpdateListener() {

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

            @Override
            public void onSuccess(int rows) {
                queryData();
            }

        });


    }

    /**
     * 描述：根据ID查询数据
     *
     * @param id
     * @return
     */
    public void queryDataById(int id) {

        //条件
        AbStorageQuery mAbStorageQuery = new AbStorageQuery();
        mAbStorageQuery.equals("_id", String.valueOf(id));

        //无sql存储的查询
        mAbSqliteStorage.findData(mAbStorageQuery, userDao, new AbDataSelectListener() {

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

            @Override
            public void onSuccess(List<?> paramList) {
                if (paramList != null && paramList.size() > 0) {
                    LocalUser u = (LocalUser) paramList.get(0);
                    AbToastUtil.showToast(DBObjectActivity.this, "结果：_id:" + u.get_id() + ",name:" + u.getName());
                }
            }

        });

    }

    /**
     * 描述：删除数据
     *
     * @param id
     */
    public void delData(int id) {

        //条件
        AbStorageQuery mAbStorageQuery = new AbStorageQuery();
        mAbStorageQuery.equals("_id", String.valueOf(id));

        //无sql存储的删除
        mAbSqliteStorage.deleteData(mAbStorageQuery, userDao, new AbDataDeleteListener() {

            @Override
            public void onSuccess(int rows) {
                queryData();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                AbToastUtil.showToast(DBObjectActivity.this, errorMessage);
            }

        });

    }


    @Override
    public void finish() {
        //必须要释放
        mAbSqliteStorage.release();
        super.finish();
    }


}
