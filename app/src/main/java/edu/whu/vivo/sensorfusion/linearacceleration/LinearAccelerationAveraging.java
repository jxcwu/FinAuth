package edu.whu.vivo.sensorfusion.linearacceleration;


/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import edu.whu.vivo.sensorfusion.filter.averaging.AveragingFilter;

/**
 * A wrapper for Linear Acceleration filters that are backed by Averaging filters.
 * Created by kaleb on 7/6/17.
 */

public class LinearAccelerationAveraging extends LinearAcceleration {

    public LinearAccelerationAveraging(AveragingFilter averagingFilter) {
        super(averagingFilter);
    }

    @Override
    public float[] getGravity() {
        return filter.getOutput();
    }
}
