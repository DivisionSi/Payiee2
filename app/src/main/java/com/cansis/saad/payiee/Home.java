package com.cansis.saad.payiee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    Integer Amount,tempAmount;
    String strAmount, strTempAmount;
    GridView gridView;
    TextView txtCharge,txtTemp;
    boolean firstTap = true;
    String txtItem;
    static final String[] numbers = new String[] {
            "1", "2", "3", "4", "5" , "6", "7" , "8", "9", "C", "0" , "+"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Amount=0;
        tempAmount=0;
        strTempAmount = "";
        strAmount = "";

        gridView = (GridView) findViewById(R.id.gridView1);

        txtCharge= (TextView) findViewById(R.id.txtCharge);
        txtTemp = (TextView) findViewById(R.id.txtTemporary);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, numbers);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                txtItem=((TextView) v).getText().toString();


                if (txtItem.equals("C"))
                {
                    txtCharge.setText("Charge Rs ");
                    txtTemp.setText("Charge Rs ");

                    Amount = 0;
                    tempAmount=0;
                    strTempAmount ="";
                    strAmount="";



                }
                else if (! txtItem.equals("+") )
                    {
                        Integer temp=0;

                        //First Tap on Numpad
                        if (firstTap) {
                            txtCharge.setText("Charge Rs " );
                            txtTemp.setText("Rs ");

                            tempAmount = tempAmount + Integer.parseInt(txtItem);

                            temp= Amount + tempAmount;

                            strAmount= Integer.toString(temp);
                            strTempAmount = txtItem;

                            txtCharge.setText(txtCharge.getText() + strAmount);
                            txtTemp.setText(txtTemp.getText() + strTempAmount);
                            firstTap=false;

                        }
                        //Subsequent Taps on Numpad
                        else {




                            strTempAmount = strTempAmount + txtItem;


                            txtTemp.setText("Rs " + strTempAmount);

                            tempAmount=Integer.parseInt(strTempAmount);

                            temp= Amount + tempAmount;
                            strAmount= Integer.toString(temp);
                            txtCharge.setText("Charge Rs " + strAmount);

                        }
                 } //Pressing +
                else
                {
                    txtTemp.setText("Rs ");
                    tempAmount=0;
                    strTempAmount="";
                    Amount=Integer.parseInt(strAmount);
                }

            }

        });

        txtCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Home.this, StripeClass.class);
                startActivity(i);

            }
        });
    }
}
