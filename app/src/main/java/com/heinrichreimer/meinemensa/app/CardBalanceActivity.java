/*
 * MIT License
 *
 * Copyright (c) 2017 Jan Heinrich Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
