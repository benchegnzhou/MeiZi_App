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

package me.drakeet.meizhi.util;

/**
 * Created by drakeet on 8/14/15.
 */
public class LoveStrings {

    public static String getVideoPreviewImageUrl(String resp) {
        int s0 = resp.indexOf("<h1>休息视频</h1>");
        if (s0 == -1) return null;
        int s1 = resp.indexOf("<img", s0);
        if (s1 == -1) return null;
        int s2 = resp.indexOf("http:", s1);
        if (s2 == -1) return null;
        int e2 = resp.indexOf(".jpg", s2) + ".jpg".length();
        if (e2 == -1) return null;
        try {
            return resp.substring(s2, e2);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }
}
