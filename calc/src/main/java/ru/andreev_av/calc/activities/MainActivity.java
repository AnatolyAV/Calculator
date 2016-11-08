package ru.andreev_av.calc.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ru.andreev_av.calc.exceptions.ExpressionException;
import ru.andreev_av.calc.exceptions.DivisionByZeroException;

import java.util.Stack;

import ru.andreev_av.calc.R;
import ru.andreev_av.calc.calculations.CalcExpressions;
import ru.andreev_av.calc.enums.ActionType;

public class MainActivity extends Activity {

    private EditText txtResult;

    private Button btnAdd;
    private Button btnDivide;
    private Button btnMultiply;
    private Button btnSubtract;
    private Button btnPercent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = (EditText) findViewById(R.id.txtResult);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDivide = (Button) findViewById(R.id.btnDivide);
        btnMultiply = (Button) findViewById(R.id.btnMultiply);
        btnSubtract = (Button) findViewById(R.id.btnSubtract);
        btnPercent = (Button) findViewById(R.id.btnPercent);

        actionTypeStack = (Stack<ActionType>) getLastNonConfigurationInstance();
        if (actionTypeStack==null){
            actionTypeStack=new Stack<ActionType>();
        }else{
            if (!actionTypeStack.isEmpty())
                lastAction=actionTypeStack.pop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    private void showToastMessage(int messageId) {
        Toast toastMessage = Toast.makeText(this, messageId, Toast.LENGTH_LONG);
        toastMessage.setGravity(Gravity.TOP, 0, 100);
        toastMessage.show();
    }
    public Object onRetainNonConfigurationInstance() {
        return actionTypeStack;
    }


    private ActionType lastAction;
    private Stack<ActionType> actionTypeStack = new Stack<ActionType>();

    private ActionType actionTypeTmp;
    private Stack<ActionType> actionTypeTmpStack=new Stack<ActionType>();

    public void buttonClick(View view) {

        switch (view.getId()) {
            case R.id.btnAdd:
            case R.id.btnSubtract:
            case R.id.btnDivide:
            case R.id.btnMultiply:
            case R.id.btnPercent: {

                if (((lastAction == null || lastAction == ActionType.CALCULATION) && view.getId() != R.id.btnSubtract) // первой не может быть операция +,*,/,%, кроме -
                        || (lastAction == ActionType.SUBSTRACT && view.getId() != R.id.btnSubtract) // после операции - запрещаем вводить +,*,/,% (упростил логику, можно было анализировать к примеру: (-*, *-+, не стал)
                        || lastAction == ActionType.POINT // после "." запрещаем вводить -,+,*,/,%
                        || (lastAction == ActionType.OPEN_BRACKET && view.getId() != R.id.btnSubtract)) {// после "(" запрещаем вводить +,*,/,%
                    break;
                }
                else if ((lastAction == ActionType.SUBSTRACT && view.getId() == R.id.btnSubtract) // если предыдущая операция - и текущая -, то удаляем предыдущую
                        || (lastAction == ActionType.OPERATION && view.getId() != R.id.btnSubtract)) { // если предыдущая операция +,*,/,% и текущая +,*,/,% , то удаляем предыдущую
                    txtResult.setText(txtResult.getText().delete(
                            txtResult.getText().length() - 1,
                            txtResult.getText().length()) + view.getContentDescription().toString());
                }
                else {
                    if (lastAction != ActionType.CALCULATION)
                        txtResult.setText(txtResult.getText() + view.getContentDescription().toString());
                    else
                        txtResult.setText(view.getContentDescription().toString());
                }

                if (view.getId() != R.id.btnSubtract)
                    lastAction = ActionType.OPERATION;
                else
                    lastAction = ActionType.SUBSTRACT;

                actionTypeStack.push(lastAction);
                break;
            }

            case R.id.btnClear: {
                clearResult();
                break;
            }

            case R.id.btnResult: {
                if (lastAction == null || lastAction == ActionType.CALCULATION) return;

                double result = 0;

                try {
                    result = CalcExpressions.calc(txtResult.getText().toString());

                } catch (DivisionByZeroException e) {
                    showToastMessage(R.string.division_zero);
                    return;
                }catch (ExpressionException e) {
                    showToastMessage(R.string.expression_wrong);
                    return;
                }catch (RuntimeException e) {
                    showToastMessage(R.string.expression_error);
                    return;
                }

                if (result % 1 == 0)
                    txtResult.setText(String.valueOf((int) result));// отсекать нули после точки
                 else
                    txtResult.setText(String.valueOf(result));

                lastAction = ActionType.CALCULATION;
                actionTypeStack.clear();
                actionTypeStack.add(lastAction);
                break;
            }

            case R.id.btnPoint: {
                if (lastAction == null || lastAction == ActionType.CALCULATION) return;

                actionTypeTmpStack.addAll(actionTypeStack);

                if (lastAction == ActionType.DIGIT) {
                    while (!actionTypeTmpStack.isEmpty()){
                        actionTypeTmp=actionTypeTmpStack.pop();
                        if (actionTypeTmp == ActionType.POINT){
                            actionTypeTmpStack.clear();
                            return;
                        }

                        if (actionTypeTmp != ActionType.DIGIT)
                            break;
                    }
                    actionTypeTmpStack.clear();
                    txtResult.setText(txtResult.getText() + ".");
                    lastAction = ActionType.POINT;
                    actionTypeStack.push(lastAction);
                }
                break;
            }

            case R.id.btnDelete: {

                if (txtResult.getText().toString().trim().length() == 0 || lastAction == null || lastAction == ActionType.CALCULATION) {
                    clearResult();
                    break;
                }

                txtResult.setText(txtResult.getText().delete(
                        txtResult.getText().length() - 1,
                        txtResult.getText().length()));

                if (!actionTypeStack.isEmpty())
                    actionTypeStack.pop();
                if (!actionTypeStack.isEmpty())
                    lastAction=actionTypeStack.peek();
                else
                    lastAction=null;
                break;
            }
            case R.id.btnOpenBracket: {

                if ( lastAction==null || lastAction==ActionType.CALCULATION ||  lastAction==ActionType.SUBSTRACT || lastAction==ActionType.OPERATION || lastAction == ActionType.OPEN_BRACKET){
                    if (lastAction==ActionType.CALCULATION)
                        txtResult.setText(view.getContentDescription().toString());
                    else
                        txtResult.setText(txtResult.getText() + view.getContentDescription().toString());
                    lastAction = ActionType.OPEN_BRACKET;
                    actionTypeStack.push(lastAction);
                }
                break;
            }
            case R.id.btnCloseBracket: {

                if ((lastAction==ActionType.DIGIT || lastAction == ActionType.CLOSE_BRACKET)&& lastAction!=ActionType.CALCULATION){
                    txtResult.setText(txtResult.getText() + view.getContentDescription().toString());
                    lastAction = ActionType.CLOSE_BRACKET;
                    actionTypeStack.push(lastAction);
                }

                break;
            }
            default: {
                if (txtResult.getText().toString().equals("0") || lastAction == ActionType.CALCULATION) {
                    txtResult.setText(view.getContentDescription().toString());
                }
                else if ( lastAction == ActionType.DIGIT && txtResult.getText().toString().charAt(txtResult.getText().length()-1)=='0') {// условие для проверки на ввод некорректных цифр, начинающихся с 0. Например: 056 меняем на 56
                    actionTypeTmpStack.addAll(actionTypeStack);
                    actionTypeTmpStack.pop();//вытаскиваем 0
                    actionTypeTmp = actionTypeTmpStack.pop();
                    if (actionTypeTmp != ActionType.DIGIT && actionTypeTmp != ActionType.POINT)
                        txtResult.setText(txtResult.getText().delete(
                                txtResult.getText().length() - 1,
                                txtResult.getText().length()) + view.getContentDescription().toString());
                    else
                        txtResult.setText(txtResult.getText()
                                + view.getContentDescription().toString());
                    actionTypeTmpStack.clear();
                }else {
                    txtResult.setText(txtResult.getText()
                            + view.getContentDescription().toString());
                }

                lastAction = ActionType.DIGIT;
                actionTypeStack.push(lastAction);

            }


        }
    }
    private void clearResult(){
        txtResult.setText("");
        lastAction = null;
        actionTypeStack.clear();
    }
}
