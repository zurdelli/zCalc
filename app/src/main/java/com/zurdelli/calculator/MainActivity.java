package com.zurdelli.calculator;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    LinearLayout button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonAdd, buttonSubstract, buttonMul, buttonDiv, buttonDel;
    LinearLayout buttonEqual, buttonPercent, buttonDot, buttonAns, buttonPow;
    Button buttonM1, buttonM2, buttonM3, buttonM4;
    String result, tmp, operator, numMemory, ans, sCurrency;
    TextView textView1, currencyView, coinView;
    EditText textLine;
    Spinner spinner;
    Boolean currencyFlag = false, parentesis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControl();
        initControlListener();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menu (si es que hay una actionbar)
        getMenuInflater().inflate(R.menu.menu, menu);

        // Busca un MenuItem
        MenuItem item = menu.findItem(R.id.currency_switcher);
        item.setActionView(R.layout.switch_item);
        final ToggleButton switchAB = item.getActionView().findViewById(R.id.switcherr);

        switchAB.setChecked(false);

        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sCurrency == null){
                        Toast.makeText(MainActivity.this, "Mantener presionado para configurar", Toast.LENGTH_SHORT).show();
                        switchAB.setChecked(false);
                    } else {
                        currencyView.setVisibility(View.VISIBLE);
                        coinView.setVisibility(View.VISIBLE);
                        currencyFlag = true;
                    }

                } else {
                    currencyView.setVisibility(View.GONE);
                    coinView.setVisibility(View.GONE);
                    currencyFlag = false;
                }
            }
        });

        // si mantengo apretado abre el menu de conversor de divisas
        switchAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                layoutInflador();
                sCurrency = "1";
                switchAB.setChecked(true);
                return true;
            }
        });
        return true;
    }

    // Este metodo es para crear el menu contextual cuando se mantiene pulsada la tecla de currency
    private void layoutInflador(){
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_switch_currency,null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText currency = view.findViewById(R.id.changeValue);
        final Spinner spinner = view.findViewById(R.id.currency_spinner);

        // Creamos un ArrayAdapter usando el array hecho en values/stringsArray y el layout spinner
        // que viene por defecto
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Especificamos el layout para usar cuando la lista de opciones aparezca
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Aplicamos el adapter al spinner
        spinner.setAdapter(adapter);

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sCurrency = currency.getText().toString();
                coinView.setText(getResources().getStringArray(R.array.currency_array)[spinner.getSelectedItemPosition()]);
                currencyExchange();
            }

        });
        alert.setNegativeButton("Cancelar", null);
        alert.setView(view);
        alert.show();

    }

    private boolean lastCharIsOp (char c){
        return c == '+' || c == '^' || c == '*' || c == '/' || c == '%' || c == '-' || c == '.' || c == '√';
    }

    public void showText(){
        String a = textLine.getText().toString();
        char c = a.charAt(a.length() - 1);
        if (!lastCharIsOp(c)) {
            double res = eval(a);
            textView1.setText(cutDecimal(res));
            if (currencyFlag)
                currencyExchange();
            ans = result;
        }
    }

    private void onDelButtonClicked (){
        if(textLine.getText().length() > 0 ){

            CharSequence textSelec = textLine.getText().subSequence(textLine.getSelectionStart(),textLine.getSelectionEnd());
            if (textSelec.length()>0){
                textLine.getText().replace(textLine.getSelectionStart(),textLine.getSelectionEnd(),"");
            } else if (textLine.getSelectionStart()>0) {
                textLine.getText().delete(textLine.getSelectionStart()-1, textLine.getSelectionStart());
            }
            if (textLine.getText().length() > 1) {
                showText();
            } else{
                textView1.setText("");
            }
        }
    }

    private void onNumberButtonClicked(String num) {
        if(textLine.getText().length() == 0){
            textView1.setTextColor(Color.rgb(80,80,80));
        }
        textLine.getText().replace(textLine.getSelectionStart(), textLine.getSelectionEnd(), num);
        if (!parentesis)
          showText();
    }

    private void onOperatorButtonClicked(String operator) {
        tmp = textLine.getText().toString();
        if (tmp.length()>0) {
            char c = tmp.charAt(tmp.length()-1);
            // Si antes se habia pulsado un operador, lo reemplaza
            if (lastCharIsOp(c))
                onDelButtonClicked();
            textLine.getText().insert(textLine.getSelectionStart(),operator);
            tmp = textLine.getText().toString();
            this.operator = operator;
        } else if (textView1.getText().length() > 0){
            textLine.setText(textView1.getText() + operator);
            textLine.setSelection(textLine.getText().length());
        } else if (operator.equals("-") || operator.equals("√")){
           tmp = textLine.getText().toString();
           textLine.getText().insert(textLine.getSelectionStart(),operator);
           this.operator = operator;
        }
    }

    private void onClearButtonClicked() {
        result = "";
        textView1.setText("");
        textLine.setText("");
    }

    // funcion para cortar parte decimal de un numero
    public String cutDecimal(double num){
        String s = "";
        // Si el resultado no tiene parte decimal se muestra solo la parte entera (long) res
        if (num - (long) num == 0) {
            s = String.valueOf((long) num);
        } else {
            //result = String.valueOf(res);
            s = String.valueOf((double)Math.round(num * 100000d) / 100000d);
        }
        return s;
    }

    public static double eval(final String str) {
        return new Object() {

            int pos = -1, ch;

            void nextChar() {
                // Si pos+1 es menor que length()
                // int ch = str.charAt(pos)
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                // mientras ch = es un espacio (' ') va al proximo char
                // si es igual que el int enviado por referencia va al proximo char y retorna true
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                if (eat('%')) x = x * (parseFactor()/100);

                return x;
            }
        }.parse();
    }

    private void currencyExchange(){
        // Multiplica el texto de textView1 * el valor asignado anteriormente
        if (textView1.getText().length() == 0){
            currencyView.setText(null);
        } else {
            currencyView.setText((cutDecimal(Double.parseDouble((String) textView1.getText().toString()) * Double.parseDouble(sCurrency))));
        }
    }

    public void memoryClick (String m) {
        // Si el textView1 esta vacio busca en textLine
        if (textView1.getText().length() == 0) {
            if (textLine.getText().length() > 0) {
                numMemory = (textLine.getText().toString());
            } else {
                // Si el textView y el textline estan vacios es que se tiene que borrar
                numMemory = "";
            }
        } else {
            // la variable numMemory pasa a valer textView1
            numMemory = (textView1.getText().toString());
        }
        if (numMemory.length()>0){
            switch (m.substring(0, 2)) {
                case "M1":
                    buttonM1.setText("M1\n"+numMemory);
                    break;
                case "M2":
                    buttonM2.setText("M2\n"+numMemory);
                    break;
                case "M3":
                    buttonM3.setText("M3\n"+numMemory);
                    break;
                case "M4":
                    buttonM4.setText("M4\n"+numMemory);
                    break;
            }
        } else {
            switch (m.substring(0, 2)) {
                case "M1":
                    buttonM1.setText("M1");
                    break;
                case "M2":
                    buttonM2.setText("M2");
                    break;
                case "M3":
                    buttonM3.setText("M3");
                    break;
                case "M4":
                    buttonM4.setText("M4");
                    break;
            }
        }

        textView1.setText("");
        textLine.setText("");
    }

    private void initControl() {
        button0 =  findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);

        buttonPercent =  findViewById(R.id.buttonPercent);
        buttonDot = findViewById(R.id.buttonDot);
        buttonAns = findViewById(R.id.buttonAns);
        buttonPow = findViewById(R.id.buttonPow);


        buttonAdd = findViewById(R.id.buttonAdd);
        buttonDel = findViewById(R.id.buttonDel);
        buttonSubstract = findViewById(R.id.buttonSub);
        buttonMul = findViewById(R.id.buttonMul);
        buttonDiv = findViewById(R.id.buttonDiv);
        buttonEqual = findViewById(R.id.buttonEqual);

        textView1 = findViewById(R.id.textView1);
        currencyView = findViewById(R.id.currency_view);
        textLine = findViewById(R.id.textLine);
        coinView = findViewById(R.id.moneda);

        buttonM1 =findViewById(R.id.buttonM1);
        buttonM2 =findViewById(R.id.buttonM2);
        buttonM3 =findViewById(R.id.buttonM3);
        buttonM4 =findViewById(R.id.buttonM4);

        spinner = findViewById(R.id.currency_spinner);
    }

    private void initControlListener() {

        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("0");
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("1");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("2");
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("3");
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("4");
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("5");
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("6");
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("7");
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("8");
            }
        });
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked("9");
            }
        });

        buttonDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberButtonClicked(".");
            }
        });
        buttonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearButtonClicked();
            }
        });
        buttonSubstract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){ onOperatorButtonClicked("-");
            }
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOperatorButtonClicked("+");
            }
        });
        buttonMul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOperatorButtonClicked("*");
            }
        });
        buttonDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOperatorButtonClicked("/");
            }
        });
        buttonPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOperatorButtonClicked("%");
            }
        });
        buttonPow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOperatorButtonClicked("^");
            }
        });

        buttonM1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonM1.getText().toString().length()>2)
                    onNumberButtonClicked(buttonM1.getText().toString().substring(3));
            }
        });

        buttonM1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                memoryClick(buttonM1.getText().toString());
                return true;
            }
        });

        buttonM2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonM2.getText().toString().length()>2)
                    onNumberButtonClicked(buttonM2.getText().toString().substring(3));
            }
        });

        buttonM2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                memoryClick(buttonM2.getText().toString());
                return true;
            }
        });

        buttonM3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonM3.getText().toString().length()>2)
                    onNumberButtonClicked(buttonM3.getText().toString().substring(3));
            }
        });

        buttonM3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                memoryClick(buttonM3.getText().toString());
                return true;
            }
        });

        buttonM4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonM4.getText().toString().length()>2)
                    onNumberButtonClicked(buttonM4.getText().toString().substring(3));
            }
        });

        buttonM4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                memoryClick(buttonM4.getText().toString());
                return true;
            }
        });

        buttonEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                textLine.getText().toString().replace("√", "sqrt");
                if(textLine.getText().length()>0){
                    showText();
                    textLine.setText("");
                    textView1.setTextColor(Color.rgb(0,0,0));
                }
            }
        });

        buttonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textLine.getText().toString().length()>0)
                    onDelButtonClicked();
            }
        });

        buttonDel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClearButtonClicked();
                return true;
            }
        });

        button2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onNumberButtonClicked("^2");
                return true;
            }
        });

        button7.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                parentesis = true;
                String a = textLine.getText().toString();
                char c = a.charAt(a.length() - 1);
                if (lastCharIsOp(c)) {
                    onNumberButtonClicked("(");
                } else {
                    onNumberButtonClicked("*(");
                }

                return true;
            }
        });

        button9.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                parentesis = false;
                onNumberButtonClicked(")");
                return true;
            }
        });

//        buttonPow.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                onOperatorButtonClicked("√");
//                return true;
//            }
//        });

        //Para evitar que se muestre el teclado
        textLine.setShowSoftInputOnFocus(false);

        // Para mostrar el cursor solo si se pulsa sobre el edittext
        if (textLine.requestFocus()) {
            textLine.setCursorVisible(true);
        } else{
            textLine.setCursorVisible(false);
        }
    }
}
