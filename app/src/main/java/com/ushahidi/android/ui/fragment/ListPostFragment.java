/*
 * Copyright (c) 2014 Ushahidi.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program in the file LICENSE-AGPL. If not, see
 * https://www.gnu.org/licenses/agpl-3.0.html
 */

package com.ushahidi.android.ui.fragment;

import com.ushahidi.android.R;
import com.ushahidi.android.core.usecase.post.FetchPost;
import com.ushahidi.android.core.usecase.post.ListPost;
import com.ushahidi.android.model.PostModel;
import com.ushahidi.android.model.mapper.PostModelDataMapper;
import com.ushahidi.android.presenter.ListPostPresenter;
import com.ushahidi.android.ui.adapter.PostAdapter;
import com.ushahidi.android.ui.view.IPostListView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;

/**
 * Shows list of posts
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class ListPostFragment extends BaseRecyclerViewFragment<PostModel, PostAdapter>
        implements
        IPostListView, RecyclerView.OnItemTouchListener {

    @Inject
    ListPost mListPost;

    @Inject
    FetchPost mFetchPost;

    @Inject
    PostModelDataMapper mPostModelDataMapper;

    @InjectView(android.R.id.empty)
    TextView mEmptyView;

    private ListPostPresenter mPostListPresenter;

    private PostListListener mPostListListener;

    public ListPostFragment() {
        super(PostAdapter.class, R.layout.list_post, 0,
                android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPostListPresenter.init();
        initRecyclerView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PostListListener) {
            mPostListListener = (PostListListener) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPostListPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPostListPresenter.pause();
    }

    @Override
    void initPresenter() {
        mPostListPresenter = new ListPostPresenter(this, mListPost, mFetchPost,
                mPostModelDataMapper);
    }

    private void setEmptyView() {
        if (mRecyclerViewAdapter != null && mRecyclerViewAdapter.getItemCount() == 0) {
            setViewGone(mEmptyView, false);
        } else {
            setViewGone(mEmptyView);
        }
    }

    private void initRecyclerView() {
        mRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                setEmptyView();
            }
        });
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        setEmptyView();
    }

    @Override
    public void renderPostList(List<PostModel> postModel) {
        if (postModel != null && mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.setItems(postModel);
        }
    }

    @Override
    public void viewPost(PostModel postModel) {
        //TODO: implement this to launch activity for viewing post detail
    }

    @Override
    public void refreshList() {
        mPostListPresenter.refreshList();
    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void showLoading() {
        //TODO: implement loading animation
    }

    @Override
    public void hideLoading() {
        //TODO: implement this
    }

    @Override
    public void showRetry() {
        //TODO: implement this
    }

    @Override
    public void hideRetry() {
        //TODO: implement this
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    /**
     * Listens for post list events
     */
    public interface PostListListener {

        void onPostClicked(final PostModel postModel);
    }
}
