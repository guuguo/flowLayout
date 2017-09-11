package top.guuguo.flowlayout.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.flyco.roundview.RoundTextView;

import java.util.ArrayList;

import top.guuguo.flowlayout.FlowAdapter;
import top.guuguo.flowlayout.FlowLayout;

public class MainActivity extends AppCompatActivity {
    String[] bookStr = {"影视", "读书", "音乐"};
    String[] lifeStr = {"旅行", "居家", "美食", "手作", "运动健身", "时尚"};
    String[] worldStr = {"人文", "科技", "摄影", "艺术", "画画儿", "建筑"};
    String[] growStr = {"故事", "情感", "成长", "涨知识", "理财"};
    String[] interestStr = {"找乐", "宠物", "娱乐八卦", "动漫", "自然", "美女"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAdapterViews((FlowLayout) findViewById(R.id.fl_no_check), bookStr, R.color.color_green);
        setAdapterViews((FlowLayout) findViewById(R.id.fl_single_check), lifeStr, R.color.color_yellow_dark);
        setAdapterViews((FlowLayout) findViewById(R.id.fl_multi_check), worldStr, R.color.color_green_dark);
        FlowAdapter multiCheck3Adapter = setAdapterViews((FlowLayout) findViewById(R.id.fl_multi_3), growStr, R.color.color_blue_dark);
        multiCheck3Adapter.setCheckLimit(3);
        FlowAdapter multiCheck5Adapter = setAdapterViews((FlowLayout) findViewById(R.id.fl_multi_5), interestStr, R.color.color_purple);
        multiCheck5Adapter.setCheckLimit(5);
    }

    private FlowAdapter<String> setAdapterViews(final FlowLayout view, String[] strS, @ColorRes int colorRes) {
        final int color = ContextCompat.getColor(this, colorRes);
        FlowAdapter adapter = new FlowAdapter<String>() {
            @Override
            protected View onCreateView() {
                return MainActivity.this.getLayoutInflater().inflate(R.layout.item_tag, view, false);
            }

            @Override
            protected void onBindView(View view, String item, boolean isChecked) {
                RoundTextView tv = view.findViewById(R.id.tv_content);
                tv.setText(item);
                tv.getDelegate().setStrokeColor(color);
                if (isChecked) {
                    tv.setTextColor(Color.WHITE);
                    tv.getDelegate().setBackgroundColor(color);
                } else {
                    tv.setTextColor(color);
                    tv.getDelegate().setBackgroundColor(Color.TRANSPARENT);
                }
            }

            @Override
            protected void isMaxChecked(int limitedMaxNum) {
                super.isMaxChecked(limitedMaxNum);
                Toast.makeText(MainActivity.this, "最多只能选择" + limitedMaxNum + "个", Toast.LENGTH_SHORT).show();
            }
        };
        adapter.setNewData(toList(strS));
        view.setAdapter(adapter);
        return adapter;
    }

    ArrayList<String> toList(String[] strS) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : strS) {
            list.add(s);
        }
        return list;
    }
}
