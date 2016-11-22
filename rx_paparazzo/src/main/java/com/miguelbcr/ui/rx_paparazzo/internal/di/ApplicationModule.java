/*
 * Copyright 2016 Miguel Garcia
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

package com.miguelbcr.ui.rx_paparazzo.internal.di;

import com.miguelbcr.ui.rx_paparazzo.entities.Config;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;

public class ApplicationModule {

    private final Config config;
    private final TargetUi ui;

    public ApplicationModule(Config config, Object originUi) {
        this.config = config;
        ui = new TargetUi(originUi);
    }

    public Config getConfig() {
        return config;
    }

    public TargetUi getUi() {
        return ui;
    }
}
