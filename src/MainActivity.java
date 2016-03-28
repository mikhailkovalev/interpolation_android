package com.example.mixon.lagrangepolynomes;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements OpenGLViewListener {

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ArrayList<ListItem> myItems = new ArrayList<>();

        public MyAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int i = 0; i < 4/*Integer.parseInt(numPoints.getText().toString())*/; i++) {
                ListItem listItem = new ListItem();
                //listItem.caption = "Caption" + i;
                listItem.valueX = String.valueOf(myRand());
                listItem.valueY = String.valueOf(myRand());
                myItems.add(listItem);
            }
            notifyDataSetChanged();
        }

        public void addItem()
        {
            ListItem item = new ListItem();
            item.valueX = String.valueOf(myRand());
            item.valueY = String.valueOf(myRand());
            myItems.add(item);
            notifyDataSetChanged();
        }

        public void removeLastItem()
        {
            if (myItems.size() > 0)
                myItems.remove(myItems.size() - 1);
        }

        public int getCount() {
            return myItems.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public ArrayList<Float> getValuesX()
        {
            int i, n = myItems.size();
            //double[] ret = new double[n];
            ArrayList<Float> ret = new ArrayList<>();

            for (i = 0; i < n; ++i)
            {
                //ret[i] = Double.parseDouble(myItems.get(i).valueX);
                ret.add(Float.parseFloat(myItems.get(i).valueX));
            }

            return ret;
        }

        public ArrayList<Float> getValuesY()
        {
            int i, n = myItems.size();
            //double[] ret = new double[n];
            ArrayList<Float> ret = new ArrayList<>();

            for (i = 0; i < n; ++i)
            {
                //ret[i] = Double.parseDouble(myItems.get(i).valueY);
                ret.add(Float.parseFloat(myItems.get(i).valueY));
            }

            return ret;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.my_item, null);
                holder.valueX = (EditText) convertView
                        .findViewById(R.id.valueX);
                holder.valueY = (EditText) convertView.findViewById(R.id.valueY);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            //Fill EditText with the value you have in data source
            holder.valueX.setText(myItems.get(position).valueX);
            holder.valueX.setId(2 * position);
            holder.valueY.setText(myItems.get(position).valueY);
            holder.valueY.setId(2 * position + 1);

            //we need to update adapter once we finish with editing
            holder.valueX.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        final int position = v.getId();
                        final EditText Caption = (EditText) v;
                        myItems.get(position / 2).valueX = Caption.getText().toString();
                    }
                }
            });

            holder.valueY.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = v.getId();
                        final EditText Caption = (EditText) v;
                        myItems.get(position / 2).valueY = Caption.getText().toString();
                    }
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        EditText valueX;
        EditText valueY;
    }

    class ListItem {
        String valueX;
        String valueY;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myList = (ListView) findViewById(R.id.myList);
        numPoints  = (EditText) findViewById(R.id.numPoints);
        if (myAdapter == null) myAdapter = new MyAdapter();
        myList.setAdapter(myAdapter);
        glView = (OpenGLView) findViewById(R.id.glView);
        glView.setListener(this);

        numPoints.setText(String.valueOf(myList.getCount()));

        setTabHost();
        numPoints.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    try
                    {
                        int newValue = Integer.parseInt(((EditText) v).getText().toString());
                        while (myAdapter.getCount() < newValue) myAdapter.addItem();
                        while (myAdapter.getCount() > newValue) myAdapter.removeLastItem();
                    }
                    catch (Exception e)
                    {
                        return;
                    }
                }
            }
        });
        /*numPoints.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(MainActivity.this, "new Value = " + s.toString(), Toast.LENGTH_SHORT).show();
                try
                {
                    int newValue = Integer.parseInt(s.toString());
                    while (myAdapter.getCount() < newValue) myAdapter.addItem();
                    while (myAdapter.getCount() > newValue) myAdapter.removeLastItem();
                }
                catch (Exception e)
                {
                    return;
                }
            }
        });*/
        findViewById(R.id.draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    ArrayList<Float> vx = myAdapter.getValuesX();
                    ArrayList<Float> vy = myAdapter.getValuesY();
                    ICalculable f;
                    Float min = (float) getMin(vx),
                          max = (float) getMax(vx);
                    Float length = max - min;
                    switch (((RadioGroup) findViewById(R.id.polynomial_spline_group)).getCheckedRadioButtonId())
                    {
                        case R.id.polynomial:
                            f = new LagrangePolynomial(vx, vy);
                            break;
                        default:
                            sortData(vx, vy);
                            min -= 0.05f * length;
                            max += 0.05f * length;
                            f = new Spline(
                                    ((RadioGroup) findViewById(R.id.first_second_derivative_group)).getCheckedRadioButtonId() == R.id.first_derivative ? Spline.Type.FIRST : Spline.Type.SECOND,
                                    Double.parseDouble(((EditText) findViewById(R.id.left_boundary_value)).getText().toString()),
                                    Double.parseDouble(((EditText) findViewById(R.id.right_boundary_value)).getText().toString()),
                                    vx, vy
                            );
                    }
                    ArrayList<Float> color = randColor();

                    Model.addGraphic(f , min, max, color);
                    Model.addPointCollection(vx, vy, color);
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Model.removeAll();
                try {
                    Model.clear();
                    copyContent();
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.draw_func).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    ICalculable f = Parser.Parse(((EditText) findViewById(R.id.func)).getText().toString());
                    //float[] color = randColor();
                    ArrayList<Float> color = randColor();
                    Model.addGraphic(f,
                            Float.parseFloat(((EditText) findViewById(R.id.leftBound)).getText().toString()),
                            Float.parseFloat(((EditText) findViewById(R.id.rightBound)).getText().toString()),
                            color);

                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.appr_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Float> vx;
                ArrayList<Float> vy = new ArrayList<>();
                TreeNode f = Parser.Parse(((EditText) findViewById(R.id.func)).getText().toString());
                float left = Float.parseFloat(((EditText) findViewById(R.id.leftBound)).getText().toString());
                float right = Float.parseFloat(((EditText) findViewById(R.id.rightBound)).getText().toString());
                int i, n = myAdapter.getCount();
                switch(((RadioGroup) findViewById(R.id.rgroup)).getCheckedRadioButtonId())
                {
                    case R.id.equidistant:
                        vx = getEquidistantNodes(
                                left,
                                right,
                                n
                        );
                        break;
                    default:
                        vx = getChebyshevNodes(
                                left,
                                right,
                                n
                        );
                }
                for (i = 0; i < n; ++i)
                {
                    vy.add((float)f.calculate(vx.get(i)));
                }

                ICalculable interpF = null;
                Float min = left, max = right;
                Float length = max - min;
                //setCalculable(interpF, vx, vy, min, max);
                switch (((RadioGroup) findViewById(R.id.polynomial_spline_group)).getCheckedRadioButtonId())
                {
                    case R.id.polynomial:
                        interpF = new LagrangePolynomial(vx, vy);
                        break;
                    default:
                        sortData(vx, vy);
                        double leftBoundaryValue;
                        double rightBoundaryValue;
                        if (((CheckBox) findViewById(R.id.native_values_for_spline_check_box)).isChecked())
                        {
                            if (((RadioButton) findViewById(R.id.first_derivative)).isChecked())
                            {
                                leftBoundaryValue = f.derivative().calculate(left);
                                rightBoundaryValue = f.derivative().calculate(right);
                            }
                            else
                            {
                                leftBoundaryValue = f.derivative().derivative().calculate(left);
                                rightBoundaryValue = f.derivative().derivative().calculate(right);
                            }
                        }
                        else
                        {
                            leftBoundaryValue = Double.parseDouble(((EditText) findViewById(R.id.left_boundary_value)).getText().toString());
                            rightBoundaryValue = Double.parseDouble(((EditText) findViewById(R.id.right_boundary_value)).getText().toString());
                        }
                        min -= 0.05f * length;
                        max += 0.05f * length;
                        interpF = new Spline(
                                ((RadioGroup) findViewById(R.id.first_second_derivative_group)).getCheckedRadioButtonId() == R.id.first_derivative ? Spline.Type.FIRST : Spline.Type.SECOND,
                                leftBoundaryValue,
                                rightBoundaryValue,
                                vx, vy
                        );
                }

                ArrayList<Float> color = randColor();
                Model.addGraphic(interpF, min, max, color);
                Model.addPointCollection(vx, vy, color);
            }
        });

        ((RadioButton) findViewById(R.id.spline)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.first_derivative).setEnabled(isChecked);
                findViewById(R.id.second_derivative).setEnabled(isChecked);
                findViewById(R.id.right_boundary_value).setEnabled(isChecked);
                findViewById(R.id.left_boundary_value).setEnabled(isChecked);
                findViewById(R.id.native_values_for_spline_check_box).setEnabled(isChecked);
            }
        });

    }

    @Override
    public void rescalingEvent(OpenGLView v)
    {
        int i;
        int n = v.getCountInterval() + 1;
        int height = v.getHeight() / n;
        int width = v.getWidth() / n;

        float[] xValues = new float[n];
        float[] yValues = new float[n];

        float stepX = (Model.Right - Model.Left) / (n - 1);
        float stepY = (Model.Max - Model.Min) / (n - 1);

        for (i = 0; i < n; ++i)
        {
            xValues[i] = Model.Left + i * stepX;
            yValues[i] = Model.Min + i * stepY;
        }

        LinearLayout yLayout = (LinearLayout) findViewById(R.id.verticalAxis);
        LinearLayout xLayout = (LinearLayout) findViewById(R.id.horizontalAxis);
        TextView tv;
        if (yLayout.getChildCount() > 0)
        {
            yLayout.removeAllViews();
            xLayout.removeAllViews();
        }
        yLayout.setMinimumHeight(v.getHeight());
        xLayout.setMinimumWidth(v.getWidth());

        for (i = 0; i < n; ++i)
        {
            tv = new TextView(this);
            tv.setText(String.valueOf(yValues[n - i - 1]));
            tv.setHeight(height);
            yLayout.addView(tv);
        }

        tv = new TextView(this);
        tv.setText("");
        tv.setWidth(yLayout.getWidth());
        xLayout.addView(tv);

        for(i = 0; i < n; ++i)
        {
            tv = new TextView(this);
            tv.setText(String.valueOf(xValues[i]));
            tv.setWidth(width);
            xLayout.addView(tv);
        }
    }

    /*private void setCalculable(ICalculable f, ArrayList<Float> vx, ArrayList<Float> vy, Float min, Float max)
    {
        float length = max - min;
        switch (((RadioGroup) findViewById(R.id.polynomial_spline_group)).getCheckedRadioButtonId())
        {
            case R.id.polynomial:
                f = new LagrangePolynomial(vx, vy);
                break;
            default:
                sortData(vx, vy);
                min -= 0.05f * length;
                max += 0.05f * length;
                f = new Spline(
                        ((RadioGroup) findViewById(R.id.first_second_derivative_group)).getCheckedRadioButtonId() == R.id.first_derivative ? Spline.Type.FIRST : Spline.Type.SECOND,
                        Double.parseDouble(((EditText) findViewById(R.id.left_boundary_value)).getText().toString()),
                        Double.parseDouble(((EditText) findViewById(R.id.right_boundary_value)).getText().toString()),
                        vx, vy
                );
        }
    }*/

    private ArrayList<Float> getEquidistantNodes(float a, float b, int n)
    {
        ArrayList<Float> ret = new ArrayList<>();
        int i;
        for (i = 0; i < n; ++i)
        {
            ret.add(a + (b - a) * (float) i / (n - 1));
        }
        return ret;
    }

    private ArrayList<Float> getChebyshevNodes(float a, float b, int n)
    {
        ArrayList<Float> ret = new ArrayList<>();
        int i;
        for (i = 0; i < n; ++i)
        {
            ret.add(0.5f * (a + b) - 0.5f * (b - a) * (float) Math.cos((2.0 * i + 1.0) * Math.PI / (2.0 * n)));
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static void sortData(ArrayList<Float> vx, ArrayList<Float> vy)
    {
        int i, j, n = vx.size(), minNum;
        float temp;

        for (i = 0; i < n; ++i)
        {
            minNum = i;
            for (j = i + 1; j < n; ++j)
            {
                if (vx.get(j) < vx.get(minNum))
                {
                    minNum = j;
                }
            }
            temp = vx.get(i);
            vx.set(i, vx.get(minNum));
            vx.set(minNum, temp);

            temp = vy.get(i);
            vy.set(i, vy.get(minNum));
            vy.set(minNum, temp);
        }
    }

    private static void copyContent()
    {
        int i, n = used.size();

        for (i = 0; i < n; ++i)
        {
            unUsed.add(used.get(i));
        }
        used.clear();
    }

    private static double getMin(ArrayList<Float> m)
    {
        int i, n = m.size();
        double min = m.get(0);
        for (i = 1; i < n; ++i)
        {
            if (m.get(i) < min) min = m.get(i);
        }
        return min;
    }

    private static double getMax(ArrayList<Float> m)
    {
        int i, n = m.size();
        double max = m.get(0);
        for (i = 1; i < n; ++i)
        {
            if (m.get(i) > max) max = m.get(i);
        }
        //Toast.makeText(this, "max = " + String.valueOf(max), Toast.LENGTH_SHORT).show();
        return max;
    }

    private static double myRand()
    {
        double ret = Math.random() * 4.0 + 1.0;
        return ((int) (ret * 100)) / 100.0;
    }

    private static ArrayList<Float> randColor()
    {
        if (unUsed.size() == 0)
        {
            copyContent();
        }
        int i = (int) (Math.random() * unUsed.size()), j , n;
        if (i == unUsed.size()) --i;
        ArrayList<Float> ret = unUsed.get(i);
        used.add(ret);
        unUsed.remove(i);
        return ret;
    }

    private void setTabHost()
    {
        TabHost th = (TabHost) findViewById(android.R.id.tabhost);
        th.setup();
        TabHost.TabSpec ts = th.newTabSpec("tag1");

        ts.setIndicator("Interpolation");
        ts.setContent(R.id.Interpolation);
        th.addTab(ts);

        ts = th.newTabSpec("tag2");

        ts.setIndicator("Approximation");
        ts.setContent(R.id.Approximation);
        th.addTab(ts);

        ts = th.newTabSpec("tag3");

        ts.setIndicator("Settings");
        ts.setContent(R.id.Settings);
        th.addTab(ts);
    }

    private Double testFunction(Double x)
    {
        return x * x;
    }

    //private LayoutInflater ltInflater;
    //private LinearLayout point_list;
    private EditText numPoints;
    private static MyAdapter myAdapter = null;
    //private NumberPicker numPoints;
    private ListView myList;
    private OpenGLView glView;
    //private static Float[][] colors = {{1.0f, 0.75f, 0.0f}, {0.0f, 0.55f, 0.71f}, {0.0f, 0.49f, 1.0f}, {0.004f, 0.196f, 0.125f}, {0.0f, 0.0f, 0.54f}, {1.0f, 0.0f, 0.0f}};
    private static ArrayList<ArrayList<Float> > unUsed = new ArrayList<>();
    private static ArrayList<ArrayList<Float> > used = new ArrayList<>();

    static
    {
        ArrayList<Float> lst;
        lst = new ArrayList<>(); lst.add(1.0f); lst.add(0.0f); lst.add(0.0f); unUsed.add(lst);
        lst = new ArrayList<>(); lst.add(0.13f); lst.add(0.54f); lst.add(0.13f); unUsed.add(lst);
        lst = new ArrayList<>(); lst.add(0.0f); lst.add(0.0f); lst.add(1.0f); unUsed.add(lst);
        lst = new ArrayList<>(); lst.add(1.0f); lst.add(0.75f); lst.add(0.0f); unUsed.add(lst);
        lst = new ArrayList<>(); lst.add(0.6f); lst.add(0.4f); lst.add(0.8f); unUsed.add(lst);
        lst = new ArrayList<>(); lst.add(0.0f); lst.add(0.0f); lst.add(0.0f); unUsed.add(lst);
    }
}
