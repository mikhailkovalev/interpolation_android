package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;

public class Model
{
    public enum State {NONE, ADD, STATIC, REMOVE}

    public static ArrayList<Graphic> graphicList = new ArrayList<>();
    public static ArrayList<PointCollection> pointCollectionList = new ArrayList<>();
    public static float Left, Right, Min, Max;
    public static State state = State.NONE;

    public static void addGraphic(ICalculable f, float left, float right, ArrayList<Float> color)
    {
        Graphic g = new Graphic();
        g.f = f;
        g.leftBorder = left;
        g.rightBorder = right;
        g.color = color;

        if (state == State.NONE)
        {
            Left = left;
            Right = right;
            Min = Max = (float) f.calculate(left);
        }
        else
        {
            if (left < Left) Left = left;
            if (right > Right) Right = right;
        }

        state = State.ADD;
        graphicList.add(g);
    }

    public static void addPointCollection(ArrayList<Float> absciss, ArrayList<Float> ordinat, ArrayList<Float> color)
    {
        PointCollection p = new PointCollection();
        p.absciss = absciss;
        p.ordinat = ordinat;
        p.color = color;
        pointCollectionList.add(p);
    }

    public static void clear()
    {
        graphicList.clear();
        pointCollectionList.clear();
        state = State.REMOVE;
    }
}