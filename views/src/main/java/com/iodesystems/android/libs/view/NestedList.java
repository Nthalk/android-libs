package com.iodesystems.android.libs.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.iodesystems.android.libs.view.transitions.TransitionPair;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class NestedList<T> extends RelativeLayout {

    private List<T> root;
    private final Deque<T> path = new LinkedList<T>();
    private ListView nextList = null;

    private Pager pager;
    private ListView listViewB;
    private ListView listViewA;
    private Adapter<T> adapter;
    private boolean selectable;

    public static abstract class Adapter<ITEM> {
        public abstract List<ITEM> getChildren(ITEM item);

        public abstract int getItemLayout();

        public abstract void populateView(View view, ITEM item);

        public void onItemClick(NestedList<ITEM> nestedList, ITEM item) {
            List<ITEM> children = getChildren(item);
            if (!children.isEmpty()) {
                nestedList.down(item);
            }
        }

        public View initializeView(View view) {
            return view;
        }
    }

    public NestedList(Context context) {
        super(context);
    }

    public NestedList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void init(List<T> root, Adapter<T> adapter) {
        this.adapter = adapter;
        this.root = root;
        pager = new Pager(getContext());
        buildLists();
        pager.enter(nextList(root));
    }

    private void buildLists() {
        listViewA = new ListView(getContext());
        if (!selectable) listViewA.setSelector(android.R.color.transparent);
        addView(listViewA);

        listViewB = new ListView(getContext());
        if (!selectable) listViewB.setSelector(android.R.color.transparent);
        addView(listViewB);
    }

    private List<T> getChildren(T item) {
        if (item == null) {
            return root;
        }
        return adapter.getChildren(item);
    }

    private int getItemLayout() {
        return adapter.getItemLayout();
    }

    private void populateView(View view, T item) {
        adapter.populateView(view, item);
    }

    public boolean onBackPressed() {
        if (path.isEmpty()) {
            return false;
        } else {
            up();
            pager.exit();
            return true;
        }
    }

    private void up() {
        path.pop();
        T top = path.peek();

        if (top == null) {
            nextList(root);
        } else {
            nextList(getChildren(top));
        }
    }

    private ListView nextList(final List<T> items) {
        nextList = nextList == listViewA ? listViewB : listViewA;
        nextList.setAdapter(getListAdapter(items));
        nextList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(NestedList.this, items.get(position));
            }
        });

        return nextList;
    }

    public void down(T to) {
        down(to, pager.getDefaultTransition());
    }

    public void down(T to, TransitionPair transition) {
        path.push(to);
        pager.enter(nextList(getChildren(to)), transition);
    }

    private ArrayAdapter<T> getListAdapter(List<T> items) {
        return new ArrayAdapter<T>(getContext(),
                                   getItemLayout(),
                                   items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;

                if (convertView == null) {
                    view = initializeView(LayoutInflater.from(getContext()).inflate(getItemLayout(), parent, false));
                } else {
                    view = convertView;
                }

                T item = getItem(position);
                populateView(view, item);

                return view;
            }
        };
    }

    private View initializeView(View view) {
        return adapter.initializeView(view);
    }
}
