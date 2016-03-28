package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;

/**
 * Created by Mixon on 06.04.2015.
 */
public interface IFunction
{
    double f(ArrayList<TreeNode> args, double valueOfX);
    TreeNode d(ArrayList<TreeNode> args);
}
