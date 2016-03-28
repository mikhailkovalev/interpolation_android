package com.example.mixon.lagrangepolynomes;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mixon on 06.04.2015.
 */
public class LagrangePolynomial implements ICalculable
{
    LagrangePolynomial(ArrayList<Float> args, ArrayList<Float> values)
    {
        if (args.size() != values.size())
        {
            throw new RuntimeException("Different sizes of arguments!");
        }

        int i, j;
        int n = args.size();

        //String s = "";

        ArrayList<Float> a = new ArrayList<>();
        for (Float val: args)
        {
            a.add(val);
        }
        java.util.Collections.sort(a);
        for (i = 1; i < n; ++i)
        {
            if (Math.abs(a.get(i) - a.get(i - 1)) < 1e-6)
            {
                throw new RuntimeException("Values of arguments is not different!");
            }
        }

        factor = new double[n];
        nodes = new double [n];

        for (i = 0; i < n; ++i)
        {
            nodes[i] = args.get(i);
        }

        double product;

        for (i = 0; i < n; ++i)
        {
            product = 1.0;
            for (j = 0; j < n; ++j)
            {
                if (i == j) continue;
                product *= (args.get(i) - args.get(j));
            }
            factor[i] = values.get(i) / product;
            //s += String.valueOf(factor[i]) + "\n";
        }
        //Toast.makeText().show();
       //Log.d("myLog", s);
    }

    @Override
    public double calculate(double valueOfX)
    {
        double res = 0.0;
        double product;
        int n = factor.length;
        int i, j;

        for (i = 0; i < n; ++i)
        {
            product = 1.0;
            for (j = 0; j < n; ++j)
            {
                if (i == j) continue;
                product *= (valueOfX - nodes[j]);
            }
            res += factor[i] * product;
        }

        return res;
    }

    private double[] factor;
    private double[] nodes;
}
