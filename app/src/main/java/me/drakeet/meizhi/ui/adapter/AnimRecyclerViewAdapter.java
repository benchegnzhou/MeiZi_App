/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meizhi.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import me.drakeet.meizhi.R;

/**
 * 参考了 fython/ExpressHelper @{https://github.com/PaperAirplane-Dev-Team/ExpressHelper/blob/master/app%2Fsrc%2Fmain%2Fjava%2Finfo%2Fpapdt%2Fexpress%2Fhelper%2Fui%2Fcommon%2FMyRecyclerViewAdapter.java}
 */
public class AnimRecyclerViewAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {

    private static final int DELAY = 138;
    private int mLastPosition = -1;


    @Override public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }


    @Override public void onBindViewHolder(T holder, int position) {}


    @Override public int getItemCount() {return 0;}


    public void showItemAnim(final View view, final int position) {
        Context context = view.getContext();
        if (position > mLastPosition) {
            view.setAlpha(0);
            view.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(context,
                        R.anim.slide_in_right);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {
                        view.setAlpha(1);
                    }


                    @Override public void onAnimationEnd(Animation animation) {}


                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                view.startAnimation(animation);
            }, DELAY * position);
            mLastPosition = position;
        }
    }
}
