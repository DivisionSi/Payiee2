package com.cansis.saad.payiee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
//import com.stripe.android.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.src;

public class StripeClass extends AppCompatActivity {

    Button btn_done,btn_switch;
    CardInputWidget mCardInputWidget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);

        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        btn_done=(Button) findViewById(R.id.btn_done);
        btn_switch= (Button) findViewById(R.id.btn_switch);


        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Card card = mCardInputWidget.getCard();

                if (card == null) {

                    Toast.makeText(StripeClass.this,"Invalid Card Data",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Stripe stripe = new Stripe(getApplicationContext(), "pk_test_ajcWuwnpG7K7eFnpgTDHZ3p1");
                    stripe.createToken(
                            card,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    // Send token to your server
                                    Toast.makeText(getApplicationContext(),
                                            token.toString(),
                                            Toast.LENGTH_LONG
                                    ).show();

                                //  ChargeCustomer cc= new ChargeCustomer(token);



                                }
                                public void onError(Exception error) {
                                    // Show localized error message
                                    Toast.makeText(getApplicationContext(),
                                            "Error",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );


                }


            }
        });

        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(StripeClass.this,SquareActivity.class);
                startActivity(i);
            }
        });
    }
}
