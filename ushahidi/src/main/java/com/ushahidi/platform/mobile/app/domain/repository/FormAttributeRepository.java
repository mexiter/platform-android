package com.ushahidi.platform.mobile.app.domain.repository;

import com.ushahidi.platform.mobile.app.domain.entity.FormAttribute;
import com.ushahidi.platform.mobile.app.domain.entity.From;

import java.util.List;

import rx.Observable;

/**
 * @author Ushahidi Team <team@ushahidi.com>
 */
public interface FormAttributeRepository {

    /**
     * Get a list of {@link FormAttribute} from either the database or online.
     *
     * @param deploymentId An ID of {@link com.ushahidi.platform.mobile.app.domain.entity.Deployment}
     * @param formId       The form Id
     * @return The retrieved form attribute.
     */
    Observable<List<FormAttribute>> getFormAttributes(Long deploymentId, Long formId, From from);
}
