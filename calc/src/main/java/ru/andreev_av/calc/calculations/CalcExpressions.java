package ru.andreev_av.calc.calculations;

import java.util.Stack;

import ru.andreev_av.calc.exceptions.ExpressionException;
import ru.andreev_av.calc.exceptions.DivisionByZeroException;
/**
 * Created by Tolik on 04.11.2016.
 */
// Класс для вычисления выражения методом двух стеков
public class CalcExpressions {

    private static int posToken;

    public static double calc(String expression) {

        String expressionTmp="("+expression+")";
        Stack<Double> operStack = new Stack<Double>();// Стек операндов
        Stack<Character> funcStack = new Stack<Character>();// Стек операций
        posToken=0;
        Object token;
        Object prevToken=null;
        int countArithmeticOper=0;// количество арифметических операций подряд
        boolean openingBracket=false;

        do {
            token = getToken(expressionTmp);

            if (String.valueOf(token).equals("(")){
                openingBracket=true;
            }else if (!String.valueOf(token).equals("+") && !String.valueOf(token).equals("-")
                    && !String.valueOf(token).equals("*") && !String.valueOf(token).equals("/")
                    && !String.valueOf(token).equals("%")){
                openingBracket=false;
            }

            if (countArithmeticOper==3 || (openingBracket && countArithmeticOper==2)){//проверка на три ариф. операции подряд или на открывающуюся скобку и на две ариф. операции подряд
                throw new ExpressionException();
            }
            if (String.valueOf(token).equals("+") || String.valueOf(token).equals("-")
                    || String.valueOf(token).equals("*") || String.valueOf(token).equals("/")
                    || String.valueOf(token).equals("%")){
                countArithmeticOper++;
            }else {
                countArithmeticOper=0;
            }

            if (prevToken!=null && !String.valueOf(prevToken).equals(")")&&
                    (String.valueOf(prevToken).equals("+") || String.valueOf(prevToken).equals("-")
                            || String.valueOf(prevToken).equals("*") || String.valueOf(prevToken).equals("/")
                            || String.valueOf(prevToken).equals("%") || String.valueOf(prevToken).equals("("))
                    && (String.valueOf(token).equals("-"))){//допускаем унарную операцию(Например: -5)
                operStack.push(0.0);
                funcStack.push((Character) token);
                continue;
            }

            if (token instanceof Double) {
                operStack.push((Double) token);//помещаем операнд в стек
            } else if (String.valueOf(token).equals("+") || String.valueOf(token).equals("-")
                    || String.valueOf(token).equals("*") || String.valueOf(token).equals("/")
                    || String.valueOf(token).equals("%")
                    || String.valueOf(token).equals("(") || String.valueOf(token).equals(")")) {
                if (String.valueOf(token).equals(")")) {  // ) выталкивает все операции до первой (
                    while (funcStack.size() > 0 && !String.valueOf(funcStack.peek()).equals("("))
                        popFuncStack(operStack, funcStack);
                    if (funcStack.size()==0)
                        throw new ExpressionException();
                    funcStack.pop();// удаляем (
                } else {
                    while (canPop((Character) token, funcStack))//если удается вытолкнуть,
                        popFuncStack(operStack, funcStack); //то выталкиваем
                    if (token instanceof Character) {
                        funcStack.push((Character) token);//помещаем функцию в стек

                    } else throw new ExpressionException();
                }
            }
            prevToken=token;
        }
        while(token!=null);

        if (operStack.size() != 1 || funcStack.size() > 0)
            throw new ExpressionException();
        return operStack.pop();
    }

    private static boolean canPop(Character func, Stack<Character> funcStack)
    {
        if (funcStack.size() == 0) return false;

        int pr1,pr2;

        pr1 = getPriority(func);
        pr2 = getPriority(funcStack.peek());

        if (pr1==0 || pr2==0){
            throw new ExpressionException();
        }
        return pr1 > 0 && pr2 > 0 && pr1 >= pr2;
    }

    private static int getPriority(Character func)
    {
        switch (func)
        {
            case '(':
                return -1;
            case '*':
            case '/':
            case '%':
                return 2;
            case '+':
            case '-':
                return 3;
            default:
                throw new ExpressionException();
        }
    }

    private static void popFuncStack(Stack<Double> operStack, Stack<Character> funcStack)
    {
        double x,y;
        if (operStack.size()==0 || funcStack.size()==0){
            throw new ExpressionException();
        }
        y=operStack.pop();
        x=operStack.pop();

        switch (funcStack.pop())
        {
            case '+': operStack.push(x + y);
                break;
            case '-': operStack.push(x - y);
                break;
            case '*': operStack.push(x * y);
                break;
            case '/':
                if (y==0){
                    throw new DivisionByZeroException();
                }
                operStack.push(x / y);
                break;
            case '%': operStack.push(y / 100 * x);
                break;
            default: throw new ExpressionException();
        }

    }

    private static Object getToken(String s)
    {
        if (posToken == s.length())
            return null;

        if (Character.isDigit(s.charAt(posToken)))
            return Double.parseDouble(readDouble(s));
        else
            return readFunction(s);
    }

    private static Character readFunction(String s)
    {
        return s.charAt(posToken++);
    }
    private static String readDouble(String s)
    {
        String res = "";
        while (posToken < s.length() && (Character.isDigit(s.charAt(posToken)) || s.charAt(posToken) == '.'))
            res += s.charAt(posToken++);

        return res;
    }

}
