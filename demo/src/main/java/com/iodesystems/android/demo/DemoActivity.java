package com.iodesystems.android.demo;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.iodesystems.android.libs.view.NestedList;
import com.iodesystems.fn.Fn;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_demo)
public class DemoActivity extends Activity {
    @ViewById(R.id.nested_list)
    NestedList<Integer> nestedList;

    @AfterViews
    public void init() {
        nestedList.init(Fn.ofRange(1, 2).toList(), new NestedList.Adapter<Integer>() {
            @Override
            public List<Integer> getChildren(Integer o) {
                return Fn.ofRange(1, 1).convert(i -> i * o).toList();
            }

            @Override
            public int getItemLayout() {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            public void populateView(View view, Integer o) {
                ((TextView) view).setText(String.valueOf(o));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (nestedList.onBackPressed()) return;
        super.onBackPressed();
    }
}