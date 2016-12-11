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

package me.drakeet.meizhi;

import android.app.Application;
import android.content.Context;
import com.litesuits.orm.LiteOrm;
import me.drakeet.meizhi.util.Toasts;

/**
 * Created by drakeet on 6/21/15.
 */
public class App extends Application {

    private static final String DB_NAME = "gank.db";
    public static Context sContext;
    public static LiteOrm sDb;


    @Override public void onCreate() {
        super.onCreate();
        sContext = this;
        Toasts.register(this);
        sDb = LiteOrm.newSingleInstance(this, DB_NAME);
        if (BuildConfig.DEBUG) {
            sDb.setDebugged(true);
        }
    }


    @Override public void onTerminate() {
        super.onTerminate();
    }
}
