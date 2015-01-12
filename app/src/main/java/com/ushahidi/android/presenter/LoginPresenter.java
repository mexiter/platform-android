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

package com.ushahidi.android.presenter;

import com.google.common.base.Preconditions;

import com.ushahidi.android.Util.ApiServiceUtil;
import com.ushahidi.android.core.entity.Deployment;
import com.ushahidi.android.core.entity.UserAccount;
import com.ushahidi.android.core.exception.ErrorWrap;
import com.ushahidi.android.core.respository.IUserAccountRepository;
import com.ushahidi.android.core.usecase.account.Login;
import com.ushahidi.android.core.usecase.deployment.ListDeployment;
import com.ushahidi.android.data.Constants;
import com.ushahidi.android.data.api.service.UserService;
import com.ushahidi.android.data.entity.mapper.UserAccountEntityMapper;
import com.ushahidi.android.data.repository.UserAccountDataRepository;
import com.ushahidi.android.data.repository.datasource.account.UserAccountDataSourceFactory;
import com.ushahidi.android.exception.ErrorMessageFactory;
import com.ushahidi.android.model.DeploymentModel;
import com.ushahidi.android.model.UserAccountModel;
import com.ushahidi.android.model.mapper.DeploymentModelDataMapper;
import com.ushahidi.android.model.mapper.UserAccountModelDataMapper;
import com.ushahidi.android.ui.view.ILoadViewData;

import android.accounts.AccountManager;
import android.content.Context;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class LoginPresenter implements IPresenter {

    private final Login mLogin;

    private final ListDeployment mListDeployment;

    private final UserAccountModelDataMapper mUserAccountModelDataMapper;

    private final DeploymentModelDataMapper mDeploymentModelDataMapper;

    private final AccountManager mAccountManager;

    private final UserAccountEntityMapper mUserAccountEntityMapper;

    private final Context mContext;

    private final ApiServiceUtil mApiServiceUtil;

    private final Login.Callback mCallback = new Login.Callback() {

        @Override
        public void onUserAccountLoggedIn(UserAccount userAccount) {
            final UserAccountModel userAccountModel = mUserAccountModelDataMapper.map(userAccount);
            mView.loggedIn(userAccountModel);
            mView.hideLoading();
        }

        @Override
        public void onError(ErrorWrap error) {
            mView.hideLoading();
            showErrorMessage(error);
        }
    };

    private final ListDeployment.Callback mListDeploymentCallback = new ListDeployment.Callback() {

        @Override
        public void onDeploymentListLoaded(List<Deployment> listDeployment) {
            deploymentList(listDeployment);
        }

        @Override
        public void onError(ErrorWrap error) {
            showErrorMessage(error);
        }
    };

    private View mView;

    @Inject
    public LoginPresenter(Login login, ListDeployment listDeployment,
            AccountManager accountManager,
            UserAccountEntityMapper userAccountEntityMapper,
            UserAccountModelDataMapper userAccountModelDataMapper,
            DeploymentModelDataMapper deploymentModelDataMapper,
            ApiServiceUtil apiServiceUtil,
            Context context) {
        mLogin = Preconditions.checkNotNull(login, "Login use case cannot be null.");
        mAccountManager = Preconditions
                .checkNotNull(accountManager, "Account Manager cannot be null");
        mUserAccountEntityMapper = Preconditions
                .checkNotNull(userAccountEntityMapper, "UserAccount Entity Mapper cannot be null");
        mListDeployment = Preconditions.checkNotNull(listDeployment,
                "List deployment use case cannot be null.");
        mUserAccountModelDataMapper = Preconditions
                .checkNotNull(userAccountModelDataMapper, "User model data mapper cannot be null.");
        mDeploymentModelDataMapper = Preconditions.checkNotNull(deploymentModelDataMapper,
                "Deployment Mapper cannot be null.");
        mApiServiceUtil = apiServiceUtil;
        mContext = context;
    }

    public void setUserService(UserService userService) {
        UserAccountDataSourceFactory userAccountDataSourceFactory
                = new UserAccountDataSourceFactory(mContext,
                userService);
        IUserAccountRepository userAccountRepository = UserAccountDataRepository
                .getInstance(mAccountManager, userAccountDataSourceFactory,
                        mUserAccountEntityMapper);
        mLogin.setUserAccountRepository(userAccountRepository);
    }

    public void login(UserAccountModel userAccountModel, DeploymentModel deploymentModel) {
        UserAccount userAccount = mUserAccountModelDataMapper.unmap(userAccountModel);
        UserService userService = mApiServiceUtil
                .createService(UserService.class, deploymentModel.getUrl(),
                        Constants.USHAHIDI_AUTHTOKEN_PASSWORD_TYPE);
        this.setUserService(userService);
        mLogin.execute(userAccount, mCallback);
    }

    public void setView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null.");
        }
        mView = view;
    }

    private void showErrorMessage(ErrorWrap errorWrap) {
        String errorMessage = ErrorMessageFactory.create(mView.getContext(),
                errorWrap.getException());
        mView.showError(errorMessage);
    }

    private void deploymentList(List<Deployment> listDeployments) {
        final List<DeploymentModel> deploymentModelsList =
                mDeploymentModelDataMapper.map(listDeployments);
        mView.deploymentList(deploymentModelsList);
    }

    public int getActiveDeploymetPosition(List<DeploymentModel> deploymentModels) {
        int position = 0;
        for (int i = 0; i < deploymentModels.size(); i++) {
            if (deploymentModels.get(i).getStatus() == DeploymentModel.Status.ACTIVATED) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void getDeploymentList() {
        mListDeployment.execute(mListDeploymentCallback);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public interface View extends ILoadViewData {

        /**
         * loggedIn
         *
         * @param userModel The collection of {@link com.ushahidi.android.model.PostModel} that will
         *                  be shown.
         */
        void loggedIn(UserAccountModel userModel);

        void deploymentList(List<DeploymentModel> deploymentModelList);

    }
}
