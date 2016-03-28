package com.example.mixon.lagrangepolynomes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mixon on 06.04.2015.
 */
public class Parser
{
    public static TreeNode Parse(String s)
    {
        str = s;
        index = 0;
        getToken();
        return Expression();
    }

    private static TreeNode Expression()
    {
        TreeNode res = Term();
        TreeNode temp;
        TokenType type;

        while (currentTokenCode == TokenType.PLUS || currentTokenCode == TokenType.MINUS)
        {
            type = currentTokenCode;
            getToken();

            temp = res;

            if (type == TokenType.PLUS)
                res = new TreeNode(TreeNode.Type.ADD, 0);
            else
                res = new TreeNode(TreeNode.Type.SUB, 0);
            res.add(temp);
            res.add(Term());
        }

        return res;
    }

    private static TreeNode Term()
    {
        TreeNode res = Exponentiation();
        TokenType type;
        TreeNode temp;

        while (currentTokenCode == TokenType.ASTERISK || currentTokenCode == TokenType.SLASH)
        {
            type = currentTokenCode;
            getToken();

            temp = res;

            if (type == TokenType.ASTERISK)
            {
                res = new TreeNode(TreeNode.Type.MUL, 0);
            }
            else
            {
                res = new TreeNode(TreeNode.Type.DIV, 0);
            }
            res.add(temp);
            res.add(Exponentiation());
        }
        return res;
    }

    private static TreeNode Exponentiation()
    {
        TreeNode res = PreUnary();
        TreeNode temp = null;

        while (currentTokenCode == TokenType.CIRCUMFLEX)
        {
            getToken();

            temp = res;
            res = new TreeNode(TreeNode.Type.POW, 0);
            res.add(temp);
            res.add(PreUnary());
        }
        return res;
    }

    private static TreeNode PreUnary()
    {
        boolean isNegative = false;

        if (currentTokenCode == TokenType.PLUS || currentTokenCode == TokenType.MINUS)
        {
            if (currentTokenCode == TokenType.MINUS)
                isNegative = true;
            getToken();
        }
        TreeNode res = Factor();
        if (isNegative)
        {
            TreeNode temp = res;
            res = new TreeNode(TreeNode.Type.SUB, 0);
            res.add(new TreeNode(TreeNode.Type.NUMBER, 0));
            res.add(temp);
        }
        return res;
    }

    private static TreeNode Factor()
    {
        TreeNode res;
        int prevPosition;
        String prevToken;
        if (currentTokenCode == TokenType.OPEN_BRACKET)
        {
            getToken();
            res = Expression();
            if (currentTokenCode != TokenType.CLOSE_BRACKET)
            {
                throw new RuntimeException("\')\' expected before " + currentToken);
            }
        }
		
        else if(currentTokenCode == TokenType.NUMBER)
        {
            res = Number();
        }
        else if (currentTokenCode == TokenType.ID)
        {
            prevPosition = index;
            prevToken = currentToken;

            getToken();
            if (currentTokenCode == TokenType.OPEN_BRACKET)
            {
                res = FunctionCall(prevToken);
            }
            else
            {
                index = prevPosition;
                if (prevToken.equals("x"))
                {
                    res = new TreeNode(TreeNode.Type.VARIABLE, 0);
                }
                else
                {
                    throw new RuntimeException("Incorrect name of variable \'" + prevToken + "\'");
                }
            }
        }
        else
        {
            throw new RuntimeException("Incorrect token \'" + currentToken + "\'");
        }

        getToken();
        return res;
    }

    private static TreeNode FunctionCall(String name)
    {
        ArrayList<TreeNode> args = ArgList();
        if (currentTokenCode != TokenType.CLOSE_BRACKET)
        {
            throw new RuntimeException("\')\' expected before " + currentToken);
        }
        TreeNode res = new TreeNode(NameAssociation.get(name), 0);
        res.add(args);
        return res;
    }

    private static ArrayList<TreeNode> ArgList()
    {
        ArrayList<TreeNode> res = new ArrayList<>();
        do
        {
            getToken();
            res.add(Expression());
        }
        while(currentTokenCode == TokenType.COMMA);
        return res;
    }

    private static TreeNode Number()
    {
        double result = 0.0;
        double mul = 0.1;
        int idx = 0;
        while (idx < currentToken.length() && Character.isDigit(currentToken.charAt(idx)))
        {
            result = result * 10.0 + (double)(currentToken.charAt(idx) - '0');
            ++idx;
        }
        if (idx < currentToken.length() && currentToken.charAt(idx) == '.')
        {
            ++idx;
            while (idx < currentToken.length() && Character.isDigit(currentToken.charAt(idx)))
            {
                result += (double)(currentToken.charAt(idx) - '0') * mul;
                mul *= 0.1;
                ++idx;
            }
        }
        return new TreeNode(TreeNode.Type.NUMBER, result);
    }

    private static void getToken()
    {
        currentToken = "";
        currentTokenCode = TokenType.NONE;

        if (index == str.length()) return;

        while (index < str.length() && Character.isWhitespace(str.charAt(index)))
        {
            ++index;
        }

        if (index == str.length()) return;

        if (IsDelim(str.charAt(index)))
        {
            currentToken += str.charAt(index);
            ++index;
            currentTokenCode = TokenAssociation.get(currentToken);
        }
        else if (Character.isLetter(str.charAt(index)))
        {
            while (index < str.length() && !IsDelim(str.charAt(index)))
            {
                currentToken += str.charAt(index);
                ++index;
            }
            currentTokenCode = TokenType.ID;
        }
        else if (Character.isDigit(str.charAt(index)))
        {
            while (index < str.length() && !IsDelim(str.charAt(index)))
            {
                currentToken += str.charAt(index);
                ++index;
            }
            currentTokenCode = TokenType.NUMBER;
        }
    }

    private static boolean IsDelim(char c)
    {
        return delimiters.indexOf(c) != -1;
    }


    private enum TokenType {NONE, OPEN_BRACKET, CLOSE_BRACKET, PLUS, MINUS, ASTERISK, SLASH, CIRCUMFLEX, COMMA, NUMBER, ID};
    private static HashMap<String, TokenType> TokenAssociation = new HashMap<>();
    private static HashMap<String, TreeNode.Type> NameAssociation = new HashMap<>();
    private static int index;
    private static TokenType currentTokenCode;
    private static String currentToken;
    private static String str;
    private static String delimiters = " +-*/^(),";

    static
    {
        TokenAssociation.put("+", TokenType.PLUS);
        TokenAssociation.put("-", TokenType.MINUS);
        TokenAssociation.put("*", TokenType.ASTERISK);
        TokenAssociation.put("/", TokenType.SLASH);
        TokenAssociation.put("^", TokenType.CIRCUMFLEX);
        TokenAssociation.put(",", TokenType.COMMA);
        TokenAssociation.put("(", TokenType.OPEN_BRACKET);
        TokenAssociation.put(")", TokenType.CLOSE_BRACKET);

        NameAssociation.put("sin", TreeNode.Type.SIN);
        NameAssociation.put("cos", TreeNode.Type.COS);
        NameAssociation.put("exp", TreeNode.Type.EXP);
        NameAssociation.put("ln", TreeNode.Type.LN);
    }
}
