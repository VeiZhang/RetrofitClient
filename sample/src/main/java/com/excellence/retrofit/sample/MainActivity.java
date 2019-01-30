package com.excellence.retrofit.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.excellence.basetoolslibrary.baseadapter.CommonAdapter;
import com.excellence.basetoolslibrary.baseadapter.ViewHolder;
import com.excellence.basetoolslibrary.utils.ActivityUtils;
import com.excellence.permission.PermissionListener;
import com.excellence.permission.PermissionRequest;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private GridView mGridView = null;
    private String[] mActivityNames = null;
    private String[] mActivities = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivityNames = getResources().getStringArray(R.array.activity_names);
        mActivities = getResources().getStringArray(R.array.activities);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new ActivityAdapter(this, mActivityNames, android.R.layout.simple_list_item_1));
        mGridView.setOnItemClickListener(this);

        PermissionRequest.with(this).permission(WRITE_EXTERNAL_STORAGE).request(new PermissionListener() {
            @Override
            public void onPermissionsDenied() {
                Toast.makeText(MainActivity.this, "请授权，否则下载功能不能使用", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            ActivityUtils.startAnotherActivity(this, (Class<? extends Activity>) Class.forName(mActivities[position]));
        } catch (Exception e) {
            Toast.makeText(this, String.format("没有%1$s的栗子", mActivityNames[position]), Toast.LENGTH_SHORT).show();
        }
    }

    private class ActivityAdapter extends CommonAdapter<String> {
        public ActivityAdapter(Context context, String[] activityNames, int layoutId) {
            super(context, activityNames, layoutId);
        }

        @Override
        public void convert(ViewHolder viewHolder, String item, int position) {
            viewHolder.setText(android.R.id.text1, item);
        }
    }
}
