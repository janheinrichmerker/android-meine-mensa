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

package com.heinrichreimer.meinemensa.view;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

@SuppressWarnings("SameParameterValue")
public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private final boolean edgeSpace;

    public SpacingItemDecoration(int space) {
        this(space, false);
    }

    public SpacingItemDecoration(int space, boolean edgeSpace) {
        this.space = space;
        this.edgeSpace = edgeSpace;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null)
            return;

        final int spanCount;
        final int orientation;
        final boolean reverseLayout;
        final GridLayoutManager.SpanSizeLookup spanSizeLookup;
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            orientation = ((GridLayoutManager) layoutManager).getOrientation();
            reverseLayout = ((GridLayoutManager) layoutManager).getReverseLayout();
            spanSizeLookup = ((GridLayoutManager) layoutManager).getSpanSizeLookup();
        } else if (layoutManager instanceof LinearLayoutManager) {
            spanCount = 1;
            orientation = ((LinearLayoutManager) layoutManager).getOrientation();
            reverseLayout = ((LinearLayoutManager) layoutManager).getReverseLayout();
            spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            };
        } else {
            spanCount = 1;
            orientation = VERTICAL;
            reverseLayout = false;
            spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            };
        }
        spanSizeLookup.setSpanIndexCacheEnabled(true);


        final int position = parent.getChildLayoutPosition(view);
        final int itemCount = parent.getAdapter().getItemCount();

        //Calculate all insets as if the layout were vertical and not reversed

        //Left & right ofsets
        int rowSpanCount = 1; //Items in this row (count the current item)
        int rowSpanIndex = 0;
        int rowSpanCountAfter = 0;
        int oldSpanIndex = spanSizeLookup.getSpanIndex(position, spanCount);
        //Count spans for items before the current item
        for (int i = 1; position - i >= 0 && spanSizeLookup.getSpanIndex(position - i, spanCount) < oldSpanIndex; i++) {
            oldSpanIndex = spanSizeLookup.getSpanIndex(position - i, spanCount);
            rowSpanCount++;
            rowSpanIndex++;
        }
        //Count spans for items after the current item
        for (int i = 1; position + i < itemCount && spanSizeLookup.getSpanIndex(position + i, spanCount) > 0; i++) {
            rowSpanCountAfter++;
        }
        rowSpanCount += rowSpanCountAfter;

        final float spaceSum = space * (rowSpanCount + (edgeSpace ? 1 : -1));
        final float spacePerSpan = spaceSum / rowSpanCount;

        //Calculate current left and right offsets by calculating the space left by the items in
        //this row before the current item
        int right = 0;
        int left = 0;
        for (int i = 0; i <= rowSpanIndex; i++) {
            if (i == 0) {
                left = edgeSpace ? space : 0;
            } else {
                left = Math.round(space - right);
            }
            right = Math.round(spacePerSpan - left);
        }

        outRect.right = right;
        outRect.left = left;

        //Top offset
        if (edgeSpace && position < spanCount) {
            int realPosition = 0;
            for (int i = 0; i <= position; i++) {
                realPosition += spanSizeLookup.getSpanSize(position);
            }
            if (realPosition < spanCount) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        } else {
            outRect.top = 0;
        }

        //Bottom offset
        if (edgeSpace) {
            outRect.bottom = space;
        } else if (position < itemCount - spanCount) {
            outRect.bottom = space;
        } else {
            int lastRowStartPosition = itemCount - 1;
            while (lastRowStartPosition > position && spanSizeLookup.getSpanIndex(lastRowStartPosition, spanCount) > 0) {
                lastRowStartPosition--;
            }

            if (position < lastRowStartPosition) {
                outRect.bottom = space;
            } else {
                outRect.bottom = 0;
            }
        }

        if (orientation == HORIZONTAL) {
            //Just rotate insets 90 degrees
            int temp = outRect.right;
            outRect.right = outRect.bottom;
            outRect.bottom = temp;

            temp = outRect.left;
            outRect.left = outRect.top;
            outRect.top = temp;

            if (reverseLayout) {
                //Just flip the offsets horizontally
                temp = outRect.left;
                outRect.left = outRect.right;
                outRect.right = temp;
            }
        } else if (reverseLayout) {
            //Just flip the offsets horizontally
            int temp = outRect.top;
            outRect.top = outRect.bottom;
            outRect.bottom = temp;
        }
    }
}