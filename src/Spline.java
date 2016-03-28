package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;

/**
 * Created by Mixon on 30.04.2015.
 */
public class Spline implements ICalculable
{
    public enum Type {FIRST, SECOND}

    ArrayList<Polynomial> Sequence;
    ArrayList<Double> Nodes;

    public Spline(Type type, double leftCondition, double rightCondition, ArrayList<Float> args, ArrayList<Float> values)
    {
        if (args.size() != values.size())
        {
            throw new RuntimeException("Different sizes of arguments!");
        }
        Nodes = new ArrayList<>(args.size());
        for (Float t: args)
        {
            Nodes.add((double) t);
        }

        double nu0, nun, mu0, mun;

        if (type == Type.FIRST)
        {
            nu0 = 0.5;
            nun = 0.5;
            mu0 = -3.0 * (leftCondition * (args.get(1) - args.get(0)) - 
			                              (values.get(1) - values.get(0)))
										  / ((args.get(1) - args.get(0)) 
										  * (args.get(1) - args.get(0)));
            mun = 3.0 * (rightCondition * (args.get(args.size() - 1) - 
			                               args.get(args.size() - 2)) - 
										  (values.get(values.size() - 1) - 
										   values.get(values.size() - 2))) /
										   ((args.get(args.size() - 1) - 
										   args.get(args.size() - 2)) * 
										   (args.get(args.size() - 1) - 
										   args.get(args.size() - 2)));
        }

        else
        {
            nu0 = nun = 0;
            mu0 = leftCondition;
            mun = rightCondition;
        }

        ArrayList<Double> M = Shuttle(args, values, nu0, nun, mu0, mun);

        double[] c = new double[6];
        int i, n = args.size() - 1;
        Sequence = new ArrayList<>(n);
        ArrayList<Double> f = new ArrayList<>(4);

        for (i = 0; i < 4; ++i)
        {
            f.add(0.0);
        }

        for (i = 0; i < n; ++i)
        {
            c[0] = values.get(i) - args.get(i) * 
			(values.get(i + 1) - values.get(i)) / (args.get(i + 1) - 
			args.get(i));
            c[1] = (values.get(i + 1) - values.get(i)) / 
			(args.get(i + 1) - args.get(i));
            c[2] = -(args.get(i + 1) + args.get(i));
            c[3] = args.get(i + 1) * args.get(i);
            c[4] = (1.0 / 6.0) * (M.get(i + 1) - M.get(i)) / 
			       (args.get(i + 1) - args.get(i));
            c[5] = 0.5 * M.get(i) + c[4] * 
			(args.get(i + 1) - 2.0 * args.get(i));

            f.set(0, c[0] + c[3] * c[5]);
            f.set(1, c[1] + c[2] * c[5] + c[3] * c[4]);
            f.set(2, c[5] + c[2] * c[4]);
            f.set(3, c[4]);

            Sequence.add(new Polynomial(f));
        }
    }

    @Override
    public double calculate(double valueOfX) {
        int i, n = Nodes.size() - 1;
        for (i = 1; i < n; ++i)
        {
            if (valueOfX < Nodes.get(i)) break;
        }
        return Sequence.get(i - 1).calculate(valueOfX);
    }

    private static ArrayList<Double> Shuttle(ArrayList<Float> args, 
	                                         ArrayList<Float> values, 
											 double nu0, 
											 double nun, 
											 double mu0, 
											 double mun)
    {
        int i, n = args.size() - 1;

        ArrayList<Double> X = new ArrayList<>(n + 1);

        double[] alpha = new double[n];
        double[] beta = new double[n];
        double C_i, A_i_1, B_i, F_i, denom;

        alpha[0] = -nu0;
        beta[0] = mu0;

        C_i = args.get(1) - args.get(0);

        for (i = 1; i < n; ++i)
        {
            A_i_1 = C_i;
            C_i = args.get(i + 1) - args.get(i);
            B_i = 2 * (args.get(i + 1) - args.get(i - 1));
            F_i = 6 * ((values.get(i + 1) - values.get(i)) / 
			           (args.get(i + 1) - args.get(i)) - 
					   (values.get(i) - values.get(i - 1)) / 
					   (args.get(i) - args.get(i - 1)));
            denom = alpha[i - 1] * A_i_1 + B_i;
            alpha[i] = -C_i / denom;
            beta[i] = (F_i - A_i_1 * beta[i - 1]) / denom;
        }

        X.add((mun - nun * beta[n - 1]) / (nun * alpha[n - 1] + 1));

        for (i = n - 1; i >= 0; --i)
        {
            X.add(0, alpha[i] * X.get(0) + beta[i]);
        }

        return X;
    }
}
