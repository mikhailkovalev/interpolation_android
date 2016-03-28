package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mixon on 06.04.2015.
 */
public class TreeNode implements ICalculable
{
    public TreeNode(Type t, double c)
    {
        type = t;
        content = c;
        child = new ArrayList<TreeNode>();
    }

    public void add(TreeNode t)
    {
        child.add(t);
    }

    public void add(ArrayList<TreeNode> lst)
    {
        int i = 0, c = lst.size();
        for (; i < c; ++i)
        {
            child.add(lst.get(i));
        }
    }

    public double calculate(double valueOfX)
    {
        if (type == Type.NUMBER) return content;
        if (type == Type.VARIABLE) return valueOfX;
        IFunction f = FunctionAssociation.get(type);
        return f.f(child, valueOfX);
    }

    public TreeNode derivative()
    {
        if (type == Type.NUMBER) return new TreeNode(Type.NUMBER, 0);
        if (type == Type.VARIABLE) return new TreeNode(Type.NUMBER, 1);
        IFunction f = FunctionAssociation.get(type);
        return f.d(child);
    }

    public enum Type {NUMBER, VARIABLE, ADD, SUB, MUL, DIV, POW, SIN, COS, EXP, LN};

    private Type type;
    private double content;
    private ArrayList<TreeNode> child;
    private static HashMap<Type, IFunction> FunctionAssociation = new HashMap<>();

    static
    {
        FunctionAssociation.put(Type.ADD, new IFunction()
        {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX)
            {
                if (args.size() != 2)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'+\'");
                }
                return args.get(0).calculate(valueOfX) + args.get(1).calculate(valueOfX);
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 2) throw new RuntimeException("Incorrect number of arguments for \'+\'");
                TreeNode res = new TreeNode(Type.ADD, 0);
                res.add(args.get(0).derivative());
                res.add(args.get(1).derivative());
                return res;
            }
        });
        FunctionAssociation.put(Type.SUB, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 2)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'-\'");
                }
                return args.get(0).calculate(valueOfX) - args.get(1).calculate(valueOfX);
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 2) throw new RuntimeException("Incorrect number of arguments for \'-\'");
                TreeNode res = new TreeNode(Type.SUB, 0);
                res.add(args.get(0).derivative());
                res.add(args.get(1).derivative());
                return res;
            }
        });
        FunctionAssociation.put(Type.MUL, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 2)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'*\'");
                }
                return args.get(0).calculate(valueOfX) * args.get(1).calculate(valueOfX);
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 2) throw new RuntimeException("Incorrect number of arguments for \'*\'");
                TreeNode res = new TreeNode(Type.ADD, 0);
                TreeNode child1 = new TreeNode(Type.MUL, 0);
                TreeNode child2 = new TreeNode(Type.MUL, 0);

                child1.add(args.get(0).derivative());
                child1.add(args.get(1));

                child2.add(args.get(0));
                child2.add(args.get(1).derivative());

                res.add(child1);
                res.add(child2);

                return res;
            }
        });
        FunctionAssociation.put(Type.DIV, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 2)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'/\'");
                }
                return args.get(0).calculate(valueOfX) / args.get(1).calculate(valueOfX);
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 2) throw new RuntimeException("Incorrect number of arguments for \'/\'");
                TreeNode res = new TreeNode(Type.DIV, 0);
                TreeNode child1 = new TreeNode(Type.SUB, 0);
                TreeNode child2 = new TreeNode(Type.MUL, 0);
                TreeNode child11 = new TreeNode(Type.MUL, 0);
                TreeNode child12 = new TreeNode(Type.MUL, 0);

                child11.add(args.get(0).derivative());
                child11.add(args.get(1));

                child12.add(args.get(0));
                child12.add(args.get(1).derivative());

                child1.add(child11);
                child1.add(child12);

                child2.add(args.get(1));
                child2.add(args.get(1));

                res.add(child1);
                res.add(child2);

                return res;
            }
        });
        FunctionAssociation.put(Type.POW, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 2)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'^\'");
                }
                return Math.pow(args.get(0).calculate(valueOfX), args.get(1).calculate(valueOfX));
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                TreeNode res = new TreeNode(Type.MUL, 0);
                TreeNode child1 = new TreeNode(Type.POW, 0);
                TreeNode child2 = new TreeNode(Type.ADD, 0);
                TreeNode child21 = new TreeNode(Type.MUL, 0);
                TreeNode child22 = new TreeNode(Type.DIV, 0);
                TreeNode child211 = new TreeNode(Type.LN, 0);
                TreeNode child221 = new TreeNode(Type.MUL, 0);

                child1.add(args);

                child211.add(args.get(0));

                child221.add(args.get(1));
                child221.add(args.get(0).derivative());

                child21.add(child211);
                child21.add(args.get(1).derivative());

                child22.add(child221);
                child22.add(args.get(0));

                child2.add(child21);
                child2.add(child22);

                res.add(child1);
                res.add(child2);

                return res;
            }
        });
        FunctionAssociation.put(Type.SIN, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 1)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'sin\'");
                }
                return Math.sin(args.get(0).calculate(valueOfX));
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 1) throw new RuntimeException("Incorrect number of arguments for \'sin\'");
                TreeNode res = new TreeNode(Type.MUL, 0);
                TreeNode child1 = new TreeNode(Type.COS, 0);

                child1.add(args.get(0));

                res.add(child1);
                res.add(args.get(0).derivative());

                return res;
            }
        });
        FunctionAssociation.put(Type.COS, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 1)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'cos\'");
                }
                return Math.cos(args.get(0).calculate(valueOfX));
            }

            @Override
            public  TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 1) throw new RuntimeException("Incorrect number of arguments for \'cos\'");
                TreeNode res = new TreeNode(Type.SUB, 0);
                TreeNode child1 = new TreeNode(Type.NUMBER, 0);
                TreeNode child2 = new TreeNode(Type.MUL, 0);
                TreeNode child21 = new TreeNode(Type.SIN, 0);

                child21.add(args.get(0));

                child2.add(child21);
                child2.add(args.get(0).derivative());

                res.add(child1);
                res.add(child2);

                return res;
            }
        });
        FunctionAssociation.put(Type.EXP, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 1)
                {
                    throw new RuntimeException("Incorrect number of arguments for \'exp\'");
                }
                return Math.exp(args.get(0).calculate(valueOfX));
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args)
            {
                if (args.size() != 1) throw new RuntimeException("Incorrect number of arguments for \'exp\'");
                TreeNode res = new TreeNode(Type.MUL, 0);
                TreeNode child1 = new TreeNode(Type.EXP, 0);

                child1.add(args.get(0));

                res.add(child1);
                res.add(args.get(0).derivative());

                return res;
            }
        });
        FunctionAssociation.put(Type.LN, new IFunction() {
            @Override
            public double f(ArrayList<TreeNode> args, double valueOfX) {
                if (args.size() != 1) throw new RuntimeException("Incorrect number of arguments for \'ln\'");
                return Math.log(args.get(0).calculate(valueOfX));
            }

            @Override
            public TreeNode d(ArrayList<TreeNode> args) {
                if (args.size() != 1) throw new RuntimeException("Incorrect number of arguments for \'ln\'");
                TreeNode res = new TreeNode(Type.DIV, 0);
                res.add(args.get(0).derivative());
                res.add(args.get(0));
                return res;
            }
        });
    }
}
