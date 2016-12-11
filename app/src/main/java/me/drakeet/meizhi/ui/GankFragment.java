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

package me.drakeet.meizhi.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.drakeet.meizhi.DrakeetFactory;
import me.drakeet.meizhi.LoveBus;
import me.drakeet.meizhi.R;
import me.drakeet.meizhi.data.GankData;
import me.drakeet.meizhi.data.entity.Gank;
import me.drakeet.meizhi.event.OnKeyBackClickEvent;
import me.drakeet.meizhi.ui.adapter.GankListAdapter;
import me.drakeet.meizhi.ui.base.BaseActivity;
import me.drakeet.meizhi.util.LoveStrings;
import me.drakeet.meizhi.util.Once;
import me.drakeet.meizhi.util.Shares;
import me.drakeet.meizhi.util.Toasts;
import me.drakeet.meizhi.widget.LoveVideoView;
import me.drakeet.meizhi.widget.VideoImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by drakeet on 8/11/15.
 */
public class GankFragment extends Fragment {

    private final String TAG = "GankFragment";
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

    @Bind(R.id.list) RecyclerView mRecyclerView;
    @Bind(R.id.stub_empty_view) ViewStub mEmptyViewStub;
    @Bind(R.id.stub_video_view) ViewStub mVideoViewStub;
    @Bind(R.id.video_image) VideoImageView mVideoImageView;
    LoveVideoView mVideoView;

    int mYear, mMonth, mDay;
    List<Gank> mGankList;
    String mVideoPreviewUrl;
    boolean mIsVideoViewInflated = false;
    Subscription mSubscription;
    GankListAdapter mAdapter;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GankFragment newInstance(int year, int month, int day) {
        GankFragment fragment = new GankFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }


    public GankFragment() {
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGankList = new ArrayList<>();
        mAdapter = new GankListAdapter(mGankList);
        parseArguments();
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    private void parseArguments() {
        Bundle bundle = getArguments();
        mYear = bundle.getInt(ARG_YEAR);
        mMonth = bundle.getInt(ARG_MONTH);
        mDay = bundle.getInt(ARG_DAY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gank, container, false);
        ButterKnife.bind(this, rootView);
        initRecyclerView();
        setVideoViewPosition(getResources().getConfiguration());
        return rootView;
    }


    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGankList.size() == 0) loadData();
        if (mVideoPreviewUrl != null) {
            Glide.with(this).load(mVideoPreviewUrl).into(mVideoImageView);
        }
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void loadData() {
        loadVideoPreview();
        // @formatter:off
        mSubscription = BaseActivity.sGankIO
                .getGankData(mYear, mMonth, mDay)
                .map(data -> data.results)
                .map(this::addAllResults)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {showEmptyView();}
                    else {mAdapter.notifyDataSetChanged();}
                }, Throwable::printStackTrace);
        // @formatter:on
    }


    private void loadVideoPreview() {
        String where = String.format("{\"tag\":\"%d-%d-%d\"}", mYear, mMonth, mDay);
        DrakeetFactory.getDrakeetSingleton()
                      .getDGankData(where)
                      .map(dGankData -> dGankData.results)
                      .single(dGanks -> dGanks.size() > 0)
                      .map(dGanks -> dGanks.get(0))
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(dGank -> startPreview(dGank.preview),
                              throwable -> getOldVideoPreview(new OkHttpClient()));
    }


    private void getOldVideoPreview(OkHttpClient client) {
        String url = "http://gank.io/" + String.format("%s/%s/%s", mYear, mMonth, mDay);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                mVideoPreviewUrl = LoveStrings.getVideoPreviewImageUrl(body);
                startPreview(mVideoPreviewUrl);
            }
        });
    }


    private void startPreview(String preview) {
        mVideoPreviewUrl = preview;
        if (preview != null && mVideoImageView != null) {
            // @formatter:off
            mVideoImageView.post(() ->
                Glide.with(mVideoImageView.getContext())
                   .load(preview)
                   .into(mVideoImageView));
            // @formatter:on
        }
    }


    private void showEmptyView() {mEmptyViewStub.inflate();}


    private List<Gank> addAllResults(GankData.Result results) {
        if (results.androidList != null) mGankList.addAll(results.androidList);
        if (results.iOSList != null) mGankList.addAll(results.iOSList);
        if (results.appList != null) mGankList.addAll(results.appList);
        if (results.拓展资源List != null) mGankList.addAll(results.拓展资源List);
        if (results.瞎推荐List != null) mGankList.addAll(results.瞎推荐List);
        if (results.休息视频List != null) mGankList.addAll(0, results.休息视频List);
        return mGankList;
    }


    @OnClick(R.id.header_appbar) void onPlayVideo() {
        resumeVideoView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
            Toasts.showLongX2(R.string.loading);
        } else {
            closePlayer();
        }
    }


    private void setVideoViewPosition(Configuration newConfig) {
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE: {
                if (mIsVideoViewInflated) {
                    mVideoViewStub.setVisibility(View.VISIBLE);
                } else {
                    mVideoView = (LoveVideoView) mVideoViewStub.inflate();
                    mIsVideoViewInflated = true;
                    String tip = getString(R.string.tip_video_play);
                    // @formatter:off
                    new Once(mVideoView.getContext()).show(tip, () ->
                            Snackbar.make(mVideoView, tip, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.i_know, v -> {})
                                    .show());
                    // @formatter:on
                }
                if (mGankList.size() > 0 && mGankList.get(0).type.equals("休息视频")) {
                    mVideoView.loadUrl(mGankList.get(0).url);
                }
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_UNDEFINED:
            default: {
                mVideoViewStub.setVisibility(View.GONE);
                break;
            }
        }
    }


    void closePlayer() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toasts.showShort(getString(R.string.tip_for_no_gank));
    }


    @Override public void onConfigurationChanged(Configuration newConfig) {
        setVideoViewPosition(newConfig);
        super.onConfigurationChanged(newConfig);
    }


    @Subscribe public void onKeyBackClick(OnKeyBackClickEvent event) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        clearVideoView();
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                if (mGankList.size() != 0) {
                    Gank gank = mGankList.get(0);
                    String shareText = gank.desc + gank.url +
                            getString(R.string.share_from);
                    Shares.share(getActivity(), shareText);
                } else {
                    Shares.share(getContext(), R.string.share_text);
                }
                return true;
            case R.id.action_subject:
                openTodaySubject();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openTodaySubject() {
        String url = getString(R.string.url_gank_io) +
                String.format("%s/%s/%s", mYear, mMonth, mDay);
        Intent intent = WebActivity.newIntent(getActivity(), url,
                getString(R.string.action_subject));
        startActivity(intent);
    }


    @Override public void onResume() {
        super.onResume();
        LoveBus.getLovelySeat().register(this);
        resumeVideoView();
    }


    @Override public void onPause() {
        super.onPause();
        LoveBus.getLovelySeat().unregister(this);
        pauseVideoView();
        clearVideoView();
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) mSubscription.unsubscribe();
        resumeVideoView();
    }


    private void pauseVideoView() {
        if (mVideoView != null) {
            mVideoView.onPause();
            mVideoView.pauseTimers();
        }
    }


    private void resumeVideoView() {
        if (mVideoView != null) {
            mVideoView.resumeTimers();
            mVideoView.onResume();
        }
    }


    private void clearVideoView() {
        if (mVideoView != null) {
            mVideoView.clearHistory();
            mVideoView.clearCache(true);
            mVideoView.loadUrl("about:blank");
            mVideoView.pauseTimers();
        }
    }
}
