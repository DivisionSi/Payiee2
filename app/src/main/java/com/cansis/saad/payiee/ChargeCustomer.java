package com.cansis.saad.payiee;

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

/**
 * Created by Saad on 31/05/2017.
 */

public class ChargeCustomer {

    ChargeCustomer(com.stripe.android.model.Token token) {

        Stripe.apiKey = "sk_test_n7GT6DGCjytvz4TMUWYhobuM";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("amount", 1000);
        params.put("currency", "usd");
        params.put("description", "Example charge");
        params.put("source", token);

        try {
            Charge charge = new Charge();
            charge.create(params);
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
