package com.cansis.saad.payiee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Token;

import java.util.HashMap;
import java.util.Map;

public class ChargeCutomer2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_cutomer2);

        Intent intent=getIntent();

        Bundle b= getIntent().getExtras();
        //Token token= Token.retrieve();


// Token is created using Stripe.js or Checkout!
// Get the payment token submitted by the form:
        Stripe.apiKey = "sk_test_n7GT6DGCjytvz4TMUWYhobuM";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("amount", 1000);
        params.put("currency", "usd");
        params.put("description", "Example charge");
        //params.put("source", token);

        try {
            Charge charge = Charge.create(params);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (CardException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }
    }
}
