package com.heinrichreimer.meinemensa.app;

import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.heinrichreimer.canteenbalance.app.AbstractCardBalanceActivity;
import com.heinrichreimer.canteenbalance.cardreader.CardBalance;
import com.heinrichreimer.meinemensa.R;

public class CardBalanceActivity extends AbstractCardBalanceActivity {

    private MaterialDialog dialog;

    @Override
    protected void onReceiveCardBalance(CardBalance balance) {
        if (dialog != null) {
            dialog.dismiss();
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(getString(R.string.title_dialog_balance, balance.getBalance()));
        if (balance.isLastTransactionSupported()) {
            builder.content(getString(R.string.description_dialog_balance, balance.getLastTransaction()));
        }
        builder.positiveText(android.R.string.ok);
        builder.autoDismiss(true);
        builder.canceledOnTouchOutside(true);
        builder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });

        dialog = builder.show();
    }

    @Override
    protected void onPause() {
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }
}
