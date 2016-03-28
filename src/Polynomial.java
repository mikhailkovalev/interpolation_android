package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;

/**
 * Created by Mixon on 30.04.2015.
 */
public class Polynomial implements ICalculable
{
    ArrayList<Double> factor;

    public Polynomial(ArrayList<Double> f)
    {
        factor = new ArrayList<>(f.size());
        for(Double c: f)
        {
            factor.add(c);
        }
    }

    @Override
    public double calculate(double valueOfX)
    {
        double res = 0.0;
        double f = 1.0 / valueOfX;
        for (Double t: factor)
        {
            res += t * (f *= valueOfX);
        }
        return res;
    }
}
