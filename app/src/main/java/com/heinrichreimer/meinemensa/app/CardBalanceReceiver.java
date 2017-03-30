package com.heinrichreimer.meinemensa.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.heinrichreimer.canteenbalance.app.AbstractCardBalanceReceiver;
import com.heinrichreimer.canteenbalance.cardreader.CardBalance;

public class CardBalanceReceiver extends AbstractCardBalanceReceiver {
    @Override
    protected void onReceiveCardBalance(Context context, CardBalance cardBalance) {
        Intent intent = new Intent(context, CardBalanceActivity.class);
        intent.putExtras(cardBalance.toBundle());
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
