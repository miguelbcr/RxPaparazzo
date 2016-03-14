/*
 * Copyright 2016 Refinería Web
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx_paparazzo.workers;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx_paparazzo.entities.Config;
import rx_paparazzo.entities.Folder;
import rx_paparazzo.interactors.CropImage;
import rx_paparazzo.interactors.GrantPermissions;
import rx_paparazzo.interactors.SaveImage;
import rx_paparazzo.interactors.TakePhoto;

import static org.mockito.Mockito.when;

public class CameraTest {
    private Camera cameraUT;
    private Activity activityMock = null;
    @Mock GrantPermissions grantPermissionsMock;
    @Mock TakePhoto takePhotoMock;
    @Mock CropImage cropImageMock;
    @Mock SaveImage saveImageMock;
    @Mock Config configMock;


    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        cameraUT = new Camera(activityMock, grantPermissionsMock, takePhotoMock, cropImageMock, saveImageMock);
    }

    @Test public void when_Folder_Private_Not_GrantPermission() throws Exception {
        when(configMock.getFolder()).thenReturn(Folder.Private);
        when(grantPermissionsMock.with(activityMock, configMock.getFolder()).react())
                .thenReturn(oBrokeChain());

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        cameraUT.takePhoto().subscribe(subscriberMock);
        subscriberMock.assertNoErrors();
        subscriberMock.assertNoValues();
        subscriberMock.assertNotCompleted();
    }

    @Test public void when_No_Permission_Granted_Broke_Chain() throws Exception {

    }

    protected <D> Observable<D> oBrokeChain() {
        return Observable.<D>create(subscriber -> subscriber.onError(new RuntimeException()))
                .onErrorResumeNext(throwable -> Observable.empty());
    }
}
