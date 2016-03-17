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

package com.refineriaweb.rx_paparazzo.library.internal.di;

import android.content.Context;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;

import dagger.Module;
import dagger.Provides;

@Module
public final class ApplicationModule {
    private final Config config;
    private final TargetUi ui;

    public ApplicationModule(Config config, Object ui) {
        this.config = config;
        this.ui = new TargetUi(ui);
    }

    @Provides Context provideContext() {
        return ui.getContext();
    }

    @Provides Config provideConfig() {
        return config;
    }

    @Provides TargetUi provideTargetUi() {
        return ui;
    }
}
